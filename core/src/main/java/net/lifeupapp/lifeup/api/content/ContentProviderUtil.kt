package net.lifeupapp.lifeup.api.content

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import net.lifeupapp.lifeup.api.exception.ContentProviderException
import net.lifeupapp.lifeup.api.exception.NullCursorException

@SuppressLint("Range")
internal inline fun Cursor.forEachRow(onEachCursor: (cursor: Cursor) -> Unit) {
    if (!moveToFirst()) {
        return
    }

    if (count == 1) {
        if (getColumnIndex("error_code") != -1 && getColumnIndex("error_message") != -1) {
            throw ContentProviderException(
                "content provider error_code: ${getString(getColumnIndex("error_code"))}, error_message: ${
                    getString(
                        getColumnIndex("error_message")
                    )
                }"
            )
        }
    }

    do {
        onEachCursor(this)
    } while (moveToNext())
}

@SuppressLint("Range")
internal inline fun Context.forEachContent(
    url: String,
    onEachCursor: (cursor: Cursor) -> Unit
) {
    contentResolver.query(
        android.net.Uri.parse(url), null, null, null, null
    ).use {
        if (it == null) {
            throw NullCursorException()
        }

        it.forEachRow(onEachCursor)
    }
}
