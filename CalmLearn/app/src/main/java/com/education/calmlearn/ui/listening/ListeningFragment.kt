package com.education.calmlearn.ui.listening

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.education.calmlearn.R
import com.education.calmlearn.data.mock.MockData
import com.education.calmlearn.data.model.ListeningLesson
import com.education.calmlearn.databinding.FragmentListeningBinding
import com.education.calmlearn.ui.common.OptionsController
import com.google.android.material.bottomnavigation.BottomNavigationView

class ListeningFragment : Fragment() {

    private var _binding: FragmentListeningBinding? = null
    private val binding get() = _binding!!

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var lesson: ListeningLesson
    private lateinit var optionsController: OptionsController

    private var elapsedSeconds = 0
    private var isPlaying = false
    private var currentQuestionIndex = 0
    private var questionChecked = false

    private val tickRunnable = object : Runnable {
        override fun run() {
            if (_binding == null || !isPlaying) return
            elapsedSeconds += 1
            if (elapsedSeconds >= lesson.durationSeconds) {
                elapsedSeconds = 0
                isPlaying = false
                binding.btnPlayPause.setImageResource(R.drawable.ic_play)
            }
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

        binding.btnPlayPause.setOnClickListener {
            isPlaying = !isPlaying
            binding.btnPlayPause.setImageResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
            if (isPlaying) handler.postDelayed(tickRunnable, 1000)
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
            }
        }
    }

    private fun updatePlayerUi() {
        binding.playerProgress.max = lesson.durationSeconds
        binding.playerProgress.progress = elapsedSeconds
        binding.playerTime.text = "${formatTime(elapsedSeconds)} / ${formatTime(lesson.durationSeconds)}"
    }

    private fun formatTime(totalSeconds: Int): String {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    override fun onDestroyView() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroyView()
        _binding = null
    }
}
