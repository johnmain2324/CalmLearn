package com.example.calmlearn.ui.lessons.topic

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.calmlearn.R
import com.example.calmlearn.data.mock.MockData
import com.example.calmlearn.data.model.VocabWord
import com.example.calmlearn.databinding.FragmentTopicDetailBinding

class TopicDetailFragment : Fragment() {

    private var _binding: FragmentTopicDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var topicId: String
    private lateinit var adapter: WordListAdapter
    private var allWords: List<VocabWord> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTopicDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        topicId = requireArguments().getString("topicId") ?: MockData.topics.first().id
        val topic = MockData.topics.firstOrNull { it.id == topicId } ?: MockData.topics.first()
        allWords = MockData.wordsForTopic(topicId)

        binding.topicTitle.text = topic.title
        binding.topicProgress.text =
            getString(R.string.topic_words_learned_format, topic.learnedWords, topic.totalWords)

        adapter = WordListAdapter(
            onWordClick = { word ->
                findNavController().navigate(R.id.action_global_wordDetail, bundleOf("wordId" to word.id))
            },
            onFavoriteClick = { word ->
                word.isFavorite = !word.isFavorite
                adapter.submitList(currentFilteredWords())
            }
        )
        binding.wordList.layoutManager = LinearLayoutManager(requireContext())
        binding.wordList.adapter = adapter
        adapter.submitList(allWords)

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        binding.btnOpenFlashcards.setOnClickListener {
            findNavController().navigate(R.id.action_global_flashcard, bundleOf("topicId" to topicId))
        }

        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.submitList(currentFilteredWords(s?.toString().orEmpty()))
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun currentFilteredWords(query: String = binding.searchInput.text.toString()): List<VocabWord> {
        if (query.isBlank()) return allWords
        val lower = query.trim().lowercase()
        return allWords.filter {
            it.word.lowercase().contains(lower) || it.meaningVi.lowercase().contains(lower)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
