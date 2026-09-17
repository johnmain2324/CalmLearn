package com.education.calmlearn.ui.achievement

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.education.calmlearn.data.mock.MockData
import com.education.calmlearn.databinding.ActivityAchievementListBinding

class AchievementListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAchievementListBinding
    private lateinit var adapter: AchievementAdapter

    private val detailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val viewedId = result.data?.getStringExtra(AchievementDetailActivity.EXTRA_VIEWED_ACHIEVEMENT_ID)
            if (viewedId != null) {
                adapter.markSeen(viewedId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAchievementListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        adapter = AchievementAdapter(MockData.achievements) { achievement ->
            val intent = Intent(this, AchievementDetailActivity::class.java).apply {
                putExtra(AchievementDetailActivity.EXTRA_ACHIEVEMENT_ID, achievement.id)
                putExtra(AchievementDetailActivity.EXTRA_ACHIEVEMENT_TITLE, achievement.title)
                putExtra(AchievementDetailActivity.EXTRA_ACHIEVEMENT_DESCRIPTION, achievement.description)
                putExtra(AchievementDetailActivity.EXTRA_ACHIEVEMENT_ICON_RES, achievement.iconRes)
                putExtra(AchievementDetailActivity.EXTRA_ACHIEVEMENT_UNLOCKED, achievement.isUnlocked)
                putExtra(AchievementDetailActivity.EXTRA_PROGRESS_CURRENT, achievement.progressCurrent)
                putExtra(AchievementDetailActivity.EXTRA_PROGRESS_TOTAL, achievement.progressTotal)
            }
            detailLauncher.launch(intent)
        }

        binding.recyclerAchievements.layoutManager = LinearLayoutManager(this)
        binding.recyclerAchievements.adapter = adapter
    }
}
