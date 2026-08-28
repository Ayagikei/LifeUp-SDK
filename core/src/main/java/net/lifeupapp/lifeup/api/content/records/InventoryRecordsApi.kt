package net.lifeupapp.lifeup.api.content.records

import android.content.Context
import net.lifeupapp.lifeup.api.content.ContentProviderApi
import net.lifeupapp.lifeup.api.content.ContentProviderUrl
import net.lifeupapp.lifeup.api.content.forEachContent
import net.lifeupapp.lifeup.api.utils.getIntOrNull
import net.lifeupapp.lifeup.api.utils.getLongOrNull
import net.lifeupapp.lifeup.api.utils.getStringOrNull

class InventoryRecordsApi(private val context: Context) : ContentProviderApi {

    fun listRecords(
        offset: Int = 0,
        limit: Int = 100,
        timeRangeStart: Long? = null,
        timeRangeEnd: Long? = null,
    ): Result<List<InventoryRecord>> {
        val rows = mutableListOf<InventoryRecord>()
        return try {
            val uri = pagedLedgerUri(
                ContentProviderUrl.INVENTORY_RECORDS, offset, limit, timeRangeStart, timeRangeEnd
            )
            context.forEachContent(uri.toString()) {
                rows.add(
                    InventoryRecord(
                        id = it.getLongOrNull("_ID"),
                        time = it.getLongOrNull("time"),
                        itemId = it.getLongOrNull("itemId"),
                        itemName = it.getStringOrNull("itemName"),
                        changeNumber = it.getIntOrNull("changeNumber") ?: 0,
                        isDecrease = it.getIntOrNull("isDecrease") == 1,
                        resCode = it.getIntOrNull("resCode") ?: 0,
                        desc = it.getStringOrNull("desc"),
                    )
                )
            }
            Result.success(rows)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
