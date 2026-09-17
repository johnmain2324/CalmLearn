package com.education.calmlearn.ui.achievement

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.education.calmlearn.R
import com.education.calmlearn.databinding.ActivityAchievementDetailBinding

class AchievementDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ACHIEVEMENT_ID = "EXTRA_ACHIEVEMENT_ID"
        const val EXTRA_ACHIEVEMENT_TITLE = "EXTRA_ACHIEVEMENT_TITLE"
        const val EXTRA_ACHIEVEMENT_DESCRIPTION = "EXTRA_ACHIEVEMENT_DESCRIPTION"
        const val EXTRA_ACHIEVEMENT_ICON_RES = "EXTRA_ACHIEVEMENT_ICON_RES"
        const val EXTRA_ACHIEVEMENT_UNLOCKED = "EXTRA_ACHIEVEMENT_UNLOCKED"
        const val EXTRA_PROGRESS_CURRENT = "EXTRA_PROGRESS_CURRENT"
        const val EXTRA_PROGRESS_TOTAL = "EXTRA_PROGRESS_TOTAL"
        const val EXTRA_VIEWED_ACHIEVEMENT_ID = "EXTRA_VIEWED_ACHIEVEMENT_ID"
    }

    private lateinit var binding: ActivityAchievementDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAchievementDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val achievementId = intent.getStringExtra(EXTRA_ACHIEVEMENT_ID)
        val title = intent.getStringExtra(EXTRA_ACHIEVEMENT_TITLE).orEmpty()
        val description = intent.getStringExtra(EXTRA_ACHIEVEMENT_DESCRIPTION).orEmpty()
        val iconRes = intent.getIntExtra(EXTRA_ACHIEVEMENT_ICON_RES, R.drawable.ic_medal)
        val isUnlocked = intent.getBooleanExtra(EXTRA_ACHIEVEMENT_UNLOCKED, false)
        val progressCurrent = intent.getIntExtra(EXTRA_PROGRESS_CURRENT, 0)
        val progressTotal = intent.getIntExtra(EXTRA_PROGRESS_TOTAL, 0)

        if (achievementId == null) {
            finish()
            return
        }

        binding.btnBack.setOnClickListener { finish() }

        binding.detailTitle.text = title
        binding.detailDescription.text = description
        binding.detailIcon.setImageResource(iconRes)
        binding.detailProgressText.text = getString(
            R.string.achievement_progress_format, progressCurrent, progressTotal
        )

        val percent = if (progressTotal > 0) (progressCurrent * 100 / progressTotal) else 0
        binding.detailProgressBar.progress = percent

        if (isUnlocked) {
            binding.detailIcon.setBackgroundResource(R.drawable.bg_icon_circle_amber_light)
            binding.detailIcon.setColorFilter(getColor(R.color.amber))
            binding.detailStatus.text = getString(R.string.achievement_status_unlocked)
            binding.detailStatus.setBackgroundResource(R.drawable.bg_pill_teal_light)
            binding.detailStatus.setTextColor(getColor(R.color.teal_primary))
            binding.detailProgressBar.progressDrawable =
                getDrawable(R.drawable.progress_teal)
        } else {
            binding.detailIcon.setBackgroundResource(R.drawable.bg_icon_circle_gray)
            binding.detailIcon.setColorFilter(getColor(R.color.text_hint))
            binding.detailStatus.text = getString(R.string.achievement_status_locked)
            binding.detailStatus.setBackgroundResource(R.drawable.bg_pill_light_gray)
            binding.detailStatus.setTextColor(getColor(R.color.text_hint))
            binding.detailProgressBar.progressDrawable =
                getDrawable(R.drawable.progress_amber)
        }

        binding.btnMarkViewed.setOnClickListener {
            val resultIntent = Intent().putExtra(EXTRA_VIEWED_ACHIEVEMENT_ID, achievementId)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
}
