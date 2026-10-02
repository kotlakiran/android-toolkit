package com.kotlakiran.toolkit.reminders

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * WorkManager-backed reminder scheduling shared by apps. Data payloads are
 * keyed strings the app's own CoroutineWorker subclass reads back.
 */
object ReminderScheduler {

    /** Cancels any existing work for this tag, then schedules the next firing. */
    fun scheduleDailyAt(context: Context, tag: String, hour: Int, minute: Int, data: Map<String, String>) {
        schedule(context, tag, delayToNext(hour, minute), data)
    }

    /** Next firing in `intervalHours` from now. */
    fun scheduleInterval(context: Context, tag: String, intervalHours: Int, data: Map<String, String>) {
        schedule(context, tag, TimeUnit.HOURS.toMillis(intervalHours.toLong()), data)
    }

    fun cancel(context: Context, tag: String) {
        WorkManager.getInstance(context).cancelAllWorkByTag(tag)
    }

    /** WorkManager keeps one unique work per tag; the worker must reschedule itself. */
    private fun schedule(context: Context, tag: String, delayMs: Long, data: Map<String, String>) {
        cancel(context, tag)
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .addTag(tag)
            .setInputData(
                androidx.work.Data.Builder()
                    .putString(ReminderWorker.KEY_TAG, tag)
                    .apply { data.forEach { (k, v) -> putString(k, v) } }
                    .build()
            )
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(tag, ExistingWorkPolicy.REPLACE, request)
    }

    /** Milliseconds until the next occurrence of hour:minute today or tomorrow. */
    fun delayToNext(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val next = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (!after(now)) add(Calendar.DAY_OF_YEAR, 1)
        }
        return next.timeInMillis - now.timeInMillis
    }
}
