package com.education.calmlearn.ui.lessons.topic

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.education.calmlearn.R
import com.education.calmlearn.data.mock.MockData
import com.education.calmlearn.data.model.VocabWord
import com.education.calmlearn.data.progress.ProgressRepositoryProvider
import com.education.calmlearn.data.progress.ProgressResult
import com.education.calmlearn.databinding.FragmentTopicDetailBinding
import com.education.calmlearn.ui.common.StudySessionTracker
import kotlinx.coroutines.launch

class TopicDetailFragment : Fragment() {

    private var _binding: FragmentTopicDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var topicId: String
    private lateinit var adapter: WordListAdapter
    private var allWords: List<VocabWord> = emptyList()
    private val studyTracker = StudySessionTracker()

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
                val newValue = !word.isFavorite
                word.isFavorite = newValue
                adapter.submitList(currentFilteredWords())
                viewLifecycleOwner.lifecycleScope.launch {
                    val result = ProgressRepositoryProvider.repository.setFavorite(word.id, newValue)
                    if (result is ProgressResult.ReadError) {
                        word.isFavorite = !newValue
                        adapter.submitList(currentFilteredWords())
                    }
                }
            }
        )
        binding.wordList.layoutManager = LinearLayoutManager(requireContext())
        binding.wordList.adapter = adapter
        adapter.submitList(allWords)
        hydrateProgress()

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

    /** Dong bo trang thai da hoc/yeu thich that tu Firestore, roi cap nhat lai tieu de "X/Y tu da
     *  hoc" bang so lieu THAT (khong con dung so tinh Topic.learnedWords co dinh trong MockData). */
    private fun hydrateProgress() {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = ProgressRepositoryProvider.repository.loadProgress()
            if (result is ProgressResult.Loaded) {
                MockData.applyProgress(result.progress)
                adapter.submitList(currentFilteredWords())
                val learnedCount = allWords.count { it.isLearned }
                binding.topicProgress.text =
                    getString(R.string.topic_words_learned_format, learnedCount, allWords.size)
            }
        }
    }

    private fun currentFilteredWords(query: String = binding.searchInput.text.toString()): List<VocabWord> {
        if (query.isBlank()) return allWords
        val lower = query.trim().lowercase()
        return allWords.filter {
            it.word.lowercase().contains(lower) || it.meaningVi.lowercase().contains(lower)
        }
    }

    override fun onResume() {
        super.onResume()
        studyTracker.start()
    }

    override fun onPause() {
        reportStudySeconds()
        super.onPause()
    }

    private fun reportStudySeconds() {
        val seconds = studyTracker.elapsedSecondsAndReset()
        if (seconds > 0) {
            lifecycleScope.launch { ProgressRepositoryProvider.repository.addStudySeconds(seconds) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
