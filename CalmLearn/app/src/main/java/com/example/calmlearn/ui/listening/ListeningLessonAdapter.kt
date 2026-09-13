package com.example.calmlearn.ui.listening

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.calmlearn.data.model.ListeningLesson
import com.example.calmlearn.databinding.ItemListeningLessonRowBinding

class ListeningLessonAdapter(
    private val lessons: List<ListeningLesson>,
    private val onClick: (ListeningLesson) -> Unit
) : RecyclerView.Adapter<ListeningLessonAdapter.LessonViewHolder>() {

    inner class LessonViewHolder(val binding: ItemListeningLessonRowBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonViewHolder {
        val binding = ItemListeningLessonRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LessonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LessonViewHolder, position: Int) {
        val lesson = lessons[position]
        holder.binding.lessonTitle.text = lesson.title
        holder.binding.lessonLevel.text = lesson.levelTag
        val minutes = lesson.durationSeconds / 60
        val seconds = lesson.durationSeconds % 60
        holder.binding.lessonDuration.text = String.format("%d:%02d", minutes, seconds)
        holder.binding.root.setOnClickListener { onClick(lesson) }
    }

    override fun getItemCount(): Int = lessons.size
}
