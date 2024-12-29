package net.lifeupapp.lifeup.api.content

import android.annotation.SuppressLint
import android.content.Context
import net.lifeupapp.lifeup.api.exception.ContentProviderException
import net.lifeupapp.lifeup.api.exception.NullCursorException

@SuppressLint("Range")
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
        if (it.count == 1) {
            if (it.getColumnIndex("error_code") != -1 && it.getColumnIndex("error_message") != -1) {
                throw ContentProviderException(
                    "content provider error_code: ${it.getString(it.getColumnIndex("error_code"))}, error_message: ${
                        it.getString(
                            it.getColumnIndex("error_message")
                        )
                    }"
                )
            }
        }
        while (it.isAfterLast.not()) {
            onEachCursor(it)
            it.moveToNext()
        }
    }
}
