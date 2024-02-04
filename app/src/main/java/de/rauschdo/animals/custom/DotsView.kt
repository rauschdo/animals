package de.rauschdo.animals.custom

import android.animation.ArgbEvaluator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Property
import android.view.View
import androidx.core.content.ContextCompat
import de.rauschdo.animals.R
import de.rauschdo.animals.utility.Utils.clamp
import de.rauschdo.animals.utility.Utils.mapValueFromRangeToRange

class DotsView : View {

    private val DOTS_COUNT = 7
    private val OUTER_DOTS_POSITION_ANGLE = 360 / DOTS_COUNT

    private val COLOR_1 = ContextCompat.getColor(context, R.color.dots_color_1)
    private val COLOR_2 = ContextCompat.getColor(context, R.color.dots_color_2)
    private val COLOR_3 = ContextCompat.getColor(context, R.color.dots_color_3)
    private val COLOR_4 = ContextCompat.getColor(context, R.color.dots_color_4)

    private val circlePaints = mutableListOf(
        Paint().apply {
            color = COLOR_1
            style = Paint.Style.FILL
        },
        Paint().apply {
            color = COLOR_2
            style = Paint.Style.FILL
        },
        Paint().apply {
            color = COLOR_3
            style = Paint.Style.FILL
        },
        Paint().apply {
            color = COLOR_4
            style = Paint.Style.FILL
        }
    )

    private val argbEvaluator = ArgbEvaluator()
    private var centerX = 0
    private var centerY = 0
    private var maxOuterDotsRadius = 0f
    private var maxInnerDotsRadius = 0f
    private var maxDotSize = 0f
    private var currentRadius1 = 0f
    private var currentDotSize1 = 0f
    private var currentDotSize2 = 0f
    private var currentRadius2 = 0f

    var mProgress = 0f

    companion object {
        // Own defined property
        val DOTS_PROGRESS: Property<DotsView, Float> = object : Property<DotsView, Float>(
            Float::class.java, "dotsProgress"
        ) {
            override fun get(`object`: DotsView): Float {
                return `object`.getCurrentProgress()
            }

            override fun set(`object`: DotsView, value: Float) {
                `object`.setCurrentProgress(value)
            }
        }
    }

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        circlePaints.forEachIndexed { index, _ ->
            circlePaints[index] = Paint()
            circlePaints[index].style = Paint.Style.FILL
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2
        centerY = h / 2
        maxDotSize = 20f
        maxOuterDotsRadius = w / 2 - maxDotSize * 2
        maxInnerDotsRadius = 0.8f * maxOuterDotsRadius
    }

    override fun onDraw(canvas: Canvas) {
        drawOuterDotsFrame(canvas)
        drawInnerDotsFrame(canvas)
    }

    private fun drawOuterDotsFrame(canvas: Canvas) {
        for (i in 0 until DOTS_COUNT) {
            val cX =
                (centerX + currentRadius1 * Math.cos(i * OUTER_DOTS_POSITION_ANGLE * Math.PI / 180)).toInt()
            val cY =
                (centerY + currentRadius1 * Math.sin(i * OUTER_DOTS_POSITION_ANGLE * Math.PI / 180)).toInt()
            canvas.drawCircle(
                cX.toFloat(),
                cY.toFloat(),
                currentDotSize1,
                circlePaints[i % circlePaints.size]
            )
        }
    }

    private fun drawInnerDotsFrame(canvas: Canvas) {
        for (i in 0 until DOTS_COUNT) {
            val cX =
                (centerX + currentRadius2 * Math.cos((i * OUTER_DOTS_POSITION_ANGLE - 10) * Math.PI / 180)).toInt()
            val cY =
                (centerY + currentRadius2 * Math.sin((i * OUTER_DOTS_POSITION_ANGLE - 10) * Math.PI / 180)).toInt()
            canvas.drawCircle(
                cX.toFloat(),
                cY.toFloat(),
                currentDotSize2,
                circlePaints[(i + 1) % circlePaints.size]
            )
        }
    }

    fun getCurrentProgress(): Float {
        return mProgress
    }

    fun setCurrentProgress(mProgress: Float) {
        this.mProgress = mProgress
        updateInnerDotsPosition()
        updateOuterDotsPosition()
        updateDotsPaints()
        updateDotsAlpha()
        postInvalidate()
    }

    private fun updateInnerDotsPosition() {
        if (mProgress < 0.3f) {
            currentRadius2 = mapValueFromRangeToRange(
                mProgress.toDouble(),
                0.0,
                0.3,
                0.0,
                maxInnerDotsRadius.toDouble()
            ).toFloat()
        } else {
            currentRadius2 = maxInnerDotsRadius
        }
        if (mProgress < 0.2) {
            currentDotSize2 = maxDotSize
        } else if (mProgress < 0.5) {
            currentDotSize2 = mapValueFromRangeToRange(
                mProgress.toDouble(),
                0.2,
                0.5,
                maxDotSize.toDouble(),
                0.3 * maxDotSize
            ).toFloat()
        } else {
            currentDotSize2 = mapValueFromRangeToRange(
                mProgress.toDouble(),
                0.5,
                1.0,
                (maxDotSize * 0.3f).toDouble(),
                0.0
            ).toFloat()
        }
    }

    private fun updateOuterDotsPosition() {
        if (mProgress < 0.3f) {
            currentRadius1 = mapValueFromRangeToRange(
                mProgress.toDouble(),
                0.0,
                0.3,
                0.0,
                (maxOuterDotsRadius * 0.8f).toDouble()
            ).toFloat()
        } else {
            currentRadius1 = mapValueFromRangeToRange(
                mProgress.toDouble(),
                0.3,
                1.0,
                (0.8f * maxOuterDotsRadius).toDouble(),
                maxOuterDotsRadius.toDouble()
            ).toFloat()
        }
        if (mProgress < 0.7) {
            currentDotSize1 = maxDotSize
        } else {
            currentDotSize1 = mapValueFromRangeToRange(
                mProgress.toDouble(),
                0.7,
                1.0,
                maxDotSize.toDouble(),
                0.0
            ).toFloat()
        }
    }

    private fun updateDotsPaints() {
        if (mProgress < 0.5f) {
            val progress = mapValueFromRangeToRange(
                mProgress.toDouble(),
                0.0,
                0.5,
                0.0,
                1.0
            ).toFloat()
            circlePaints[0].color = (argbEvaluator.evaluate(progress, COLOR_1, COLOR_2) as Int)
            circlePaints[1].color = (argbEvaluator.evaluate(progress, COLOR_2, COLOR_3) as Int)
            circlePaints[2].color = (argbEvaluator.evaluate(progress, COLOR_3, COLOR_4) as Int)
            circlePaints[3].color = (argbEvaluator.evaluate(progress, COLOR_4, COLOR_1) as Int)
        } else {
            val progress = mapValueFromRangeToRange(
                mProgress.toDouble(),
                0.5,
                1.0,
                0.0,
                1.0
            ).toFloat()
            circlePaints[0].color = (argbEvaluator.evaluate(progress, COLOR_2, COLOR_3) as Int)
            circlePaints[1].color = (argbEvaluator.evaluate(progress, COLOR_3, COLOR_4) as Int)
            circlePaints[2].color = (argbEvaluator.evaluate(progress, COLOR_4, COLOR_1) as Int)
            circlePaints[3].color = (argbEvaluator.evaluate(progress, COLOR_1, COLOR_2) as Int)
        }
    }

    private fun updateDotsAlpha() {
        val progress = clamp(mProgress.toDouble(), 0.6, 1.0)
            .toFloat()
        val alpha = mapValueFromRangeToRange(
            progress.toDouble(),
            0.6,
            1.0,
            255.0,
            0.0
        ).toInt()
        circlePaints[0].alpha = alpha
        circlePaints[1].alpha = alpha
        circlePaints[2].alpha = alpha
        circlePaints[3].alpha = alpha
    }
}