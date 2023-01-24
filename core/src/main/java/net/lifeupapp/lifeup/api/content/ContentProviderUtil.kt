package net.lifeupapp.lifeup.api.content

import android.content.Context
import net.lifeupapp.lifeup.api.exception.NullCursorException

internal inline fun Context.forEachContent(
    url: String,
    onEachCursor: (cursor: android.database.Cursor) -> Unit
) {
    contentResolver.query(
        android.net.Uri.parse(url), null, null, null, null
    ).use {
        if (it == null) {
            throw NullCursorException()
        }

        it.moveToFirst()
        while (it.isAfterLast.not()) {
            onEachCursor(it)
            it.moveToNext()
        }
    }
}