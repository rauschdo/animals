package de.rauschdo.animals.custom

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import de.rauschdo.animals.databinding.LayoutLikeButtonBinding

open class LikeButtonView : FrameLayout {

    private lateinit var binding: LayoutLikeButtonBinding

    private val DECCELERATE_INTERPOLATOR = DecelerateInterpolator()
    private val ACCELERATE_DECELERATE_INTERPOLATOR = AccelerateDecelerateInterpolator()
    private val OVERSHOOT_INTERPOLATOR = OvershootInterpolator(4f)
    private var animatorSet: AnimatorSet? = null

    val isChecked: Boolean
        get() = binding.ivStar.isChecked

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        LayoutLikeButtonBinding.inflate(
            LayoutInflater.from(context),
            this
        ).apply {
            binding = this
        }
    }

    fun init(initial: Boolean, listener: Listener) {
        with(binding) {
            ivStar.apply {
                isChecked = initial
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        hanldeAnimation()
                    }
                    listener.onCheckedChanged(isChecked)
                }
            }
            this@LikeButtonView.setOnClickListener {
                ivStar.toggle()
            }
        }
    }

    private fun hanldeAnimation() {
        if (animatorSet != null) {
            animatorSet?.cancel()
        }
        with(binding) {
            ivStar.apply {
                animate().cancel()
                scaleX = 0f
                scaleY = 0f
            }
            vCircle.apply {
                innerCircleRadiusProgress = 0f
                outerCircleRadiusProgress = 0f
            }
            vDotsView.mProgress = 0f

            val outerCircleAnimator =
                ObjectAnimator.ofFloat(
                    vCircle,
                    CircleView.OUTER_CIRCLE_RADIUS_PROGRESS,
                    0.1f,
                    1f
                ).apply {
                    duration = 250
                    interpolator = DECCELERATE_INTERPOLATOR
                }

            val innerCircleAnimator =
                ObjectAnimator.ofFloat(
                    vCircle,
                    CircleView.INNER_CIRCLE_RADIUS_PROGRESS,
                    0.1f,
                    1f
                ).apply {
                    duration = 200
                    startDelay = 200
                    interpolator = DECCELERATE_INTERPOLATOR
                }


            val starScaleYAnimator = ObjectAnimator.ofFloat(ivStar, SCALE_Y, 0.2f, 1f).apply {
                duration = 350
                startDelay = 250
                interpolator = OVERSHOOT_INTERPOLATOR
            }

            val starScaleXAnimator = ObjectAnimator.ofFloat(ivStar, SCALE_X, 0.2f, 1f).apply {
                duration = 350
                startDelay = 250
                interpolator = OVERSHOOT_INTERPOLATOR
            }

            val dotsAnimator =
                ObjectAnimator.ofFloat(vDotsView, DotsView.DOTS_PROGRESS, 0f, 1f).apply {
                    duration = 900
                    startDelay = 50
                    interpolator = ACCELERATE_DECELERATE_INTERPOLATOR
                }

            animatorSet = AnimatorSet().apply {
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationCancel(animation: Animator) {
                        vCircle.innerCircleRadiusProgress = 0f
                        vCircle.outerCircleRadiusProgress = 0f
                        vDotsView.mProgress = 0f
                        ivStar.scaleX = 1f
                        ivStar.scaleY = 1f
                    }
                })
            }.also {
                it.playTogether(
                    outerCircleAnimator,
                    innerCircleAnimator,
                    starScaleYAnimator,
                    starScaleXAnimator,
                    dotsAnimator
                )
                it.start()
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        with(binding) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    ivStar.animate().scaleX(0.7f).scaleY(0.7f).setDuration(150).interpolator =
                        DECCELERATE_INTERPOLATOR
                    isPressed = true
                }
                MotionEvent.ACTION_MOVE -> {
                    val x = event.x
                    val y = event.y
                    val isInside = x > 0 && x < width && y > 0 && y < height
                    if (isPressed != isInside) {
                        isPressed = isInside
                    }
                }
                MotionEvent.ACTION_UP -> {
                    ivStar.animate().scaleX(1f).scaleY(1f).interpolator =
                        DECCELERATE_INTERPOLATOR
                    if (isPressed) {
                        performClick()
                        isPressed = false
                    }
                }
                MotionEvent.ACTION_CANCEL -> {
                    ivStar.animate().scaleX(1f).scaleY(1f).interpolator = DECCELERATE_INTERPOLATOR
                    isPressed = false
                }
            }
        }
        return true
    }

    interface Listener {
        fun onCheckedChanged(isChecked: Boolean)
    }
}
