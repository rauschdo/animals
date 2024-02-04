package de.rauschdo.animals.custom

import android.animation.ArgbEvaluator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Property
import android.view.View
import androidx.core.content.ContextCompat
import de.rauschdo.animals.R
import de.rauschdo.animals.utility.Utils.clamp
import de.rauschdo.animals.utility.Utils.mapValueFromRangeToRange

class CircleView : View {
    private val argbEvaluator = ArgbEvaluator()
    private val circlePaint = Paint()
    private val maskPaint = Paint()

    private lateinit var tempBitmap: Bitmap
    private lateinit var tempCanvas: Canvas

    private val START_COLOR = ContextCompat.getColor(context, R.color.circle_start)
    private val END_COLOR = ContextCompat.getColor(context, R.color.circle_end)

    var outerCircleRadiusProgress = 0f
    var innerCircleRadiusProgress = 0f
    var maxCircleSize = 0

    companion object {
        val INNER_CIRCLE_RADIUS_PROGRESS: Property<CircleView, Float> =
            object : Property<CircleView, Float>(
                Float::class.java, "innerCircleRadiusProgress"
            ) {
                override fun get(`object`: CircleView): Float {
                    return `object`.getInnerCircleProgress()
                }

                override fun set(`object`: CircleView, value: Float) {
                    `object`.setInnerCircleProgress(value)
                }
            }
        val OUTER_CIRCLE_RADIUS_PROGRESS: Property<CircleView, Float> =
            object : Property<CircleView, Float>(
                Float::class.java, "outerCircleRadiusProgress"
            ) {
                override fun get(`object`: CircleView): Float {
                    return `object`.getOuterCircleProgress()
                }

                override fun set(`object`: CircleView, value: Float) {
                    `object`.setOuterCircleProgress(value)
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
        circlePaint.style = Paint.Style.FILL
        maskPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        maxCircleSize = w / 2
        tempBitmap = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888)
        tempCanvas = Canvas(tempBitmap)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        tempCanvas.drawColor(0xffffff, PorterDuff.Mode.CLEAR)
        tempCanvas.drawCircle(
            (width / 2).toFloat(),
            (height / 2).toFloat(),
            outerCircleRadiusProgress * maxCircleSize,
            circlePaint
        )
        tempCanvas.drawCircle(
            (width / 2).toFloat(),
            (height / 2).toFloat(),
            innerCircleRadiusProgress * maxCircleSize,
            maskPaint
        )
        canvas.drawBitmap(tempBitmap, 0f, 0f, null)
    }

    private fun updateCircleColor() {
        var colorProgress = clamp(outerCircleRadiusProgress.toDouble(), 0.5, 1.0)
            .toFloat()
        colorProgress = mapValueFromRangeToRange(
            colorProgress.toDouble(),
            0.5,
            1.0,
            0.0,
            1.0
        ).toFloat()
        circlePaint.color = (argbEvaluator.evaluate(colorProgress, START_COLOR, END_COLOR) as Int)
    }

    fun getOuterCircleProgress(): Float {
        return outerCircleRadiusProgress
    }

    fun setOuterCircleProgress(outerCircleRadiusProgress: Float) {
        this.outerCircleRadiusProgress = outerCircleRadiusProgress
        updateCircleColor()
        postInvalidate()
    }

    fun getInnerCircleProgress(): Float {
        return innerCircleRadiusProgress
    }

    fun setInnerCircleProgress(innerCircleRadiusProgress: Float) {
        this.innerCircleRadiusProgress = innerCircleRadiusProgress
        postInvalidate()
    }
}