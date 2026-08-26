package com.example.findphonebyclap.ui.main.view

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.SweepGradient
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
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
    private var iconDrawable = AppCompatResources.getDrawable(context, R.drawable.boomclap_btn_home_clap)

    // Animation values
    private var rippleFraction = 0f
    private var rotationAngle = 0f
    private var touchScale = 1.0f

    private var waveAnimator: ValueAnimator? = null
    private var rotationAnimator: ValueAnimator? = null

    // Paints
    private val wavePaint1 = Paint(Paint.ANTI_ALIAS_FLAG)
    private val wavePaint2 = Paint(Paint.ANTI_ALIAS_FLAG)
    private val wavePaint3 = Paint(Paint.ANTI_ALIAS_FLAG)
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val basePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val radarLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }

    init {
        isClickable = true
        isFocusable = true
        setupAnimators()
    }

    private fun setupAnimators() {
        waveAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 2200L
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener { animator ->
                rippleFraction = animator.animatedValue as Float
                invalidate()
            }
        }

        rotationAnimator = ValueAnimator.ofFloat(0f, 360f).apply {
            duration = 6000L
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener { animator ->
                rotationAngle = animator.animatedValue as Float
                if (isActive) invalidate()
            }
        }
    }

    fun setActiveState(active: Boolean) {
        if (isActive == active) return
        isActive = active
        if (isActive) {
            waveAnimator?.start()
            rotationAnimator?.start()
        } else {
            waveAnimator?.cancel()
            rotationAnimator?.cancel()
            rippleFraction = 0f
            rotationAngle = 0f
        }
        invalidate()
    }

    fun setIconResource(resId: Int) {
        iconDrawable = AppCompatResources.getDrawable(context, resId)
        invalidate()
    }

    fun isButtonActive(): Boolean = isActive

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                animateScale(0.93f)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                animateScale(1.0f)
            }
        }
        return super.onTouchEvent(event)
    }

    private fun animateScale(target: Float) {
        ValueAnimator.ofFloat(touchScale, target).apply {
            duration = 150L
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                touchScale = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val maxAvailableRadius = width.coerceAtMost(height) / 2f
        val baseRadius = maxAvailableRadius * 0.58f * touchScale

        // Draw animated concentric radar waves when active
        if (isActive) {
            val maxWaveRadius = maxAvailableRadius * 0.98f

            // Wave 1
            drawRippleWave(canvas, cx, cy, baseRadius, maxWaveRadius, rippleFraction, "#6366F1", "#00FF9D", wavePaint1)
            // Wave 2 (offset 0.33)
            val frac2 = (rippleFraction + 0.33f) % 1.0f
            drawRippleWave(canvas, cx, cy, baseRadius, maxWaveRadius, frac2, "#8B5CF6", "#00E5FF", wavePaint2)
            // Wave 3 (offset 0.66)
            val frac3 = (rippleFraction + 0.66f) % 1.0f
            drawRippleWave(canvas, cx, cy, baseRadius, maxWaveRadius, frac3, "#38BDF8", "#00FF9D", wavePaint3)

            // Outer ambient glow ring
            glowPaint.shader = RadialGradient(
                cx, cy, baseRadius * 1.35f,
                intArrayOf(
                    Color.argb(90, 99, 102, 241),
                    Color.argb(40, 0, 255, 157),
                    Color.TRANSPARENT
                ),
                floatArrayOf(0.6f, 0.85f, 1.0f),
                Shader.TileMode.CLAMP
            )
            canvas.drawCircle(cx, cy, baseRadius * 1.3f, glowPaint)
        } else {
            // Subtle ambient radar guide circles in inactive mode
            radarLinePaint.color = Color.parseColor("#162035")
            radarLinePaint.strokeWidth = 2f
            canvas.drawCircle(cx, cy, baseRadius * 1.25f, radarLinePaint)
            canvas.drawCircle(cx, cy, baseRadius * 1.5f, radarLinePaint)
        }

        // Draw outer glass halo ring
        val haloStroke = if (isActive) 6f else 3f
        ringPaint.strokeWidth = haloStroke
        ringPaint.color = if (isActive) Color.parseColor("#00FF9D") else Color.parseColor("#22304D")
        canvas.drawCircle(cx, cy, baseRadius + 6f, ringPaint)

        // Draw central orb with 3D spherical gradient
        val centerColors = if (isActive) {
            intArrayOf(
                Color.parseColor("#818CF8"),
                Color.parseColor("#6366F1"),
                Color.parseColor("#3730A3"),
                Color.parseColor("#1E1B4B")
            )
        } else {
            intArrayOf(
                Color.parseColor("#263554"),
                Color.parseColor("#19243C"),
                Color.parseColor("#11192B"),
                Color.parseColor("#0A0F1D")
            )
        }

        basePaint.shader = RadialGradient(
            cx - baseRadius * 0.25f,
            cy - baseRadius * 0.35f,
            baseRadius * 1.1f,
            centerColors,
            floatArrayOf(0.0f, 0.45f, 0.8f, 1.0f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(cx, cy, baseRadius, basePaint)

        // Draw inner top highlight crescent
        val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f
            shader = RadialGradient(
                cx, cy - baseRadius * 0.5f, baseRadius * 0.8f,
                intArrayOf(Color.argb(120, 255, 255, 255), Color.TRANSPARENT),
                floatArrayOf(0.0f, 1.0f),
                Shader.TileMode.CLAMP
            )
        }
        val highlightRect = RectF(cx - baseRadius + 4f, cy - baseRadius + 4f, cx + baseRadius - 4f, cy + baseRadius - 4f)
        canvas.drawArc(highlightRect, 200f, 140f, false, highlightPaint)

        // Draw rotating neon scanner tick when active
        if (isActive) {
            val scanPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 3.5f
                color = Color.parseColor("#00E5FF")
            }
            val sweepRect = RectF(cx - baseRadius + 2f, cy - baseRadius + 2f, cx + baseRadius - 2f, cy + baseRadius - 2f)
            canvas.drawArc(sweepRect, rotationAngle, 45f, false, scanPaint)
        }

        // Draw centered icon
        iconDrawable?.let { drawable ->
            val iconSize = (baseRadius * 1.05f).toInt()
            drawable.setBounds(
                (cx - iconSize / 2).toInt(),
                (cy - iconSize / 2).toInt(),
                (cx + iconSize / 2).toInt(),
                (cy + iconSize / 2).toInt()
            )
            drawable.draw(canvas)
        }
    }

    private fun drawRippleWave(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        minRadius: Float,
        maxRadius: Float,
        fraction: Float,
        colorStartHex: String,
        colorEndHex: String,
        paint: Paint
    ) {
        val currentRadius = minRadius + (maxRadius - minRadius) * fraction
        val alpha = ((1f - fraction) * 160).toInt().coerceIn(0, 255)
        val strokeW = 3f + (1f - fraction) * 5f

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeW
        paint.color = Color.parseColor(if (fraction > 0.5f) colorEndHex else colorStartHex)
        paint.alpha = alpha

        canvas.drawCircle(cx, cy, currentRadius, paint)
    }

    override fun onDetachedFromWindow() {
        waveAnimator?.cancel()
        rotationAnimator?.cancel()
        super.onDetachedFromWindow()
    }
}

