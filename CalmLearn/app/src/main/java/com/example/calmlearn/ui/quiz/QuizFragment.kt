package com.example.calmlearn.ui.quiz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.calmlearn.R
import com.example.calmlearn.data.mock.MockData
import com.example.calmlearn.data.model.QuizQuestion
import com.example.calmlearn.databinding.FragmentQuizBinding
import com.example.calmlearn.ui.common.OptionsController

class QuizFragment : Fragment() {

    private var _binding: FragmentQuizBinding? = null
    private val binding get() = _binding!!

    private val questions: List<QuizQuestion> = MockData.quizQuestions.take(5)
    private lateinit var optionsController: OptionsController

    private var currentIndex = 0
    private var correctCount = 0
    private var checked = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnClose.setOnClickListener { findNavController().popBackStack() }

        optionsController = OptionsController(binding.quizOptionsContainer)
        renderQuestion(0)

        binding.btnQuizAction.setOnClickListener { onActionClicked() }
        binding.btnRetry.setOnClickListener { restartQuiz() }
        binding.btnGoHome.setOnClickListener { findNavController().navigate(R.id.action_global_home) }
    }

    private fun renderQuestion(index: Int) {
        currentIndex = index
        checked = false
        val question = questions[index]

        binding.quizProgressBar.max = questions.size
        binding.quizProgressBar.progress = index + 1
        binding.quizProgressLabel.text = getString(R.string.quiz_question_format, index + 1, questions.size)
        binding.quizQuestionText.text = question.question
        binding.quizExplanation.visibility = View.GONE
        binding.btnQuizAction.setText(R.string.action_check)

        optionsController.bind(question.options) {}
    }

    private fun onActionClicked() {
        val question = questions[currentIndex]
        if (!checked) {
            val selected = optionsController.selectedIndex() ?: return
            if (selected == question.correctIndex) correctCount++
            optionsController.showResult(question.correctIndex)
            binding.quizExplanation.text = question.explanation
            binding.quizExplanation.visibility = View.VISIBLE
            checked = true
            val isLast = currentIndex == questions.lastIndex
            binding.btnQuizAction.setText(if (isLast) R.string.action_see_result else R.string.action_next_question)
        } else {
            val next = currentIndex + 1
            if (next < questions.size) {
                renderQuestion(next)
            } else {
                showResultScreen()
            }
        }
    }

    private fun showResultScreen() {
        binding.quizContentGroup.visibility = View.GONE
        binding.btnQuizAction.visibility = View.GONE
        binding.quizResultGroup.visibility = View.VISIBLE

        binding.resultScore.text = getString(R.string.quiz_result_score_format, correctCount, questions.size)
        val xp = correctCount * 10
        binding.resultXp.text = getString(R.string.quiz_result_xp_format, xp)

        binding.resultMessage.setText(
            when {
                correctCount == questions.size -> R.string.quiz_result_excellent
                correctCount >= (questions.size * 0.6).toInt() -> R.string.quiz_result_good
                else -> R.string.quiz_result_retry
            }
        )
    }

    private fun restartQuiz() {
        correctCount = 0
        binding.quizResultGroup.visibility = View.GONE
        binding.quizContentGroup.visibility = View.VISIBLE
        binding.btnQuizAction.visibility = View.VISIBLE
        renderQuestion(0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
