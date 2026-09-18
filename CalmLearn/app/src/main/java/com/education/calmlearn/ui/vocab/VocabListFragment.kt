package com.education.calmlearn.ui.vocab

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
import com.education.calmlearn.data.model.VocabWord
import com.education.calmlearn.data.progress.ProgressRepositoryProvider
import com.education.calmlearn.data.progress.ProgressResult
import com.education.calmlearn.data.vocab.VocabRepositoryProvider
import com.education.calmlearn.databinding.FragmentVocabListBinding
import com.education.calmlearn.ui.lessons.topic.WordListAdapter
import kotlinx.coroutines.launch

/**
 * Man "09. Danh sach tu" - toan bo tu vung (moi chu de + tu nguoi dung tu them qua
 * man "Them tu"), doc THAT tu SQLite qua [VocabRepositoryProvider] (khong con qua
 * MockData.vocabWords cache dung o cac man Hoc tu, vi man nay can luon hien THAT
 * SU MOI NHAT trong SQLite - vd ngay sau khi vua them 1 tu o man 13).
 */
class VocabListFragment : Fragment() {

    private var _binding: FragmentVocabListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: WordListAdapter
    private var learnedIds: Set<String> = emptySet()
    private var favoriteIds: Set<String> = emptySet()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVocabListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = WordListAdapter(
            onWordClick = { word ->
                findNavController().navigate(R.id.action_global_wordDetail, bundleOf("wordId" to word.id))
            },
            onFavoriteClick = { word -> toggleFavorite(word) }
        )
        binding.wordList.layoutManager = LinearLayoutManager(requireContext())
        binding.wordList.adapter = adapter

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        binding.btnAddWord.setOnClickListener { findNavController().navigate(R.id.action_global_addWord) }

        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                loadWords(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    /** Nap lai moi lan quay lai man nay (vd sau khi vua "Them tu" xong o man 13, hoac tien do
     *  Firestore vua doi o man khac) de danh sach luon phan anh dung SQLite hien tai. */
    override fun onResume() {
        super.onResume()
        viewLifecycleOwner.lifecycleScope.launch {
            val result = ProgressRepositoryProvider.repository.loadProgress()
            if (result is ProgressResult.Loaded) {
                learnedIds = result.progress.learnedWordIds
                favoriteIds = result.progress.favoriteWordIds
            }
            loadWords(binding.searchInput.text?.toString().orEmpty())
        }
    }

    private fun loadWords(query: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val repository = VocabRepositoryProvider.repository
            val words = if (query.isBlank()) repository.getAllWords() else repository.searchWords(query)
            words.forEach { word ->
                word.isLearned = learnedIds.contains(word.id)
                word.isFavorite = favoriteIds.contains(word.id)
            }
            render(words)
        }
    }

    private fun render(words: List<VocabWord>) {
        if (_binding == null) return
        adapter.submitList(words)
        binding.vocabListCount.text = getString(R.string.vocab_list_count_format, words.size)
        binding.emptyState.visibility = if (words.isEmpty()) View.VISIBLE else View.GONE
        binding.wordList.visibility = if (words.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun toggleFavorite(word: VocabWord) {
        val newValue = !word.isFavorite
        word.isFavorite = newValue
        favoriteIds = if (newValue) favoriteIds + word.id else favoriteIds - word.id
        adapter.submitList(adapter.currentWords())
        viewLifecycleOwner.lifecycleScope.launch {
            val result = ProgressRepositoryProvider.repository.setFavorite(word.id, newValue)
            if (result is ProgressResult.ReadError) {
                word.isFavorite = !newValue
                favoriteIds = if (!newValue) favoriteIds + word.id else favoriteIds - word.id
                adapter.submitList(adapter.currentWords())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
