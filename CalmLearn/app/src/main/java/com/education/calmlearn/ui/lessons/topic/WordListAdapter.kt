package com.education.calmlearn.ui.lessons.topic

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.education.calmlearn.R
import com.education.calmlearn.data.model.VocabWord
import com.education.calmlearn.databinding.ItemWordRowBinding

class WordListAdapter(
    private val onWordClick: (VocabWord) -> Unit,
    private val onFavoriteClick: (VocabWord) -> Unit
) : RecyclerView.Adapter<WordListAdapter.WordViewHolder>() {

    private val words = mutableListOf<VocabWord>()

    fun submitList(newWords: List<VocabWord>) {
        words.clear()
        words.addAll(newWords)
        notifyDataSetChanged()
    }

    fun currentWords(): List<VocabWord> = words.toList()

    inner class WordViewHolder(val binding: ItemWordRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val binding = ItemWordRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        val word = words[position]
        holder.binding.wordText.text = word.word
        holder.binding.wordType.text = word.wordType
        holder.binding.wordPhonetic.text = word.phonetic
        holder.binding.wordMeaning.text = word.meaningVi
        holder.binding.wordFavorite.setImageResource(
            if (word.isFavorite) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
        )
        holder.binding.root.setOnClickListener { onWordClick(word) }
        holder.binding.wordFavorite.setOnClickListener { onFavoriteClick(word) }
    }

    override fun getItemCount(): Int = words.size
}
