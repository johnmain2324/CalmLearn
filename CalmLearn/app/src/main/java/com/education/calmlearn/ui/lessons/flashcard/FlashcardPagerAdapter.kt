package com.education.calmlearn.ui.lessons.flashcard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.education.calmlearn.data.model.VocabWord
import com.education.calmlearn.databinding.ItemFlashcardPageBinding

class FlashcardPagerAdapter(
    private val words: List<VocabWord>
) : RecyclerView.Adapter<FlashcardPagerAdapter.CardViewHolder>() {

    inner class CardViewHolder(val binding: ItemFlashcardPageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = ItemFlashcardPageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val word = words[position]
        val binding = holder.binding

        binding.frontType.text = word.wordType
        binding.frontWord.text = word.word
        binding.frontPhonetic.text = word.phonetic

        binding.backMeaning.text = word.meaningVi
        binding.backExampleEn.text = word.exampleEn
        binding.backExampleVi.text = word.exampleVi
        binding.backSynonyms.text = word.synonyms.joinToString(", ")
        binding.backSynonyms.visibility = if (word.synonyms.isEmpty()) View.GONE else View.VISIBLE

        binding.cardFront.visibility = View.VISIBLE
        binding.cardBack.visibility = View.GONE

        val flip = View.OnClickListener {
            val showingFront = binding.cardFront.visibility == View.VISIBLE
            binding.cardFront.visibility = if (showingFront) View.GONE else View.VISIBLE
            binding.cardBack.visibility = if (showingFront) View.VISIBLE else View.GONE
        }
        binding.cardFront.setOnClickListener(flip)
        binding.cardBack.setOnClickListener(flip)
    }

    override fun getItemCount(): Int = words.size
}
