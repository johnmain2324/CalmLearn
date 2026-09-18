package com.education.calmlearn.ui.lessons.word

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.education.calmlearn.R
import com.education.calmlearn.data.mock.MockData
import com.education.calmlearn.data.model.VocabWord
import com.education.calmlearn.data.progress.ProgressRepositoryProvider
import com.education.calmlearn.data.progress.ProgressResult
import com.education.calmlearn.databinding.FragmentWordDetailBinding
import com.education.calmlearn.ui.common.StudySessionTracker
import kotlinx.coroutines.launch

class WordDetailFragment : Fragment() {

    private var _binding: FragmentWordDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var word: VocabWord
    private val studyTracker = StudySessionTracker()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWordDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val wordId = requireArguments().getString("wordId")
        loadWordThenProgress(wordId)

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        binding.btnPlaySample.setOnClickListener {
            Toast.makeText(requireContext(), R.string.action_play_sample, Toast.LENGTH_SHORT).show()
        }
    }

    /** Nap tu vung tu SQLite (qua MockData.ensureVocabWordsLoaded - xem data/vocab/) TRUOC, roi
     *  moi dong bo tien do that tu Firestore - cac nut Yeu thich/Da thuoc chi duoc bat sau khi
     *  [word] da co gia tri that (tranh bam truoc khi nap xong gay loi lateinit). */
    private fun loadWordThenProgress(wordId: String?) {
        viewLifecycleOwner.lifecycleScope.launch {
            MockData.ensureVocabWordsLoaded()
            word = (wordId?.let { id -> MockData.vocabWords.firstOrNull { it.id == id } }
                ?: MockData.vocabWords.firstOrNull())
                ?: return@launch
            renderWord()

            binding.btnFavorite.setOnClickListener {
                val newValue = !word.isFavorite
                word.isFavorite = newValue
                updateFavoriteIcon()
                persistFavorite(newValue)
            }
            binding.btnMarkLearned.setOnClickListener {
                val newValue = !word.isLearned
                word.isLearned = newValue
                updateLearnedButton()
                persistLearned(newValue)
            }

            hydrateProgress()
        }
    }

    /** Dong bo lai trang thai da hoc/yeu thich that cua nguoi dung tu Firestore (vd sau khi mo
     *  lai app) - noi dung tu khong doi, chi hai co nay co the khac voi gia tri mac dinh ban dau. */
    private fun hydrateProgress() {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = ProgressRepositoryProvider.repository.loadProgress()
            if (result is ProgressResult.Loaded) {
                MockData.applyProgress(result.progress)
                renderWord()
            }
        }
    }

    private fun persistFavorite(favorite: Boolean) {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = ProgressRepositoryProvider.repository.setFavorite(word.id, favorite)
            if (result is ProgressResult.ReadError) {
                word.isFavorite = !favorite
                updateFavoriteIcon()
                Toast.makeText(requireContext(), R.string.progress_sync_error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun persistLearned(learned: Boolean) {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = ProgressRepositoryProvider.repository.setWordLearned(word.id, learned)
            if (result is ProgressResult.ReadError) {
                word.isLearned = !learned
                updateLearnedButton()
                Toast.makeText(requireContext(), R.string.progress_sync_error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun renderWord() {
        binding.wordText.text = word.word
        binding.wordPhonetic.text = word.phonetic
        binding.wordType.text = word.wordType
        binding.wordMeaning.text = word.meaningVi
        binding.wordDefinition.text = word.definitionEn
        binding.wordExampleEn.text = word.exampleEn
        binding.wordExampleVi.text = word.exampleVi

        updateFavoriteIcon()
        updateLearnedButton()

        binding.synonymsContainer.removeAllViews()
        binding.synonymsLabel.visibility = if (word.synonyms.isEmpty()) View.GONE else View.VISIBLE
        word.synonyms.forEach { synonym ->
            val chip = TextView(requireContext()).apply {
                text = synonym
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
                textSize = 12f
                setBackgroundResource(R.drawable.bg_pill_light_gray)
                setPadding(24, 12, 24, 12)
            }
            val params = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            params.marginEnd = 16
            chip.layoutParams = params
            binding.synonymsContainer.addView(chip)
        }
    }

    private fun updateFavoriteIcon() {
        binding.btnFavorite.setImageResource(
            if (word.isFavorite) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
        )
    }

    private fun updateLearnedButton() {
        binding.btnMarkLearned.text = if (word.isLearned) {
            getString(R.string.word_detail_learned)
        } else {
            getString(R.string.word_detail_mark_learned)
        }
        binding.btnMarkLearned.setBackgroundResource(
            if (word.isLearned) R.drawable.bg_pill_success_light else R.drawable.bg_pill_teal
        )
        binding.btnMarkLearned.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (word.isLearned) R.color.success else R.color.white
            )
        )
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
