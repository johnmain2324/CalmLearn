package com.education.calmlearn.ui.lessons.word

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
import com.education.calmlearn.data.chatbot.ChatbotErrorReason
import com.education.calmlearn.data.chatbot.ChatbotRepositoryProvider
import com.education.calmlearn.data.chatbot.ChatbotResult
import com.education.calmlearn.data.mock.MockData
import com.education.calmlearn.data.model.VocabWord
import com.education.calmlearn.data.progress.ProgressRepositoryProvider
import com.education.calmlearn.data.progress.ProgressResult
import com.education.calmlearn.databinding.FragmentWordPracticeBinding
import com.education.calmlearn.databinding.ItemSentenceInputBinding
import com.education.calmlearn.ui.chatbot.toMessageRes
import com.education.calmlearn.ui.common.SpeechMatcher
import com.education.calmlearn.ui.common.hideKeyboard
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private enum class MicState { IDLE, LISTENING, PROCESSING }

class WordPracticeFragment : Fragment() {

    private var _binding: FragmentWordPracticeBinding? = null
    private val binding get() = _binding!!

    private var word: VocabWord? = null
    private val sentenceInputs = mutableListOf<ItemSentenceInputBinding>()

    private var gradeButtonLabel: CharSequence = ""
    private var successfulRounds = 0
    private var micState = MicState.IDLE
    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var ttsReady = false

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
            finishRound(isCorrect = false)
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).orEmpty()
            val target = sampleText()
            finishRound(isCorrect = matches.any { SpeechMatcher.matches(it, target) })
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWordPracticeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        viewLifecycleOwner.lifecycleScope.launch {
            MockData.ensureVocabWordsLoaded()
            val wordId = requireArguments().getString("wordId")
            val found = MockData.vocabWords.firstOrNull { it.id == wordId }
            if (found == null) {
                findNavController().popBackStack()
                return@launch
            }
            word = found
            setupStep1(found)
            setupStep2()
        }
    }

    private fun setupStep1(word: VocabWord) {
        binding.tvWord.text = word.word
        binding.tvMeaning.text = word.meaningVi
        binding.tvStep.setText(R.string.word_practice_step1)
        binding.tvStep1Hint.text = getString(R.string.word_practice_step1_hint, word.word)

        for (index in 1..WordPracticeGrader.SENTENCE_COUNT) {
            val input = ItemSentenceInputBinding.inflate(layoutInflater, binding.sentenceContainer, false)
            input.tvSentenceIndex.text = getString(R.string.word_practice_sentence_index, index)
            binding.sentenceContainer.addView(input.root)
            sentenceInputs.add(input)
        }

        gradeButtonLabel = getString(R.string.word_practice_grade)
        binding.btnGrade.setOnClickListener { grade() }
        binding.btnContinue.setOnClickListener { showStep2() }
    }

    private fun grade() {
        val currentWord = word ?: return
        val sentences = sentenceInputs.map { it.etSentence.text.toString().trim() }
        if (sentences.any { it.isEmpty() }) {
            binding.tvInputError.visibility = View.VISIBLE
            return
        }
        binding.tvInputError.visibility = View.GONE
        binding.root.hideKeyboard()
        setGrading(true)

        viewLifecycleOwner.lifecycleScope.launch {
            val result = try {
                ChatbotRepositoryProvider.repository.sendMessage(
                    history = emptyList(),
                    userMessage = WordPracticeGrader.buildPrompt(currentWord.word, sentences),
                    systemInstruction = WordPracticeGrader.SYSTEM_INSTRUCTION
                )
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (unexpected: Exception) {
                ChatbotResult.Error(ChatbotErrorReason.UNKNOWN)
            }
            if (_binding == null) return@launch
            setGrading(false)

            when (result) {
                is ChatbotResult.Success -> showGrade(WordPracticeGrader.parse(result.reply))
                is ChatbotResult.Error ->
                    Toast.makeText(requireContext(), result.reason.toMessageRes(), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setGrading(grading: Boolean) {
        binding.gradeProgress.visibility = if (grading) View.VISIBLE else View.GONE
        binding.btnGrade.text = if (grading) "" else gradeButtonLabel
        binding.btnGrade.isEnabled = !grading
        sentenceInputs.forEach { it.etSentence.isEnabled = !grading }
    }

    private fun showGrade(result: GradeResult) {
        binding.feedbackCard.visibility = View.VISIBLE
        when (result) {
            is GradeResult.Passed -> {
                binding.tvFeedbackTitle.setText(R.string.word_practice_passed)
                binding.tvFeedback.text = result.feedback
                sentenceInputs.forEach { it.etSentence.isEnabled = false }
                binding.btnGrade.visibility = View.GONE
                binding.btnContinue.visibility = View.VISIBLE
            }
            is GradeResult.NotPassed -> {
                binding.tvFeedbackTitle.setText(R.string.word_practice_failed)
                binding.tvFeedback.text = result.feedback
                setGradeButtonLabel(R.string.word_practice_regrade)
            }
            is GradeResult.Unparsable -> {
                binding.tvFeedbackTitle.setText(R.string.word_practice_unparsable)
                binding.tvFeedback.text = result.raw
                setGradeButtonLabel(R.string.word_practice_retry_grade)
            }
        }
    }

    private fun setGradeButtonLabel(resId: Int) {
        gradeButtonLabel = getString(resId)
        binding.btnGrade.text = gradeButtonLabel
    }

    private fun setupStep2() {
        textToSpeech = TextToSpeech(requireContext()) { status ->
            ttsReady = status == TextToSpeech.SUCCESS
            if (ttsReady) textToSpeech?.language = Locale.US
        }
        binding.btnListen.setOnClickListener { playSample() }
        binding.micButton.setOnClickListener {
            if (micState == MicState.IDLE) onMicTapped()
        }
        binding.btnRetrySave.setOnClickListener { saveLearned() }
    }

    private fun showStep2() {
        binding.groupStep1.visibility = View.GONE
        binding.groupStep2.visibility = View.VISIBLE
        binding.tvStep.setText(R.string.word_practice_step2)
        binding.tvSampleText.text = sampleText()
        updateRoundLabel()
    }

    private fun sampleText(): String {
        val currentWord = word ?: return ""
        return currentWord.exampleEn.ifBlank { currentWord.word }
    }

    private fun updateRoundLabel() {
        binding.tvRound.text = getString(
            R.string.word_practice_round_format,
            (successfulRounds + 1).coerceAtMost(ROUNDS_REQUIRED)
        )
    }

    private fun playSample() {
        val engine = textToSpeech
        val target = sampleText()
        if (engine == null || !ttsReady || target.isBlank()) {
            Toast.makeText(requireContext(), R.string.speaking_tts_unavailable, Toast.LENGTH_SHORT).show()
            return
        }
        engine.speak(target, TextToSpeech.QUEUE_FLUSH, null, "word_practice_sample")
    }

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

    private fun beginListening() {
        micState = MicState.LISTENING
        binding.tvRoundResult.visibility = View.GONE
        binding.tvMicHint.setText(R.string.speaking_listening_hint)

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
        if (_binding == null || micState != MicState.LISTENING) return
        micState = MicState.PROCESSING
        binding.micButton.visibility = View.INVISIBLE
        binding.micProcessingSpinner.visibility = View.VISIBLE
        binding.tvMicHint.setText(R.string.speaking_processing_hint)
    }

    private fun finishRound(isCorrect: Boolean) {
        speechRecognizer?.destroy()
        speechRecognizer = null
        if (_binding == null || micState == MicState.IDLE) return

        micState = MicState.IDLE
        binding.micProcessingSpinner.visibility = View.GONE
        binding.micButton.visibility = View.VISIBLE
        binding.tvMicHint.setText(R.string.speaking_idle_hint)
        binding.tvRoundResult.visibility = View.VISIBLE

        if (!isCorrect) {
            binding.tvRoundResult.setTextColor(ContextCompat.getColor(requireContext(), R.color.error))
            binding.tvRoundResult.setText(R.string.word_practice_round_fail)
            return
        }

        successfulRounds++
        binding.tvRoundResult.setTextColor(ContextCompat.getColor(requireContext(), R.color.success))
        binding.tvRoundResult.text = getString(R.string.word_practice_round_ok, successfulRounds, ROUNDS_REQUIRED)
        if (successfulRounds >= ROUNDS_REQUIRED) {
            completeExercise()
        } else {
            updateRoundLabel()
        }
    }

    private fun completeExercise() {
        binding.micButton.visibility = View.GONE
        binding.btnListen.visibility = View.GONE
        binding.tvMicHint.setText(R.string.word_practice_done)
        saveLearned()
    }

    private fun saveLearned() {
        val currentWord = word ?: return
        binding.btnRetrySave.visibility = View.GONE
        viewLifecycleOwner.lifecycleScope.launch {
            val result = ProgressRepositoryProvider.repository.setWordLearned(currentWord.id, true)
            if (_binding == null) return@launch
            if (result is ProgressResult.Loaded) {
                currentWord.isLearned = true
                findNavController().popBackStack()
            } else {
                Toast.makeText(requireContext(), R.string.progress_sync_error, Toast.LENGTH_SHORT).show()
                binding.btnRetrySave.visibility = View.VISIBLE
            }
        }
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

    private companion object {
        const val ROUNDS_REQUIRED = 2
    }
}
