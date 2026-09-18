package com.education.calmlearn.ui.lessons

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.education.calmlearn.R
import com.education.calmlearn.data.progress.ProgressRepositoryProvider
import com.education.calmlearn.data.progress.ProgressResult
import com.education.calmlearn.data.vocab.VocabRepositoryProvider
import com.education.calmlearn.databinding.FragmentLessonsBinding
import kotlinx.coroutines.launch

class LessonsFragment : Fragment() {

    private var _binding: FragmentLessonsBinding? = null
    private val binding get() = _binding!!

    /** 4 view dong tren 1 the chu de (xem item_lesson_*.xml) - gan gia tri THAT
     *  tu SQLite (tong so tu) + Firestore (so tu da hoc) trong [refreshTopicStats]. */
    private class TopicCardViews(
        val wordCount: TextView,
        val progressBar: ProgressBar,
        val learnedCount: TextView,
        val percent: TextView
    )

    private lateinit var topicCards: List<Pair<String, TopicCardViews>>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLessonsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        topicCards = listOf(
            "travel" to TopicCardViews(binding.itemTravel.tvWordCount, binding.itemTravel.progressBar, binding.itemTravel.tvLearnedCount, binding.itemTravel.tvPercent),
            "daily" to TopicCardViews(binding.itemDaily.tvWordCount, binding.itemDaily.progressBar, binding.itemDaily.tvLearnedCount, binding.itemDaily.tvPercent),
            "food" to TopicCardViews(binding.itemFood.tvWordCount, binding.itemFood.progressBar, binding.itemFood.tvLearnedCount, binding.itemFood.tvPercent),
            "tech" to TopicCardViews(binding.itemTech.tvWordCount, binding.itemTech.progressBar, binding.itemTech.tvLearnedCount, binding.itemTech.tvPercent),
            "school" to TopicCardViews(binding.itemSchool.tvWordCount, binding.itemSchool.progressBar, binding.itemSchool.tvLearnedCount, binding.itemSchool.tvPercent),
            "work" to TopicCardViews(binding.itemWork.tvWordCount, binding.itemWork.progressBar, binding.itemWork.tvLearnedCount, binding.itemWork.tvPercent),
            "social" to TopicCardViews(binding.itemSocial.tvWordCount, binding.itemSocial.progressBar, binding.itemSocial.tvLearnedCount, binding.itemSocial.tvPercent),
            "movie" to TopicCardViews(binding.itemMovie.tvWordCount, binding.itemMovie.progressBar, binding.itemMovie.tvLearnedCount, binding.itemMovie.tvPercent)
        )

        binding.tabVocab.setOnClickListener { showVocabTab() }
        binding.tabGrammar.setOnClickListener { showGrammarTab() }

        binding.btnFlashcards.setOnClickListener { openFlashcards("travel") }
        binding.rowVocabList.setOnClickListener { findNavController().navigate(R.id.action_global_vocabList) }

        binding.itemTravel.root.setOnClickListener { openTopic("travel") }
        binding.itemDaily.root.setOnClickListener { openTopic("daily") }
        binding.itemFood.root.setOnClickListener { openTopic("food") }
        binding.itemTech.root.setOnClickListener { openTopic("tech") }
        binding.itemSchool.root.setOnClickListener { openTopic("school") }
        binding.itemWork.root.setOnClickListener { openTopic("work") }
        binding.itemSocial.root.setOnClickListener { openTopic("social") }
        binding.itemMovie.root.setOnClickListener { openTopic("movie") }

        binding.itemPresentSimple.root.setOnClickListener { openGrammar("present_simple") }
        binding.itemPresentContinuous.root.setOnClickListener { openGrammar("present_continuous") }
        binding.itemPastSimple.root.setOnClickListener { openGrammar("past_simple") }
        binding.itemFutureSimple.root.setOnClickListener { openGrammar("future_simple") }
        binding.itemComparison.root.setOnClickListener { openGrammar("comparison") }
        binding.itemGerund.root.setOnClickListener { openGrammar("gerund") }
    }

    /** Goi lai moi lan quay lai tab nay (khong chi luc tao view lan dau) vi nguoi dung co the
     *  vua danh dau hoc xong tu o man khac (Hoc tu/Flashcard) roi quay lai - so lieu phai la
     *  THAT tai thoi diem hien tai, khong duoc dung so cu. */
    override fun onResume() {
        super.onResume()
        refreshTopicStats()
    }

    /** Tong so tu = COUNT that tu bang words trong vocab.db theo topic_id (qua VocabRepository,
     *  tu chay tren Dispatchers.IO - xem SqliteVocabRepository). So da hoc = so id trong
     *  learnedWordIds (Firestore, qua ProgressRepository) thuoc ve dung chu de do. Neu tai khoan
     *  chua hoc tu nao trong chu de thi hien dung 0/tong, 0% - khong bia so khac 0. */
    private fun refreshTopicStats() {
        viewLifecycleOwner.lifecycleScope.launch {
            val progressResult = ProgressRepositoryProvider.repository.loadProgress()
            val learnedIds = (progressResult as? ProgressResult.Loaded)?.progress?.learnedWordIds ?: emptySet()

            topicCards.forEach { (topicId, views) ->
                val words = VocabRepositoryProvider.repository.getWordsByTopic(topicId)
                val total = words.size
                val learned = words.count { learnedIds.contains(it.id) }
                val percent = if (total > 0) Math.round(learned * 100f / total) else 0

                views.wordCount.text = getString(R.string.lessons_topic_word_count_format, total)
                views.progressBar.progress = percent
                views.learnedCount.text = getString(R.string.achievement_progress_format, learned, total)
                views.percent.text = getString(R.string.lessons_topic_percent_format, percent)
            }
        }
    }

    private fun openTopic(topicId: String) {
        findNavController().navigate(R.id.action_global_topicDetail, bundleOf("topicId" to topicId))
    }

    private fun openFlashcards(topicId: String) {
        findNavController().navigate(R.id.action_global_flashcard, bundleOf("topicId" to topicId))
    }

    private fun openGrammar(grammarId: String) {
        findNavController().navigate(R.id.action_global_grammarDetail, bundleOf("grammarId" to grammarId))
    }

    private fun showVocabTab() {
        binding.vocabContainer.visibility = View.VISIBLE
        binding.grammarContainer.visibility = View.GONE

        binding.tabVocab.setBackgroundResource(R.drawable.bg_pill_teal)
        binding.tabVocab.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

        binding.tabGrammar.background = null
        binding.tabGrammar.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
    }

    private fun showGrammarTab() {
        binding.grammarContainer.visibility = View.VISIBLE
        binding.vocabContainer.visibility = View.GONE

        binding.tabGrammar.setBackgroundResource(R.drawable.bg_pill_teal)
        binding.tabGrammar.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

        binding.tabVocab.background = null
        binding.tabVocab.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
