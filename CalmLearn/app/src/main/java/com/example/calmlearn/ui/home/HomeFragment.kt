package com.example.calmlearn.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.calmlearn.R
import com.example.calmlearn.databinding.FragmentHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnContinueCourse.setOnClickListener { openTopic("travel") }
        binding.itemTopicTravel.root.setOnClickListener { openTopic("travel") }
        binding.itemTopicDaily.root.setOnClickListener { openTopic("daily") }
        binding.itemTopicFood.root.setOnClickListener { openTopic("food") }

        binding.itemQuickVocab.root.setOnClickListener { openFlashcards("travel") }
        binding.itemQuickListening.root.setOnClickListener {
            findNavController().navigate(R.id.action_global_listeningList)
        }
        binding.itemQuickSpeaking.root.setOnClickListener {
            findNavController().navigate(R.id.action_global_speaking)
        }
        binding.itemQuickQuiz.root.setOnClickListener {
            findNavController().navigate(R.id.action_global_quiz)
        }
        binding.btnDailyChallenge.setOnClickListener {
            findNavController().navigate(R.id.action_global_speaking)
        }
        binding.btnRecentReview.setOnClickListener { openWord("travel_1") }

        binding.btnQuickPracticeSeeAll.setOnClickListener { switchTab(R.id.practiceFragment) }
        binding.btnWeekDetail.setOnClickListener { switchTab(R.id.progressFragment) }
        binding.btnBadgesViewAll.setOnClickListener { switchTab(R.id.progressFragment) }
    }

    private fun openTopic(topicId: String) {
        findNavController().navigate(R.id.action_global_topicDetail, bundleOf("topicId" to topicId))
    }

    private fun openFlashcards(topicId: String) {
        findNavController().navigate(R.id.action_global_flashcard, bundleOf("topicId" to topicId))
    }

    private fun openWord(wordId: String) {
        findNavController().navigate(R.id.action_global_wordDetail, bundleOf("wordId" to wordId))
    }

    private fun switchTab(itemId: Int) {
        requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)?.selectedItemId = itemId
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
