package com.education.calmlearn.ui.progress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.education.calmlearn.R
import com.education.calmlearn.data.mock.MockData
import com.education.calmlearn.data.progress.ProgressRepositoryProvider
import com.education.calmlearn.data.progress.ProgressResult
import com.education.calmlearn.databinding.FragmentProgressBinding
import kotlinx.coroutines.launch

class ProgressFragment : Fragment() {

    private var _binding: FragmentProgressBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProgressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadProgress()
    }

    /** Thay toan bo 6 the thong ke tinh (mau) bang so lieu THAT, cung nguon LearningProgress ma
     *  Home dang dung (xem HomeFragment.loadProgress()) - khong tao them mot cach tinh tien do doc
     *  lap thu hai. */
    private fun loadProgress() {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = ProgressRepositoryProvider.repository.loadProgress()
            if (result is ProgressResult.Loaded) {
                binding.statStreak.statStreakValue.text =
                    getString(R.string.streak_value_format, result.progress.streak)
                binding.statWords.statWordsValue.text =
                    getString(R.string.words_learned_format, result.progress.learnedWordIds.size)
                binding.statQuiz.statQuizValue.text =
                    getString(R.string.quiz_accuracy_format, result.progress.lastQuizAccuracyPercent ?: 0)
                binding.statSpeaking.statSpeakingValue.text =
                    getString(R.string.speaking_sessions_format, result.progress.speakingSessionCount)
                val totalMinutes = result.progress.totalStudySeconds / 60
                binding.statTotal.statTotalValue.text =
                    getString(R.string.total_study_time_format, totalMinutes / 60, totalMinutes % 60)

                MockData.applyProgress(result.progress)
                binding.statLessons.statLessonsValue.text =
                    getString(R.string.lessons_completed_format, MockData.lessonsCompletedCount(result.progress))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
