package net.lifeupapp.lifeup.api.content.records

import android.content.Context
import kotlinx.serialization.decodeFromString
import net.lifeupapp.lifeup.api.content.ContentProviderApi
import net.lifeupapp.lifeup.api.content.ContentProviderUrl
import net.lifeupapp.lifeup.api.content.forEachContent
import net.lifeupapp.lifeup.api.utils.getIntOrNull
import net.lifeupapp.lifeup.api.utils.getLongOrNull
import net.lifeupapp.lifeup.api.utils.getStringOrNull
import net.lifeupapp.lifeup.api.utils.json

class ExpRecordsApi(private val context: Context) : ContentProviderApi {

    fun listRecords(
        offset: Int = 0,
        limit: Int = 100,
        timeRangeStart: Long? = null,
        timeRangeEnd: Long? = null,
    ): Result<List<ExpRecord>> {
        val rows = mutableListOf<ExpRecord>()
        return try {
            val uri = pagedLedgerUri(
                ContentProviderUrl.EXP_RECORDS, offset, limit, timeRangeStart, timeRangeEnd
            )
            context.forEachContent(uri.toString()) {
                rows.add(
                    ExpRecord(
                        id = it.getLongOrNull("_ID"),
                        time = it.getLongOrNull("time"),
                        value = it.getIntOrNull("value") ?: 0,
                        isDecrease = it.getIntOrNull("isDecrease") == 1,
                        totalValue = it.getIntOrNull("totalValue") ?: 0,
                        resCode = it.getIntOrNull("resCode") ?: 0,
                        relatedId = it.getLongOrNull("relatedId"),
                        content = it.getStringOrNull("content"),
                        skillIds = decodeLongs(it.getStringOrNull("skillIds")),
                        relatedAttributes = decodeStrings(it.getStringOrNull("relatedAttributes")),
                    )
                )
            }
            Result.success(rows)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun decodeLongs(raw: String?): List<Long> =
        runCatching { json.decodeFromString<List<Long>>(raw ?: "[]") }.getOrDefault(emptyList())

    private fun decodeStrings(raw: String?): List<String> =
        runCatching { json.decodeFromString<List<String>>(raw ?: "[]") }.getOrDefault(emptyList())
}
