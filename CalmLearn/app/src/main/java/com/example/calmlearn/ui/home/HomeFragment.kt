package com.example.calmlearn.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.calmlearn.R
import com.example.calmlearn.data.auth.AuthRepositoryProvider
import com.example.calmlearn.databinding.FragmentHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

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

        loadCurrentUser()

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

    /**
     * Hien ten that cua nguoi vua dang nhap (khong con hardcode "Alex Nguyen"). XP/streak dat ve
     * trang thai "chua co thanh tich" vi buoc nay chua trien khai theo doi tien do that - tranh
     * gan nham so lieu mau (440 XP, 7 ngay...) cho tai khoan that moi tao.
     */
    private fun loadCurrentUser() {
        binding.tvStreakValue.text = getString(R.string.home_streak_value_empty)
        binding.tvXpValue.text = getString(R.string.home_xp_value_empty)

        viewLifecycleOwner.lifecycleScope.launch {
            val profile = AuthRepositoryProvider.repository.currentUserProfile()
            if (profile != null && profile.fullName.isNotBlank()) {
                binding.tvUserName.text = profile.fullName
            }
        }
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
