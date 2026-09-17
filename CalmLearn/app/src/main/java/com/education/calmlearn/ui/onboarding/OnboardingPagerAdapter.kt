package com.education.calmlearn.ui.onboarding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.education.calmlearn.data.model.OnboardingPage
import com.education.calmlearn.databinding.ItemOnboardingPageBinding

class OnboardingPagerAdapter(
    private val pages: List<OnboardingPage>
) : RecyclerView.Adapter<OnboardingPagerAdapter.PageViewHolder>() {

    inner class PageViewHolder(val binding: ItemOnboardingPageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val binding = ItemOnboardingPageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        val page = pages[position]
        holder.binding.pageIcon.setImageResource(page.iconRes)
        holder.binding.pageTitle.text = page.title
        holder.binding.pageDescription.text = page.description
    }

    override fun getItemCount(): Int = pages.size
}
