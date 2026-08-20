package net.lifeupapp.lifeup.api.content.records

import android.content.Context
import net.lifeupapp.lifeup.api.content.ContentProviderApi
import net.lifeupapp.lifeup.api.content.ContentProviderUrl
import net.lifeupapp.lifeup.api.content.forEachContent
import net.lifeupapp.lifeup.api.utils.getIntOrNull
import net.lifeupapp.lifeup.api.utils.getLongOrNull

class StepRecordsApi(private val context: Context) : ContentProviderApi {

    fun listRecords(
        offset: Int = 0,
        limit: Int = 100,
        timeRangeStart: Long? = null,
        timeRangeEnd: Long? = null,
    ): Result<List<StepRecord>> {
        val rows = mutableListOf<StepRecord>()
        return try {
            val uri = pagedLedgerUri(
                ContentProviderUrl.STEP_RECORDS, offset, limit, timeRangeStart, timeRangeEnd
            )
            context.forEachContent(uri.toString()) {
                rows.add(
                    StepRecord(
                        id = it.getLongOrNull("_ID"),
                        date = it.getLongOrNull("date"),
                        dailyStepCount = it.getLongOrNull("dailyStepCount") ?: 0L,
                        totalStepCount = it.getLongOrNull("totalStepCount") ?: 0L,
                        isGotReward = it.getIntOrNull("isGotReward") == 1,
                        isUserInput = it.getIntOrNull("isUserInput") == 1,
                        rewardLevel = it.getIntOrNull("rewardLevel"),
                    )
                )
            }
            Result.success(rows)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
