package net.lifeupapp.lifeup.http.qrcode

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import net.lifeupapp.lifeup.http.R

class ScanOverlay(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val animator: ObjectAnimator by lazy {
        ObjectAnimator.ofFloat(
            this,
            "floatYFraction",
            0f,
            1f
        ).apply {
            duration = 5000
            repeatCount = -1 // -1 means infinite loop
        }
    }

    private var bitmap: Bitmap

    private var resultRect: RectF? = null

    private var showLine = true

    private var floatYFraction = 0f
        set(value) {
            field = value
            invalidate()
        }

    init {
        paint.style = Paint.Style.FILL
        paint.color = ContextCompat.getColor(context, R.color.md_theme_light_secondary)
        paint.strokeWidth = 3f.toPx().toFloat()
        bitmap = BitmapFactory.decodeResource(resources, R.drawable.icon_scan_line)
        animator.start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (showLine) {
            canvas?.drawBitmap(bitmap, (width - bitmap.width) / 2f, height * floatYFraction, paint)
        }
        resultRect?.let { rect ->
            canvas?.drawCircle(
                rect.left + (rect.right - rect.left) / 2f,
                rect.top + (rect.bottom - rect.top) / 2f,
                10f.toPx().toFloat(),
                paint
            )
        }
    }

    fun addRect(rect: RectF) {
        showLine = false
        resultRect = rect
        animator.cancel()
        invalidate()
    }
}
