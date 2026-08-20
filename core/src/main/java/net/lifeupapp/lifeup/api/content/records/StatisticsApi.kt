package net.lifeupapp.lifeup.api.content.records

import android.content.Context
import android.net.Uri
import net.lifeupapp.lifeup.api.content.ContentProviderApi
import net.lifeupapp.lifeup.api.content.ContentProviderUrl
import net.lifeupapp.lifeup.api.content.forEachContent
import net.lifeupapp.lifeup.api.utils.getDoubleOrNull
import net.lifeupapp.lifeup.api.utils.getIntOrNull
import net.lifeupapp.lifeup.api.utils.getLongOrNull

class StatisticsApi(private val context: Context) : ContentProviderApi {

    fun getStatistics(timeRangeStart: Long? = null, timeRangeEnd: Long? = null): Result<Statistics> {
        return try {
            val uri = Uri.parse(ContentProviderUrl.STATISTICS).buildUpon().apply {
                if (timeRangeStart != null && timeRangeEnd != null && timeRangeStart < timeRangeEnd) {
                    appendQueryParameter("time_range_start", timeRangeStart.toString())
                    appendQueryParameter("time_range_end", timeRangeEnd.toString())
                }
            }.build()
            var found: Statistics? = null
            context.forEachContent(uri.toString()) {
                found = Statistics(
                    start = it.getLongOrNull("start"),
                    end = it.getLongOrNull("end"),
                    tomatoes = it.getIntOrNull("tomatoes") ?: 0,
                    pomodoroMinutes = it.getIntOrNull("pomodoroMinutes") ?: 0,
                    focusCount = it.getIntOrNull("focusCount") ?: 0,
                    tasksCompleted = it.getIntOrNull("tasksCompleted") ?: 0,
                    tasksTotal = it.getIntOrNull("tasksTotal") ?: 0,
                    taskCompleteRate = it.getDoubleOrNull("taskCompleteRate") ?: 0.0,
                    feelingsCount = it.getIntOrNull("feelingsCount") ?: 0,
                    usingDays = it.getIntOrNull("usingDays") ?: 0,
                    companionDays = it.getIntOrNull("companionDays") ?: 0,
                    interestChanged = it.getLongOrNull("interestChanged") ?: 0L,
                    coinPurchaseConsumed = it.getLongOrNull("coinPurchaseConsumed") ?: 0L,
                )
            }
            found?.let { Result.success(it) } ?: Result.failure(IllegalAccessException())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
