package com.example.calmlearn.ui.speaking

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.calmlearn.R
import com.example.calmlearn.data.mock.MockData
import com.example.calmlearn.data.model.SpeakingItem
import com.example.calmlearn.data.model.SpeakingLevel
import com.example.calmlearn.databinding.FragmentSpeakingBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlin.random.Random

private enum class MicState { IDLE, LISTENING, PROCESSING, RESULT }

class SpeakingFragment : Fragment() {

    private var _binding: FragmentSpeakingBinding? = null
    private val binding get() = _binding!!

    private val handler = Handler(Looper.getMainLooper())
    private var currentLevel = SpeakingLevel.WORD
    private var items: List<SpeakingItem> = emptyList()
    private var currentIndex = 0
    private var micState = MicState.IDLE

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

        binding.btnPlaySample.setOnClickListener { /* simulated sample playback, no audio in prototype */ }

        binding.micButton.setOnClickListener {
            if (micState == MicState.IDLE) startListening()
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

    private fun startListening() {
        micState = MicState.LISTENING
        binding.micButton.setBackgroundResource(R.drawable.bg_circle_teal)
        binding.micHint.setText(R.string.speaking_listening_hint)
        binding.resultGroup.visibility = View.GONE
        handler.postDelayed({ startProcessing() }, 1500)
    }

    private fun startProcessing() {
        if (_binding == null) return
        micState = MicState.PROCESSING
        binding.micButton.setBackgroundResource(R.drawable.bg_circle_amber)
        binding.micButton.visibility = View.INVISIBLE
        binding.micProcessingSpinner.visibility = View.VISIBLE
        binding.micHint.setText(R.string.speaking_processing_hint)
        handler.postDelayed({ showResult() }, 1000)
    }

    private fun showResult() {
        if (_binding == null) return
        micState = MicState.RESULT
        binding.micProcessingSpinner.visibility = View.GONE
        binding.micButton.visibility = View.VISIBLE
        binding.micButton.setBackgroundResource(R.drawable.bg_circle_coral)

        val isCorrect = Random.nextInt(10) < 7
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

    private fun resetToIdle() {
        micState = MicState.IDLE
        handler.removeCallbacksAndMessages(null)
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
        }
    }

    private fun showPracticeContent(visible: Boolean = true) {
        binding.practiceContentGroup.visibility = if (visible) View.VISIBLE else View.GONE
        if (visible) binding.completionGroup.visibility = View.GONE
    }

    override fun onDestroyView() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroyView()
        _binding = null
    }
}
