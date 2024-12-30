package net.lifeupapp.lifeup.api.utils

import android.database.Cursor


fun Cursor.getLongOrNull(columnName: String): Long? {
    val index = getColumnIndex(columnName)
    if (index == -1) {
        return null
    }
    return if (isNull(index)) null else getLong(index)
}

fun Cursor.getStringOrNull(columnName: String): String? {
    val index = getColumnIndex(columnName)
    if (index == -1) {
        return null
    }
    return if (isNull(index)) null else getString(index)
}

fun Cursor.getIntOrNull(columnName: String): Int? {
    val index = getColumnIndex(columnName)
    if (index == -1) {
        return null
    }
    return if (isNull(index)) null else getInt(index)
}


fun Cursor.getFloatOrNull(columnName: String): Float? {
    val index = getColumnIndex(columnName)
    if (index == -1) {
        return null
    }
    return if (isNull(index)) null else getFloat(index)
}

fun Cursor.getDoubleOrNull(columnName: String): Double? {
    val index = getColumnIndex(columnName)
    if (index == -1) {
        return null
    }
    return if (isNull(index)) null else getDouble(index)
}