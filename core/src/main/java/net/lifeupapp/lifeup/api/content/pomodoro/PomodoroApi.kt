package net.lifeupapp.lifeup.api.content.pomodoro

import android.content.Context
import android.net.Uri
import net.lifeupapp.lifeup.api.content.ContentProviderApi
import net.lifeupapp.lifeup.api.content.ContentProviderUrl
import net.lifeupapp.lifeup.api.content.forEachContent
import net.lifeupapp.lifeup.api.utils.getDoubleOrNull
import net.lifeupapp.lifeup.api.utils.getLongOrNull

class PomodoroApi(private val context: Context) : ContentProviderApi {

    fun listRecords(
        offset: Int = 0,
        limit: Int = 100,
        timeRangeStart: Long? = null,
        timeRangeEnd: Long? = null
    ): Result<List<PomodoroRecord>> {
        val records = mutableListOf<PomodoroRecord>()
        try {
            val uri = Uri.parse(ContentProviderUrl.POMODORO_RECORDS).buildUpon()
                .appendQueryParameter("offset", offset.toString())
                .appendQueryParameter("limit", limit.toString())
                .apply {
                    if (timeRangeStart != null && timeRangeEnd != null && timeRangeStart < timeRangeEnd) {
                        appendQueryParameter("time_range_start", timeRangeStart.toString())
                        appendQueryParameter("time_range_end", timeRangeEnd.toString())
                    }
                }
                .build()

            context.forEachContent(uri.toString()) {
                val id = it.getLongOrNull("_ID")
                val startTime = it.getLongOrNull("startTime")
                val endTime = it.getLongOrNull("endTime")
                val duration = it.getLongOrNull("duration")
                val reward = it.getDoubleOrNull("reward")

                records.add(
                    PomodoroRecord.builder {
                        setId(id)
                        setStartTime(startTime)
                        setEndTime(endTime)
                        setDuration(duration ?: 0L)
                        setReward(reward ?: 0.0)
                    }
                )
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }

        return Result.success(records)
    }
}
