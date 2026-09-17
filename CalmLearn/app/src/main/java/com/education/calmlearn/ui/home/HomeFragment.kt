package com.education.calmlearn.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.education.calmlearn.R
import com.education.calmlearn.data.auth.AuthRepositoryProvider
import com.education.calmlearn.data.auth.ProfileResult
import com.education.calmlearn.databinding.FragmentHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
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
     * Hien ten that cua nguoi vua dang nhap (khong con hardcode "Alex Nguyen"/email mau - XML chi
     * con dung tools:text cho preview, khong con android:text). XP/streak/muc tieu/khoa hoc dat ve
     * trang thai "chua co du lieu" vi buoc nay chua trien khai theo doi tien do that - tranh gan
     * nham so lieu mau (440 XP, 65% muc tieu, 70% khoa hoc...) thanh thanh tich that cua nguoi moi.
     */
    private fun loadCurrentUser() {
        binding.tvUserName.text = getString(R.string.profile_name_loading)

        binding.tvStreakValue.text = getString(R.string.home_streak_value_empty)
        binding.tvXpValue.text = getString(R.string.home_xp_value_empty)
        binding.goalProgress.progress = 0
        binding.tvGoalPercent.text = getString(R.string.home_goal_percent_empty)
        binding.tvGoalMinutes.text = getString(R.string.home_goal_minutes_empty)
        binding.tvGoalRemaining.text = getString(R.string.home_goal_remaining_empty)
        binding.courseProgress.progress = 0
        binding.tvCoursePercent.text = getString(R.string.home_course_percent_empty)

        val requestedUid = FirebaseAuth.getInstance().currentUser?.uid
        viewLifecycleOwner.lifecycleScope.launch {
            when (val result = AuthRepositoryProvider.repository.currentUserProfile()) {
                is ProfileResult.Loaded -> {
                    // Doi chieu UID truoc khi cap nhat UI: neu phien da doi (vd dang xuat/dang
                    // nhap tai khoan khac ngay trong luc dang cho ket qua) thi bo qua ket qua cu.
                    if (result.profile.uid != requestedUid) return@launch
                    binding.tvUserName.text = result.profile.fullName.takeIf { it.isNotBlank() }
                        ?: result.profile.email.substringBefore('@').takeIf { it.isNotBlank() }
                        ?: getString(R.string.profile_name_fallback)
                }
                ProfileResult.Missing, ProfileResult.NotSignedIn -> {
                    binding.tvUserName.text = getString(R.string.profile_name_fallback)
                }
                is ProfileResult.ReadError -> {
                    // Loi doc tam thoi - khong fabricate ten, chi hien trang thai trung tinh.
                    binding.tvUserName.text = getString(R.string.profile_name_fallback)
                }
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
