package net.lifeupapp.lifeup.http.qrcode

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.util.TypedValue

fun Float.toPx(): Int {
    val resources = Resources.getSystem()
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        resources.displayMetrics
    ).toInt()
}

fun isPortraitMode(context: Context): Boolean {
    val mConfiguration: Configuration = context.resources.configuration
    return mConfiguration.orientation == Configuration.ORIENTATION_PORTRAIT
}
