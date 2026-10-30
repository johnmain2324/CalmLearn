package com.education.calmlearn.data.reminder

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class ReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        ReminderNotifier.show(applicationContext)
        return Result.success()
    }
}
