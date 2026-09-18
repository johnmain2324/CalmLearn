package com.education.calmlearn.ui.listening

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.education.calmlearn.R
import com.education.calmlearn.data.mock.MockData
import com.education.calmlearn.data.model.ListeningLesson
import com.education.calmlearn.data.progress.ProgressRepositoryProvider
import com.education.calmlearn.databinding.FragmentListeningBinding
import com.education.calmlearn.ui.common.OptionsController
import com.education.calmlearn.ui.common.StudySessionTracker
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Locale
import kotlinx.coroutines.launch

class ListeningFragment : Fragment() {

    private var _binding: FragmentListeningBinding? = null
    private val binding get() = _binding!!

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var lesson: ListeningLesson
    private lateinit var optionsController: OptionsController
    private val studyTracker = StudySessionTracker()

    private var textToSpeech: TextToSpeech? = null
    private var ttsReady = false

    private var elapsedSeconds = 0
    private var isPlaying = false
    private var currentQuestionIndex = 0
    private var questionChecked = false

    /** Chi con dung de cap nhat thanh tien do/dong ho theo tung giay TRONG KHI dang phat that
     *  (isPlaying=true) - viec DUNG phat khi noi xong duoc quyet dinh boi su kien that cua
     *  TextToSpeech (onStopPlayback(), goi tu UtteranceProgressListener.onDone/onError), khong con
     *  phai doan mo theo lesson.durationSeconds nhu truoc. */
    private val tickRunnable = object : Runnable {
        override fun run() {
            if (_binding == null || !isPlaying) return
            elapsedSeconds += 1
            updatePlayerUi()
            if (isPlaying) handler.postDelayed(this, 1000)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListeningBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val lessonId = requireArguments().getString("lessonId") ?: MockData.listeningLessons.first().id
        lesson = MockData.listeningLessons.firstOrNull { it.id == lessonId } ?: MockData.listeningLessons.first()

        binding.lessonTitle.text = lesson.title
        binding.lessonLevel.text = lesson.levelTag
        updatePlayerUi()

        binding.transcriptList.layoutManager = LinearLayoutManager(requireContext())
        binding.transcriptList.adapter = TranscriptAdapter(lesson.transcript)

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        textToSpeech = TextToSpeech(requireContext()) { status ->
            ttsReady = status == TextToSpeech.SUCCESS
            if (ttsReady) {
                textToSpeech?.language = Locale.US
                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {
                        if (utteranceId == UTTERANCE_ID_LAST_LINE) stopPlaybackOnMainThread()
                    }
                    override fun onError(utteranceId: String?, errorCode: Int) {
                        stopPlaybackOnMainThread()
                    }
                    @Deprecated("Deprecated in the platform API - required override on API < 21")
                    override fun onError(utteranceId: String?) {
                        stopPlaybackOnMainThread()
                    }
                })
            }
        }

        binding.btnPlayPause.setOnClickListener {
            if (isPlaying) stopPlayback() else startPlayback()
        }

        optionsController = OptionsController(binding.optionsContainer)
        renderQuestion(0)

        binding.btnQuestionAction.setOnClickListener { onQuestionActionClicked() }

        binding.btnCompletionHome.setOnClickListener {
            requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)?.selectedItemId =
                R.id.practiceFragment
        }
    }

    private fun renderQuestion(index: Int) {
        currentQuestionIndex = index
        questionChecked = false
        val question = lesson.questions[index]
        binding.questionProgress.text = getString(
            R.string.quiz_question_format, index + 1, lesson.questions.size
        )
        binding.questionText.text = question.question
        binding.questionExplanation.visibility = View.GONE
        binding.btnQuestionAction.setText(R.string.action_check)
        optionsController.bind(question.options) {}
    }

    private fun onQuestionActionClicked() {
        val question = lesson.questions[currentQuestionIndex]
        if (!questionChecked) {
            optionsController.selectedIndex() ?: return
            optionsController.showResult(question.correctIndex)
            binding.questionExplanation.text = question.explanation
            binding.questionExplanation.visibility = View.VISIBLE
            questionChecked = true
            binding.btnQuestionAction.setText(R.string.action_continue)
        } else {
            val next = currentQuestionIndex + 1
            if (next < lesson.questions.size) {
                renderQuestion(next)
            } else {
                binding.questionGroup.visibility = View.GONE
                binding.completionGroup.visibility = View.VISIBLE
                persistLessonCompleted()
            }
        }
    }

    /** Ghi nhan da hoan thanh xong toan bo cau hoi cua bai nghe nay - xem
     *  ProgressRepository.recordListeningLessonCompleted. */
    private fun persistLessonCompleted() {
        viewLifecycleOwner.lifecycleScope.launch {
            ProgressRepositoryProvider.repository.recordListeningLessonCompleted(lesson.id)
        }
    }

    /** Phat that toan bo hoi thoai bang TextToSpeech (khong con dem gio suong nhu truoc) - lan
     *  luot doc tung dong trong lesson.transcript, "Nhan vien"/"Khach" deu duoc doc cung mot giong
     *  he thong (TTS khong phan biet nguoi noi). Vi TTS khong ho tro tam dung/tiep tuc giua chung
     *  mot cau, bam tam dung se dung han va bam phat lai se doc lai tu dau. */
    private fun startPlayback() {
        val engine = textToSpeech
        if (engine == null || !ttsReady) {
            Toast.makeText(requireContext(), R.string.listening_tts_unavailable, Toast.LENGTH_SHORT).show()
            return
        }
        isPlaying = true
        elapsedSeconds = 0
        binding.btnPlayPause.setImageResource(R.drawable.ic_pause)
        updatePlayerUi()

        lesson.transcript.forEachIndexed { index, line ->
            val queueMode = if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            val utteranceId = if (index == lesson.transcript.lastIndex) UTTERANCE_ID_LAST_LINE else "line_$index"
            engine.speak(line.line, queueMode, null, utteranceId)
        }
        handler.postDelayed(tickRunnable, 1000)
    }

    private fun stopPlayback() {
        isPlaying = false
        textToSpeech?.stop()
        handler.removeCallbacks(tickRunnable)
        if (_binding != null) {
            binding.btnPlayPause.setImageResource(R.drawable.ic_play)
        }
    }

    /** UtteranceProgressListener chay tren luong nen cua TTS engine, khong phai main thread - phai
     *  quay ve main thread truoc khi dung cham vao View. */
    private fun stopPlaybackOnMainThread() {
        activity?.runOnUiThread { stopPlayback() }
    }

    private fun updatePlayerUi() {
        binding.playerProgress.max = lesson.durationSeconds
        // TTS that su co the noi lau/nhanh hon uoc luong durationSeconds trong MockData - gioi han
        // gia tri hien thi de thanh tien do khong vuot qua max, dong ho van hien so giay that.
        binding.playerProgress.progress = elapsedSeconds.coerceAtMost(lesson.durationSeconds)
        binding.playerTime.text = "${formatTime(elapsedSeconds)} / ${formatTime(lesson.durationSeconds)}"
    }

    private fun formatTime(totalSeconds: Int): String {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    override fun onResume() {
        super.onResume()
        studyTracker.start()
    }

    override fun onPause() {
        val seconds = studyTracker.elapsedSecondsAndReset()
        if (seconds > 0) {
            lifecycleScope.launch { ProgressRepositoryProvider.repository.addStudySeconds(seconds) }
        }
        super.onPause()
    }

    override fun onDestroyView() {
        handler.removeCallbacksAndMessages(null)
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        const val UTTERANCE_ID_LAST_LINE = "listening_last_line"
    }
}
