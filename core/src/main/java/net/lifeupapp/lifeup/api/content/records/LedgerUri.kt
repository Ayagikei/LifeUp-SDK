package net.lifeupapp.lifeup.api.content.records

import android.net.Uri

internal fun pagedLedgerUri(
    base: String,
    offset: Int,
    limit: Int,
    timeRangeStart: Long?,
    timeRangeEnd: Long?,
): Uri {
    return Uri.parse(base).buildUpon()
        .appendQueryParameter("offset", offset.toString())
        .appendQueryParameter("limit", limit.toString())
        .apply {
            if (timeRangeStart != null && timeRangeEnd != null && timeRangeStart < timeRangeEnd) {
                appendQueryParameter("time_range_start", timeRangeStart.toString())
                appendQueryParameter("time_range_end", timeRangeEnd.toString())
            }
        }
        .build()
}
