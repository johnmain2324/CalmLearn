package com.example.calmlearn.ui.lessons

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.calmlearn.R
import com.example.calmlearn.databinding.FragmentLessonsBinding

class LessonsFragment : Fragment() {

    private var _binding: FragmentLessonsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLessonsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tabVocab.setOnClickListener { showVocabTab() }
        binding.tabGrammar.setOnClickListener { showGrammarTab() }

        binding.btnFlashcards.setOnClickListener { openFlashcards("travel") }

        binding.itemTravel.root.setOnClickListener { openTopic("travel") }
        binding.itemDaily.root.setOnClickListener { openTopic("daily") }
        binding.itemFood.root.setOnClickListener { openTopic("food") }
        binding.itemTech.root.setOnClickListener { openTopic("tech") }
        binding.itemSchool.root.setOnClickListener { openTopic("school") }
        binding.itemWork.root.setOnClickListener { openTopic("work") }
        binding.itemSocial.root.setOnClickListener { openTopic("social") }
        binding.itemMovie.root.setOnClickListener { openTopic("movie") }

        binding.itemPresentSimple.root.setOnClickListener { openGrammar("present_simple") }
        binding.itemPresentContinuous.root.setOnClickListener { openGrammar("present_continuous") }
        binding.itemPastSimple.root.setOnClickListener { openGrammar("past_simple") }
        binding.itemFutureSimple.root.setOnClickListener { openGrammar("future_simple") }
        binding.itemComparison.root.setOnClickListener { openGrammar("comparison") }
        binding.itemGerund.root.setOnClickListener { openGrammar("gerund") }
    }

    private fun openTopic(topicId: String) {
        findNavController().navigate(R.id.action_global_topicDetail, bundleOf("topicId" to topicId))
    }

    private fun openFlashcards(topicId: String) {
        findNavController().navigate(R.id.action_global_flashcard, bundleOf("topicId" to topicId))
    }

    private fun openGrammar(grammarId: String) {
        findNavController().navigate(R.id.action_global_grammarDetail, bundleOf("grammarId" to grammarId))
    }

    private fun showVocabTab() {
        binding.vocabContainer.visibility = View.VISIBLE
        binding.grammarContainer.visibility = View.GONE

        binding.tabVocab.setBackgroundResource(R.drawable.bg_pill_teal)
        binding.tabVocab.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

        binding.tabGrammar.background = null
        binding.tabGrammar.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
    }

    private fun showGrammarTab() {
        binding.grammarContainer.visibility = View.VISIBLE
        binding.vocabContainer.visibility = View.GONE

        binding.tabGrammar.setBackgroundResource(R.drawable.bg_pill_teal)
        binding.tabGrammar.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

        binding.tabVocab.background = null
        binding.tabVocab.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
