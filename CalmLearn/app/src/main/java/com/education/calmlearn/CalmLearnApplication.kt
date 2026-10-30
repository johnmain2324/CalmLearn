package com.education.calmlearn

import android.app.Application
import com.education.calmlearn.data.reminder.ReminderNotifier

class CalmLearnApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        ReminderNotifier.createChannel(this)
    }
}
