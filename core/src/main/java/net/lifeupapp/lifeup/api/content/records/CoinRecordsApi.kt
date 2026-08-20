package net.lifeupapp.lifeup.api.content.records

import android.content.Context
import net.lifeupapp.lifeup.api.content.ContentProviderApi
import net.lifeupapp.lifeup.api.content.ContentProviderUrl
import net.lifeupapp.lifeup.api.content.forEachContent
import net.lifeupapp.lifeup.api.utils.getIntOrNull
import net.lifeupapp.lifeup.api.utils.getLongOrNull
import net.lifeupapp.lifeup.api.utils.getStringOrNull

class CoinRecordsApi(private val context: Context) : ContentProviderApi {

    fun listRecords(
        offset: Int = 0,
        limit: Int = 100,
        timeRangeStart: Long? = null,
        timeRangeEnd: Long? = null,
    ): Result<List<CoinRecord>> {
        val rows = mutableListOf<CoinRecord>()
        return try {
            val uri = pagedLedgerUri(
                ContentProviderUrl.COIN_RECORDS, offset, limit, timeRangeStart, timeRangeEnd
            )
            context.forEachContent(uri.toString()) {
                rows.add(
                    CoinRecord(
                        id = it.getLongOrNull("_ID"),
                        time = it.getLongOrNull("time"),
                        value = it.getLongOrNull("value") ?: 0L,
                        isDecrease = it.getIntOrNull("isDecrease") == 1,
                        totalValue = it.getLongOrNull("totalValue") ?: 0L,
                        resCode = it.getIntOrNull("resCode") ?: 0,
                        relatedId = it.getLongOrNull("relatedId"),
                        content = it.getStringOrNull("content"),
                        savingBalance = it.getLongOrNull("savingBalance"),
                    )
                )
            }
            Result.success(rows)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
