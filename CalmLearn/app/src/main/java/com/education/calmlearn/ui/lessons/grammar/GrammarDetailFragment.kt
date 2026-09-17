package com.education.calmlearn.ui.lessons.grammar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.education.calmlearn.R
import com.education.calmlearn.data.mock.MockData
import com.education.calmlearn.data.model.GrammarLesson
import com.education.calmlearn.databinding.FragmentGrammarDetailBinding
import com.education.calmlearn.databinding.ItemGrammarExampleBinding
import com.education.calmlearn.ui.common.OptionsController

class GrammarDetailFragment : Fragment() {

    private var _binding: FragmentGrammarDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var lesson: GrammarLesson
    private lateinit var optionsController: OptionsController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGrammarDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val grammarId = requireArguments().getString("grammarId") ?: MockData.grammarLessons.first().id
        lesson = MockData.grammarLessons.firstOrNull { it.id == grammarId } ?: MockData.grammarLessons.first()

        binding.grammarLevelTag.text = lesson.levelTag
        binding.grammarTitle.text = lesson.title
        binding.grammarFormula.text = lesson.formula
        binding.grammarExplanation.text = lesson.explanation
        binding.grammarNote.text = lesson.note

        binding.examplesContainer.removeAllViews()
        lesson.examples.forEach { example ->
            val exampleBinding = ItemGrammarExampleBinding.inflate(
                LayoutInflater.from(requireContext()), binding.examplesContainer, false
            )
            exampleBinding.exampleEn.text = example.english
            exampleBinding.exampleVi.text = example.vietnamese
            binding.examplesContainer.addView(exampleBinding.root)
        }

        val quiz = lesson.quiz.firstOrNull()
        if (quiz != null) {
            binding.quizQuestion.text = quiz.question
            optionsController = OptionsController(binding.quizOptionsContainer)
            optionsController.bind(quiz.options) {
                binding.btnCheckQuiz.alpha = 1f
            }
            binding.btnCheckQuiz.setOnClickListener {
                val selected = optionsController.selectedIndex()
                if (selected != null) {
                    optionsController.showResult(quiz.correctIndex)
                    binding.quizExplanation.text = quiz.explanation
                    binding.quizExplanation.visibility = View.VISIBLE
                }
            }
        } else {
            binding.quizQuestion.visibility = View.GONE
            binding.btnCheckQuiz.visibility = View.GONE
        }

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
