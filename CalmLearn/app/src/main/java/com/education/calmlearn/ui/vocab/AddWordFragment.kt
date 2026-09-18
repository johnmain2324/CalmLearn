package com.education.calmlearn.ui.vocab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.education.calmlearn.R
import com.education.calmlearn.data.mock.MockData
import com.education.calmlearn.data.vocab.VocabInput
import com.education.calmlearn.data.vocab.VocabRepositoryProvider
import com.education.calmlearn.data.vocab.VocabResult
import com.education.calmlearn.databinding.FragmentAddWordBinding
import kotlinx.coroutines.launch

/**
 * Man "13. Them tu" - form nhap 1 tu moi, luu THAT vao SQLite (INSERT that qua
 * VocabRepository.insertUserWord(), khong phai mock/giu tam trong bo nho) - xem
 * SQLITE_VOCAB.md o goc project cho luong demo day du.
 */
class AddWordFragment : Fragment() {

    private var _binding: FragmentAddWordBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddWordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val topicTitles = MockData.topics.map { it.title }
        binding.spinnerTopic.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, topicTitles
        )

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        binding.btnSave.setOnClickListener { save() }
    }

    private fun save() {
        val word = binding.etWord.text.toString().trim()
        if (word.isEmpty()) {
            binding.tvWordError.visibility = View.VISIBLE
            return
        }
        binding.tvWordError.visibility = View.GONE

        val topicId = MockData.topics[binding.spinnerTopic.selectedItemPosition].id
        val pos = binding.etPos.text.toString().trim().ifEmpty { "n." }
        val meaningVi = binding.etMeaningVi.text.toString().trim()
        val definitionEn = binding.etDefinition.text.toString().trim()
        val exampleEn = binding.etExampleEn.text.toString().trim()
        val exampleVi = binding.etExampleVi.text.toString().trim()
        val synonyms = VocabInput.parseSynonyms(binding.etSynonyms.text.toString())
        val note = binding.etNote.text.toString().trim().ifEmpty { null }

        binding.btnSave.isEnabled = false
        viewLifecycleOwner.lifecycleScope.launch {
            val result = VocabRepositoryProvider.repository.insertUserWord(
                word = word,
                topicId = topicId,
                pos = pos,
                meaningVi = meaningVi,
                definitionEn = definitionEn,
                exampleEn = exampleEn,
                exampleVi = exampleVi,
                synonyms = synonyms,
                note = note
            )
            when (result) {
                is VocabResult.Inserted -> {
                    Toast.makeText(requireContext(), R.string.add_word_success, Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
                is VocabResult.Error -> {
                    binding.btnSave.isEnabled = true
                    Toast.makeText(requireContext(), R.string.add_word_error_generic, Toast.LENGTH_SHORT).show()
                }
                else -> Unit
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
