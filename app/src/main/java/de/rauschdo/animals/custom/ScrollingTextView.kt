package de.rauschdo.animals.custom

import android.content.Context
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import android.widget.Scroller
import androidx.appcompat.widget.AppCompatTextView

/**
 * A TextView that scrolls it contents across the screen, in a similar fashion as movie credits roll
 * across the theater screen.
 */
@Suppress("unused")
class ScrollingTextView : AppCompatTextView, Runnable {

    private var mScroller: Scroller? = null

    private var speed = DEFAULT_SPEED
    private var isContinuousScrolling = true

    companion object {
        //Note: Higher is slower
        private const val DEFAULT_SPEED = 25.0f
    }

    constructor(context: Context) : super(context) {
        setup(context)
    }

    constructor(context: Context, attributes: AttributeSet?) : super(context, attributes) {
        setup(context)
    }

    private fun setup(context: Context) {
        mScroller = Scroller(context, LinearInterpolator())
        setScroller(mScroller)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (mScroller?.isFinished == true) {
            scroll()
        }
    }

    private fun scroll() {
        val viewHeight = height
        val visibleHeight = viewHeight - paddingBottom - paddingTop
        val lineHeight = lineHeight
        val offset = -1 * visibleHeight
        val totallineHeight = lineCount * lineHeight
        val distance = totallineHeight + visibleHeight
        val duration = (distance * speed).toInt()
        if (totallineHeight > visibleHeight) {
            mScroller?.startScroll(0, offset, 0, distance, duration)
            if (isContinuousScrolling) {
                post(this)
            }
        }
    }

    override fun run() {
        if (mScroller?.isFinished == true) {
            scroll()
        } else {
            post(this)
        }
    }
}