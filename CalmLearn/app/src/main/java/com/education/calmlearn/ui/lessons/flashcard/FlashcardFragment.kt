package com.education.calmlearn.ui.lessons.flashcard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.education.calmlearn.R
import com.education.calmlearn.data.mock.MockData
import com.education.calmlearn.data.model.VocabWord
import com.education.calmlearn.data.progress.ProgressRepositoryProvider
import com.education.calmlearn.data.progress.ProgressResult
import com.education.calmlearn.databinding.FragmentFlashcardBinding
import com.education.calmlearn.ui.common.StudySessionTracker
import kotlinx.coroutines.launch

class FlashcardFragment : Fragment() {

    private var _binding: FragmentFlashcardBinding? = null
    private val binding get() = _binding!!

    private lateinit var words: List<VocabWord>
    private val studyTracker = StudySessionTracker()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFlashcardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val topicId = requireArguments().getString("topicId") ?: MockData.topics.first().id
        words = MockData.wordsForTopic(topicId).ifEmpty { MockData.vocabWords }

        binding.flashcardPager.adapter = FlashcardPagerAdapter(words)
        binding.flashcardPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateProgress(position)
            }
        })
        updateProgress(0)

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        hydrateProgress()

        binding.btnKnowIt.setOnClickListener {
            val word = words[binding.flashcardPager.currentItem]
            word.isLearned = true
            persistLearned(word.id, true)
            goToNextCard()
        }
        binding.btnStillLearning.setOnClickListener {
            val word = words[binding.flashcardPager.currentItem]
            word.isLearned = false
            persistLearned(word.id, false)
            goToNextCard()
        }
        binding.btnRestart.setOnClickListener {
            binding.completionGroup.visibility = View.GONE
            binding.actionButtonsGroup.visibility = View.VISIBLE
            binding.flashcardPager.setCurrentItem(0, false)
            updateProgress(0)
        }
    }

    private fun goToNextCard() {
        val next = binding.flashcardPager.currentItem + 1
        if (next < words.size) {
            binding.flashcardPager.currentItem = next
        } else {
            binding.actionButtonsGroup.visibility = View.GONE
            binding.completionGroup.visibility = View.VISIBLE
        }
    }

    private fun hydrateProgress() {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = ProgressRepositoryProvider.repository.loadProgress()
            if (result is ProgressResult.Loaded) {
                MockData.applyProgress(result.progress)
            }
        }
    }

    private fun persistLearned(wordId: String, learned: Boolean) {
        viewLifecycleOwner.lifecycleScope.launch {
            ProgressRepositoryProvider.repository.setWordLearned(wordId, learned)
        }
    }

    private fun updateProgress(position: Int) {
        binding.flashcardProgress.text = getString(R.string.flashcard_progress_format, position + 1, words.size)
        binding.flashcardProgressBar.max = words.size
        binding.flashcardProgressBar.progress = position + 1
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
        super.onDestroyView()
        _binding = null
    }
}
