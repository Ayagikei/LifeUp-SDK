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

    private var animator: ObjectAnimator? = null

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
        getAnimator().start()
    }

    override fun onDraw(canvas: Canvas?) {
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

    private fun getAnimator(): ObjectAnimator {
        if (animator == null) {
            animator = ObjectAnimator.ofFloat(
                this,
                "floatYFraction",
                0f,
                1f
            )
            animator?.duration = 5000
            animator?.repeatCount = -1 //-1 means infinite loop
        }
        return animator!!
    }

    fun addRect(rect: RectF) {
        showLine = false
        resultRect = rect
        getAnimator().cancel()
        invalidate()
    }


}