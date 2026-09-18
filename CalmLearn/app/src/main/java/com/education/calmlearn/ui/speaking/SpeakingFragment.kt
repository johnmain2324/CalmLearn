package com.education.calmlearn.ui.speaking

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.education.calmlearn.R
import com.education.calmlearn.data.mock.MockData
import com.education.calmlearn.data.model.SpeakingItem
import com.education.calmlearn.data.model.SpeakingLevel
import com.education.calmlearn.data.progress.ProgressRepositoryProvider
import com.education.calmlearn.databinding.FragmentSpeakingBinding
import com.education.calmlearn.ui.common.StudySessionTracker
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Locale
import kotlinx.coroutines.launch

private enum class MicState { IDLE, LISTENING, PROCESSING, RESULT }

class SpeakingFragment : Fragment() {

    private var _binding: FragmentSpeakingBinding? = null
    private val binding get() = _binding!!

    private var currentLevel = SpeakingLevel.WORD
    private var items: List<SpeakingItem> = emptyList()
    private var currentIndex = 0
    private var micState = MicState.IDLE
    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var ttsReady = false
    private val studyTracker = StudySessionTracker()

    private val requestAudioPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            beginListening()
        } else {
            Toast.makeText(requireContext(), R.string.speaking_permission_denied, Toast.LENGTH_SHORT).show()
        }
    }

    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}

        override fun onEndOfSpeech() {
            startProcessing()
        }

        override fun onError(error: Int) {
            finishWithResult(isCorrect = false)
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).orEmpty()
            val target = items.getOrNull(currentIndex)?.text.orEmpty()
            finishWithResult(isCorrect = matches.any { matchesTarget(it, target) })
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSpeakingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        binding.tabWord.setOnClickListener { selectLevel(SpeakingLevel.WORD) }
        binding.tabPhrase.setOnClickListener { selectLevel(SpeakingLevel.PHRASE) }
        binding.tabSentence.setOnClickListener { selectLevel(SpeakingLevel.SENTENCE) }

        textToSpeech = TextToSpeech(requireContext()) { status ->
            ttsReady = status == TextToSpeech.SUCCESS
            if (ttsReady) textToSpeech?.language = Locale.US
        }
        binding.btnPlaySample.setOnClickListener { playSample() }

        binding.micButton.setOnClickListener {
            if (micState == MicState.IDLE) onMicTapped()
        }

        binding.btnResultSecondary.setOnClickListener { resetToIdle() }
        binding.btnResultPrimary.setOnClickListener { goToNextItem() }
        binding.btnCompletionHome.setOnClickListener {
            requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)?.selectedItemId =
                R.id.practiceFragment
        }

        selectLevel(SpeakingLevel.WORD)
    }

    private fun selectLevel(level: SpeakingLevel) {
        currentLevel = level
        items = MockData.speakingItemsFor(level)
        currentIndex = 0

        val activeBg = R.drawable.bg_pill_teal
        val activeColor = ContextCompat.getColor(requireContext(), R.color.white)
        val inactiveColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)
        listOf(binding.tabWord, binding.tabPhrase, binding.tabSentence).forEach { tab ->
            tab.background = null
            tab.setTextColor(inactiveColor)
        }
        val activeTab = when (level) {
            SpeakingLevel.WORD -> binding.tabWord
            SpeakingLevel.PHRASE -> binding.tabPhrase
            SpeakingLevel.SENTENCE -> binding.tabSentence
        }
        activeTab.setBackgroundResource(activeBg)
        activeTab.setTextColor(activeColor)

        binding.completionGroup.visibility = View.GONE
        showPracticeContent()
        renderCurrentItem()
    }

    private fun renderCurrentItem() {
        val item = items.getOrNull(currentIndex) ?: return
        binding.contentText.text = item.text
        binding.contentPhonetic.text = item.phonetic
        binding.contentMeaning.text = item.meaningVi
        binding.contentTip.text = item.tip
        binding.speakingProgress.text =
            getString(R.string.speaking_progress_format, currentIndex + 1, items.size)
        resetToIdle()
    }

    /** Kiem tra thiet bi co ho tro nhan dien giong noi khong, roi xin quyen RECORD_AUDIO neu can,
     *  truoc khi thuc su bat dau nghe (xem beginListening()). */
    private fun onMicTapped() {
        if (!SpeechRecognizer.isRecognitionAvailable(requireContext())) {
            Toast.makeText(requireContext(), R.string.speaking_recognition_unavailable, Toast.LENGTH_SHORT).show()
            return
        }
        val hasPermission = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            beginListening()
        } else {
            requestAudioPermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    /** Doc mau THAT noi dung can luyen bang TextToSpeech (truoc day la nut khong lam gi ca). */
    private fun playSample() {
        val engine = textToSpeech
        val target = items.getOrNull(currentIndex)?.text
        if (engine == null || !ttsReady || target.isNullOrBlank()) {
            Toast.makeText(requireContext(), R.string.speaking_tts_unavailable, Toast.LENGTH_SHORT).show()
            return
        }
        engine.speak(target, TextToSpeech.QUEUE_FLUSH, null, "speaking_sample")
    }

    /** Bat dau nghe THAT bang SpeechRecognizer cua he thong (khong con mo phong) - ket qua dung/
     *  sai duoc quyet dinh boi noi dung nguoi dung THAT SU noi ra, so khop voi [SpeakingItem.text]
     *  (xem matchesTarget()), khong con Random. */
    private fun beginListening() {
        micState = MicState.LISTENING
        binding.micButton.setBackgroundResource(R.drawable.bg_circle_teal)
        binding.micHint.setText(R.string.speaking_listening_hint)
        binding.resultGroup.visibility = View.GONE

        val recognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())
        speechRecognizer = recognizer
        recognizer.setRecognitionListener(recognitionListener)
        recognizer.startListening(
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, requireContext().packageName)
            }
        )
    }

    private fun startProcessing() {
        if (_binding == null) return
        micState = MicState.PROCESSING
        binding.micButton.setBackgroundResource(R.drawable.bg_circle_amber)
        binding.micButton.visibility = View.INVISIBLE
        binding.micProcessingSpinner.visibility = View.VISIBLE
        binding.micHint.setText(R.string.speaking_processing_hint)
    }

    private fun finishWithResult(isCorrect: Boolean) {
        speechRecognizer?.destroy()
        speechRecognizer = null
        if (_binding == null) return

        micState = MicState.RESULT
        binding.micProcessingSpinner.visibility = View.GONE
        binding.micButton.visibility = View.VISIBLE
        binding.micButton.setBackgroundResource(R.drawable.bg_circle_coral)

        binding.resultGroup.visibility = View.VISIBLE
        binding.btnResultSecondary.visibility = if (isCorrect) View.GONE else View.VISIBLE

        if (isCorrect) {
            binding.resultBanner.setBackgroundResource(R.drawable.bg_tile_success)
            binding.resultIcon.setBackgroundResource(R.drawable.bg_icon_circle_success_light)
            binding.resultIcon.setImageResource(R.drawable.ic_check)
            binding.resultIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.success))
            binding.resultTitle.setText(R.string.speaking_correct_title)
            binding.resultDesc.setText(R.string.speaking_correct_desc)
            binding.micHint.setText(R.string.speaking_idle_hint)
        } else {
            binding.resultBanner.setBackgroundResource(R.drawable.bg_tile_error)
            binding.resultIcon.setBackgroundResource(R.drawable.bg_icon_circle_error_light)
            binding.resultIcon.setImageResource(R.drawable.ic_close)
            binding.resultIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.error))
            binding.resultTitle.setText(R.string.speaking_incorrect_title)
            binding.resultDesc.setText(R.string.speaking_incorrect_desc)
            binding.micHint.setText(R.string.speaking_idle_hint)
        }
    }

    /** So khop noi dung nhan dien duoc voi noi dung can luyen - bo qua hoa/thuong, dau cau, va
     *  chap nhan khop mot phan (SpeechRecognizer hay them/bot tu dem khi nghe cau dai). */
    private fun matchesTarget(spoken: String, target: String): Boolean {
        val normalizedSpoken = normalize(spoken)
        val normalizedTarget = normalize(target)
        if (normalizedTarget.isEmpty()) return false
        return normalizedSpoken == normalizedTarget ||
            normalizedSpoken.contains(normalizedTarget) ||
            normalizedTarget.contains(normalizedSpoken)
    }

    private fun normalize(text: String): String =
        text.lowercase(Locale.US).replace(Regex("[^a-z0-9 ]"), "").trim().replace(Regex("\\s+"), " ")

    private fun resetToIdle() {
        micState = MicState.IDLE
        speechRecognizer?.destroy()
        speechRecognizer = null
        binding.micButton.visibility = View.VISIBLE
        binding.micButton.setBackgroundResource(R.drawable.bg_circle_coral)
        binding.micProcessingSpinner.visibility = View.GONE
        binding.micHint.setText(R.string.speaking_idle_hint)
        binding.resultGroup.visibility = View.GONE
    }

    private fun goToNextItem() {
        val next = currentIndex + 1
        if (next < items.size) {
            currentIndex = next
            renderCurrentItem()
        } else {
            showPracticeContent(visible = false)
            binding.completionGroup.visibility = View.VISIBLE
            persistSessionCompleted()
        }
    }

    /** Ghi nhan da hoan thanh mot luot luyen phat am o cap do hien tai - xem
     *  ProgressRepository.recordSpeakingSessionCompleted. Du ket qua dung/sai tung cau gio la THAT
     *  (SpeechRecognizer), man hinh nay van la luyen tap (khong gioi han so lan thu lai nhu Quiz)
     *  nen van chi ghi nhan SU KIEN hoan thanh ca luot, khong luu do chinh xac tung cau. */
    private fun persistSessionCompleted() {
        viewLifecycleOwner.lifecycleScope.launch {
            ProgressRepositoryProvider.repository.recordSpeakingSessionCompleted(currentLevel.name)
        }
    }

    private fun showPracticeContent(visible: Boolean = true) {
        binding.practiceContentGroup.visibility = if (visible) View.VISIBLE else View.GONE
        if (visible) binding.completionGroup.visibility = View.GONE
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
        speechRecognizer?.destroy()
        speechRecognizer = null
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        super.onDestroyView()
        _binding = null
    }
}
