package ru.netology.nmedia.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.withStyledAttributes
import ru.netology.nmedia.R
import ru.netology.nmedia.util.AndroidUtils
import kotlin.math.min
import kotlin.random.Random

class StatsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : View(context, attrs, defStyleAttr, defStyleRes) {
    private var radius = 0F
    private var center = PointF(0F, 0F)
    private var oval = RectF(0F, 0F, 0F, 0F)

    private var lineWidth = AndroidUtils.dp(context, 5F).toFloat()
    private var fontSize = AndroidUtils.dp(context, 40F).toFloat()
    private var colors = emptyList<Int>()

    private var progress = 0F
    private var animator: ValueAnimator? = null


    init {
        context.withStyledAttributes(attrs, R.styleable.StatsView) {
            lineWidth = getDimension(R.styleable.StatsView_lineWidth, lineWidth)
            fontSize = getDimension(R.styleable.StatsView_fontSize, fontSize)
            val resId = getResourceId(R.styleable.StatsView_colors, 0)
            colors = resources.getIntArray(resId).toList()
        }
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = lineWidth
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = fontSize
    }

    var data: List<Float> = emptyList()
        set(value) {
            field = value
            update()
        }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = min(w, h) / 2F - lineWidth / 2
        center = PointF(w / 2F, h / 2F)
        oval = RectF(
            center.x - radius, center.y - radius,
            center.x + radius, center.y + radius,
        )
    }

    override fun onDraw(canvas: Canvas) {
        if (data.isEmpty() || progress == 0F) {
            return
        }

        var startFrom = -90F + (progress * 360)
        val sum = data.sum()

        for ((index, datum) in data.withIndex()) {
            val angle = 360F * datum / sum
            paint.color = colors.getOrNull(index) ?: randomColor()
            canvas.drawArc(oval, startFrom, angle * progress, false, paint)
            startFrom += angle
        }

        canvas.drawText(
            "%.2f%%".format(data.sum() / data.sum() * 100),
            center.x,
            center.y + textPaint.textSize / 4,
            textPaint,
        )
    }

    private fun update() {
        animator?.let {
            it.removeAllListeners()
            it.cancel()
        }
        progress = 0F

        animator = ValueAnimator.ofFloat(0F, 1F).apply {
            addUpdateListener {
                interpolator = LinearInterpolator()
                duration = 2_000
                progress = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    private fun randomColor() = Random.nextInt(0xFF000000.toInt(), 0xFFFFFFFF.toInt())
}