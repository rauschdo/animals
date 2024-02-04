package de.rauschdo.animals.custom

import android.content.Context
import android.view.animation.Interpolator
import android.widget.Scroller

class AutoScroller : Scroller {

    private var scrollFactor = 1.0

    constructor(context: Context) : super(context)

    constructor(context: Context, interpolator: Interpolator) : super(context, interpolator)

    constructor(context: Context, interpolator: Interpolator, flywheel: Boolean) : super(
        context,
        interpolator,
        flywheel
    )

    override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int, duration: Int) {
        // Ignore received duration, use fixed one instead
        super.startScroll(startX, startY, dx, dy, (duration * scrollFactor).toInt())
    }

    fun setScrollDurationFactor(scrollFactor: Double) {
        this.scrollFactor = scrollFactor
    }
}