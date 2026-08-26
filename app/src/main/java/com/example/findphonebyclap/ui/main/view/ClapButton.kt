package com.example.findphonebyclap.ui.main.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.example.findphonebyclap.R

class ClapButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var isActive = false
    private var rippleRadius1 = 0f
    private var rippleRadius2 = 0f
    private var rippleAlpha1 = 0
    private var rippleAlpha2 = 0
    private var rippleAnimator: ValueAnimator? = null

    private var iconDrawable = AppCompatResources.getDrawable(context, R.drawable.boomclap_btn_home_clap)

    private val basePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ripplePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        isClickable = true
        isFocusable = true
        setupAnimation()
    }

    private fun setupAnimation() {
        rippleAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 2000L
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener { animator ->
                val fraction = animator.animatedValue as Float
                val maxRadius = width.coerceAtLeast(height) / 2f
                val innerRadius = maxRadius * 0.55f

                rippleRadius1 = innerRadius + (maxRadius - innerRadius) * fraction
                rippleAlpha1 = ((1f - fraction) * 120).toInt().coerceIn(0, 255)

                val fraction2 = (fraction + 0.5f) % 1.0f
                rippleRadius2 = innerRadius + (maxRadius - innerRadius) * fraction2
                rippleAlpha2 = ((1f - fraction2) * 120).toInt().coerceIn(0, 255)

                invalidate()
            }
        }
    }

    fun setActiveState(active: Boolean) {
        if (isActive == active) return
        isActive = active
        if (isActive) {
            rippleAnimator?.start()
        } else {
            rippleAnimator?.cancel()
        }
        invalidate()
    }

    fun setIconResource(resId: Int) {
        iconDrawable = AppCompatResources.getDrawable(context, resId)
        invalidate()
    }

    fun isButtonActive(): Boolean = isActive

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val radius = width.coerceAtMost(height) / 2f * 0.65f

        // Draw animated ripples when active
        if (isActive) {
            ripplePaint.color = Color.parseColor("#7B5CFF")
            ripplePaint.alpha = rippleAlpha1
            canvas.drawCircle(cx, cy, rippleRadius1, ripplePaint)

            ripplePaint.color = Color.parseColor("#00FF9D")
            ripplePaint.alpha = rippleAlpha2
            canvas.drawCircle(cx, cy, rippleRadius2, ripplePaint)
        }

        // Draw outer glow circle
        if (isActive) {
            glowPaint.shader = RadialGradient(
                cx, cy, radius * 1.35f,
                intArrayOf(Color.parseColor("#9A82FF"), Color.parseColor("#7B5CFF"), Color.TRANSPARENT),
                floatArrayOf(0.5f, 0.85f, 1.0f),
                Shader.TileMode.CLAMP
            )
            canvas.drawCircle(cx, cy, radius * 1.3f, glowPaint)
        }

        // Draw central button
        val centerColors = if (isActive) {
            intArrayOf(Color.parseColor("#9D7BFF"), Color.parseColor("#5A32E8"))
        } else {
            intArrayOf(Color.parseColor("#2B1C6B"), Color.parseColor("#150B3D"))
        }

        basePaint.shader = RadialGradient(
            cx, cy - radius * 0.3f, radius,
            centerColors,
            floatArrayOf(0.0f, 1.0f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(cx, cy, radius, basePaint)

        // Draw border ring
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 6f
            color = if (isActive) Color.parseColor("#00FF9D") else Color.parseColor("#443388")
        }
        canvas.drawCircle(cx, cy, radius, borderPaint)

        // Draw icon in center
        iconDrawable?.let { drawable ->
            val iconSize = (radius * 1.05f).toInt()
            drawable.setBounds(
                (cx - iconSize / 2).toInt(),
                (cy - iconSize / 2).toInt(),
                (cx + iconSize / 2).toInt(),
                (cy + iconSize / 2).toInt()
            )
            drawable.draw(canvas)
        }
    }

    override fun onDetachedFromWindow() {
        rippleAnimator?.cancel()
        super.onDetachedFromWindow()
    }
}
