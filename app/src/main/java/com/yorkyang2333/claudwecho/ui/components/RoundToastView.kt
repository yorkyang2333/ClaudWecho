package com.yorkyang2333.claudwecho.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.PI
import kotlin.math.min

class RoundToastView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val path = Path()
    private val arcRect = RectF()

    private var toastText: String = ""

    // Deep gray translucent background with hairline white border
    private val toastColor = 0xCC1A1A1A.toInt()
    private val borderColor = 0x33FFFFFF
    private val textColor = 0xFFFFFFFF.toInt()

    private val textSizeVal = dp(13).toFloat()
    private val bgThickness = dp(24).toFloat()
    private val borderWidth = dp(0.8f).toFloat()
    private val bottomMargin = dp(8).toFloat()

    private val minSweepAngle = 20f
    private val maxSweepAngle = 140f
    private val textPadding = dp(20).toFloat()

    init {
        borderPaint.style = Paint.Style.STROKE
        borderPaint.color = borderColor
        borderPaint.strokeWidth = bgThickness + (borderWidth * 2)
        borderPaint.strokeCap = Paint.Cap.ROUND

        bgPaint.style = Paint.Style.STROKE
        bgPaint.color = toastColor
        bgPaint.strokeWidth = bgThickness
        bgPaint.strokeCap = Paint.Cap.ROUND

        textPaint.color = textColor
        textPaint.textSize = textSizeVal
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.isFakeBoldText = true
        textPaint.letterSpacing = 0.05f
    }

    fun setText(text: CharSequence) {
        toastText = text.toString()
        if (width > 0 && height > 0) {
            updatePath(width.toFloat(), height.toFloat())
        }
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updatePath(w.toFloat(), h.toFloat())
    }

    private fun updatePath(w: Float, h: Float) {
        path.reset()
        if (toastText.isEmpty()) return

        val screenRadius = min(w, h) / 2f
        val totalThickness = bgThickness + (borderWidth * 2)

        val arcRadius = screenRadius - bottomMargin - (totalThickness / 2f)

        val cx = w / 2f
        val cy = h / 2f
        arcRect.set(
            cx - arcRadius,
            cy - arcRadius,
            cx + arcRadius,
            cy + arcRadius
        )

        val textWidth = textPaint.measureText(toastText)
        val perimeter = 2 * PI * arcRadius
        val targetArcLength = textWidth + textPadding
        var calculatedSweep = (targetArcLength / perimeter * 360).toFloat()
        calculatedSweep = calculatedSweep.coerceIn(minSweepAngle, maxSweepAngle)

        val sweepAngle = -calculatedSweep
        val startAngle = 90f - (sweepAngle / 2f)

        path.addArc(arcRect, startAngle, sweepAngle)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (toastText.isEmpty()) return

        canvas.drawPath(path, borderPaint)
        canvas.drawPath(path, bgPaint)

        val fontMetrics = textPaint.fontMetrics
        val textHeight = fontMetrics.descent - fontMetrics.ascent
        val vOffset = (textHeight / 2f) - fontMetrics.descent
        canvas.drawTextOnPath(toastText, path, 0f, vOffset, textPaint)
    }

    private fun dp(value: Int): Int {
        val density = resources.displayMetrics.density
        return (value * density + 0.5f).toInt()
    }

    private fun dp(value: Float): Int {
        val density = resources.displayMetrics.density
        return (value * density + 0.5f).toInt()
    }
}
