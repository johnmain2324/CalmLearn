package com.education.calmlearn.ui.lessons.word

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.education.calmlearn.R
import com.education.calmlearn.data.mock.MockData
import com.education.calmlearn.data.model.VocabWord
import com.education.calmlearn.databinding.FragmentWordDetailBinding

class WordDetailFragment : Fragment() {

    private var _binding: FragmentWordDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var word: VocabWord

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

        val wordId = requireArguments().getString("wordId") ?: MockData.vocabWords.first().id
        word = MockData.vocabWords.firstOrNull { it.id == wordId } ?: MockData.vocabWords.first()

        renderWord()

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        binding.btnFavorite.setOnClickListener {
            word.isFavorite = !word.isFavorite
            updateFavoriteIcon()
        }

        binding.btnPlaySample.setOnClickListener {
            Toast.makeText(requireContext(), R.string.action_play_sample, Toast.LENGTH_SHORT).show()
        }

        binding.btnMarkLearned.setOnClickListener {
            word.isLearned = !word.isLearned
            updateLearnedButton()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
