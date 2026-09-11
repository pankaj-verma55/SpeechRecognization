package com.example.speechrecognisation.ui

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.min

class MicAnimationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var pulseProgress = 0f
    private var dotProgress = 0f

    private var isAnimating = false

    private val pulseAnimator = ValueAnimator.ofFloat(0f, 1f).apply {

        duration = 1200

        repeatCount = ValueAnimator.INFINITE

        interpolator = LinearInterpolator()

        addUpdateListener {

            pulseProgress = it.animatedValue as Float

            invalidate()
        }
    }

    private val dotAnimator = ValueAnimator.ofFloat(0f, 1f).apply {

        duration = 900

        repeatCount = ValueAnimator.INFINITE

        interpolator = LinearInterpolator()

        addUpdateListener {

            dotProgress = it.animatedValue as Float

            invalidate()
        }
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {

        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f

        /*
         * ------------------------------------------------
         * 1. PULSING CIRCLE
         * ------------------------------------------------
         */

        if (isAnimating) {

            val maxRadius = min(width, height) * 0.42f

            val radius =
                65f + (maxRadius - 65f) * pulseProgress

            val alpha =
                ((1f - pulseProgress) * 150).toInt()

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 5f
            paint.color = Color.RED
            paint.alpha = alpha

            canvas.drawCircle(
                centerX,
                centerY,
                radius,
                paint
            )
        }

        /*
         * ------------------------------------------------
         * 2. MICROPHONE BODY
         * ------------------------------------------------
         */

        paint.style = Paint.Style.FILL
        paint.color = Color.BLACK
        paint.alpha = 255

        val micWidth = 45f
        val micHeight = 85f

        val micLeft = centerX - micWidth / 2
        val micTop = centerY - 65f
        val micRight = centerX + micWidth / 2
        val micBottom = micTop + micHeight

        canvas.drawRoundRect(
            RectF(
                micLeft,
                micTop,
                micRight,
                micBottom
            ),
            micWidth / 2,
            micWidth / 2,
            paint
        )

        /*
         * ------------------------------------------------
         * 3. MICROPHONE U-SHAPE
         * ------------------------------------------------
         */

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 7f
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = Color.BLACK

        val holderRect = RectF(
            centerX - 48f,
            centerY - 55f,
            centerX + 48f,
            centerY + 45f
        )

        canvas.drawArc(
            holderRect,
            0f,
            180f,
            false,
            paint
        )

        /*
         * ------------------------------------------------
         * 4. VERTICAL STAND
         * ------------------------------------------------
         */

        canvas.drawLine(
            centerX,
            centerY + 45f,
            centerX,
            centerY + 70f,
            paint
        )

        /*
         * ------------------------------------------------
         * 5. BOTTOM STAND
         * ------------------------------------------------
         */

        canvas.drawLine(
            centerX - 30f,
            centerY + 70f,
            centerX + 30f,
            centerY + 70f,
            paint
        )

        /*
         * ------------------------------------------------
         * 6. THREE DOTS
         * ------------------------------------------------
         */

        drawDots(
            canvas,
            centerX,
            centerY + 105f
        )
    }

    private fun drawDots(
        canvas: Canvas,
        centerX: Float,
        centerY: Float
    ) {

        val dotRadius = 5f
        val spacing = 25f

        for (i in 0..2) {

            /*
             * Create phase difference between dots.
             */

            val phase =
                (dotProgress + i * 0.25f) % 1f

            /*
             * Convert 0..1 into a pulse.
             */

            val scale =
                if (phase < 0.5f) {
                    phase * 2f
                } else {
                    (1f - phase) * 2f
                }

            val radius =
                dotRadius + (dotRadius * scale)

            val x =
                centerX + (i - 1) * spacing

            paint.style = Paint.Style.FILL
            paint.color = Color.RED
            paint.alpha = 255

            canvas.drawCircle(
                x,
                centerY,
                radius,
                paint
            )
        }
    }

    fun startAnimation() {

        isAnimating = true

        if (!pulseAnimator.isRunning) {
            pulseAnimator.start()
        }

        if (!dotAnimator.isRunning) {
            dotAnimator.start()
        }

        invalidate()
    }

    fun stopAnimation() {

        isAnimating = false

        pulseAnimator.cancel()
        dotAnimator.cancel()

        pulseProgress = 0f
        dotProgress = 0f

        invalidate()
    }
}