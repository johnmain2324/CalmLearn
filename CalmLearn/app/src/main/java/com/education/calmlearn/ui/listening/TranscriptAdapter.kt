package com.education.calmlearn.ui.listening

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.education.calmlearn.data.model.TranscriptLine
import com.education.calmlearn.databinding.ItemTranscriptLineBinding

class TranscriptAdapter(private val lines: List<TranscriptLine>) :
    RecyclerView.Adapter<TranscriptAdapter.LineViewHolder>() {

    inner class LineViewHolder(val binding: ItemTranscriptLineBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LineViewHolder {
        val binding = ItemTranscriptLineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LineViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LineViewHolder, position: Int) {
        val line = lines[position]
        holder.binding.speakerName.text = line.speaker
        holder.binding.lineText.text = line.line
    }

    override fun getItemCount(): Int = lines.size
}
