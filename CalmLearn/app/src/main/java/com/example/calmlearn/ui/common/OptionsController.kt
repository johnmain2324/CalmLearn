package com.example.calmlearn.ui.common

import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.example.calmlearn.R
import com.example.calmlearn.databinding.ItemQuizOptionBinding

/**
 * Renders a list of selectable answer options inside [container] and manages
 * selected / correct / incorrect visual states. Shared by Quiz, Listening
 * comprehension questions and the grammar mini-quiz so the interaction stays
 * consistent everywhere a "pick one of N options" flow is needed.
 */
class OptionsController(private val container: LinearLayout) {

    private var selectedIndex: Int? = null
    private var isChecked = false
    private val optionBindings = mutableListOf<ItemQuizOptionBinding>()

    fun bind(options: List<String>, onSelectionChanged: () -> Unit) {
        container.removeAllViews()
        optionBindings.clear()
        selectedIndex = null
        isChecked = false

        val letters = listOf("A", "B", "C", "D", "E", "F")
        options.forEachIndexed { index, optionText ->
            val binding = ItemQuizOptionBinding.inflate(
                LayoutInflater.from(container.context), container, false
            )
            binding.optionLetter.text = letters.getOrElse(index) { (index + 1).toString() }
            binding.optionText.text = optionText
            binding.root.setOnClickListener {
                if (!isChecked) {
                    selectedIndex = index
                    refreshSelectionVisuals()
                    onSelectionChanged()
                }
            }
            container.addView(binding.root)
            optionBindings.add(binding)
        }
    }

    fun selectedIndex(): Int? = selectedIndex

    fun showResult(correctIndex: Int) {
        isChecked = true
        val context = container.context
        optionBindings.forEachIndexed { index, binding ->
            binding.optionResultIcon.visibility = android.view.View.VISIBLE
            when {
                index == correctIndex -> {
                    binding.root.setBackgroundResource(R.drawable.bg_option_correct)
                    binding.optionResultIcon.setImageResource(R.drawable.ic_check)
                    binding.optionResultIcon.setColorFilter(ContextCompat.getColor(context, R.color.success))
                }
                index == selectedIndex -> {
                    binding.root.setBackgroundResource(R.drawable.bg_option_incorrect)
                    binding.optionResultIcon.setImageResource(R.drawable.ic_close)
                    binding.optionResultIcon.setColorFilter(ContextCompat.getColor(context, R.color.error))
                }
                else -> {
                    binding.optionResultIcon.visibility = android.view.View.GONE
                    binding.root.setBackgroundResource(R.drawable.bg_card_stroke)
                }
            }
        }
    }

    private fun refreshSelectionVisuals() {
        optionBindings.forEachIndexed { index, binding ->
            binding.root.setBackgroundResource(
                if (index == selectedIndex) R.drawable.bg_option_selected else R.drawable.bg_card_stroke
            )
        }
    }
}
