package com.example.splashscreen

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.graphics.withClip
import androidx.core.graphics.withSave
import kotlin.math.PI
import kotlin.math.sin

class BabyBottleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    // baby bottle animation
    private val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = dp(6f)
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }
    private val milkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#46A6FF")
        style = Paint.Style.FILL
    }

    private val bodyRect = RectF()
    private val fillClipPath = Path()
    private val wavePath = Path()

    private var progress = 0.05f
    private var phase = 0f
    private var animator: ValueAnimator? = null

    fun setProgress(p: Float) {
        progress = p.coerceIn(0f, 1f).coerceAtLeast(0.05f)
        invalidate()
    }

    fun start() {
        if (animator?.isRunning == true) return
        animator = ValueAnimator.ofFloat(0f, 2f * Math.PI.toFloat()).apply {
            duration = 2500
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                phase = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    fun stop() {
        animator?.cancel()
        animator = null
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stop()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()

        val padding = dp(16f)
        val neckWidth = w * 0.42f
        val rimWidth = neckWidth + dp(36f)
        val pacifierWidth = neckWidth * 0.55f

        val bodyTop = dp(70f)
        val bodyBottom = h - dp(28f)
        val bodyLeft = (w - neckWidth) * 0.5f - dp(24f)
        val bodyRight = w - bodyLeft

        // body rect with big corner radius
        bodyRect.set(bodyLeft, bodyTop + dp(24f), bodyRight, bodyBottom)
        val bodyRadius = dp(22f)

        // pacifier
        canvas.withSave {
            val pacifierTop = dp(14f)
            val pacifierHeight = dp(26f)
            val pacifierLeft = (w - pacifierWidth) * 0.5f
            val pacifierRight = pacifierLeft + pacifierWidth
            val pacOval = RectF(pacifierLeft, pacifierTop, pacifierRight, pacifierTop + pacifierHeight)
            drawRoundRect(pacOval, pacifierHeight, pacifierHeight, outlinePaint)
        }

        // ring
        val ringTop = dp(42f)
        val ringHeight = dp(20f)
        val ringLeft = (w - rimWidth) * 0.5f
        val ringRight = ringLeft + rimWidth
        val ringRect = RectF(ringLeft, ringTop, ringRight, ringTop + ringHeight)
        canvas.drawRoundRect(ringRect, dp(6f), dp(6f), outlinePaint)

        // short neck
        val neckLeft = (w - neckWidth) * 0.5f
        val neckRight = neckLeft + neckWidth
        val neckRect = RectF(neckLeft, ringRect.bottom, neckRight, ringRect.bottom + dp(22f))
        canvas.drawRect(neckRect, outlinePaint)

        // shoulder line
        canvas.drawLine(neckRect.left, neckRect.bottom + dp(6f), neckRect.right, neckRect.bottom + dp(6f), outlinePaint)

        // bottle body outline
        canvas.drawRoundRect(bodyRect, bodyRadius, bodyRadius, outlinePaint)

        // compute clipping path for the milk inside the body
        fillClipPath.reset()
        fillClipPath.addRoundRect(bodyRect, bodyRadius, bodyRadius, Path.Direction.CW)

        // build wavy surface
        val level = bodyRect.bottom - (bodyRect.height() * progress)
        wavePath.reset()
        val amp = dp(8f)
        val waveLen = dp(80f)
        var x = bodyRect.left
        wavePath.moveTo(x, level)
        while (x <= bodyRect.right) {
            val y = (level + amp * sin((x / waveLen + phase) * 2f * PI).toFloat())
            wavePath.lineTo(x, y)
            x += dp(2f)
        }
        wavePath.lineTo(bodyRect.right, bodyRect.bottom)
        wavePath.lineTo(bodyRect.left, bodyRect.bottom)
        wavePath.close()

        // draw milk inside the body only
        canvas.withClip(fillClipPath) {
            drawPath(wavePath, milkPaint)
        }
    }

    private fun dp(v: Float): Float = v * resources.displayMetrics.density
}
