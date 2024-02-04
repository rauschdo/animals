@file:JvmName("PixelDPConverter")

package de.rauschdo.animals.utility

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue

val DENSITY: Float = Resources.getSystem().displayMetrics.density

fun Int.dpToPx(): Int = (this * DENSITY).toInt()

val Int.dpToPx: Int
    get() = this.dpToPx()

fun Int.pxToDp(): Int = (this / DENSITY).toInt()

val Int.pxToDp: Int
    get() = this.pxToDp()


//

fun getPxForDp(context: Context, sizeInDP: Int): Int {
    return TypedValue
        .applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, sizeInDP.toFloat(), context.resources.displayMetrics
        )
        .toInt()
}

fun pxToSp(context: Context, sizeInPX: Int): Float {
    return TypedValue
        .applyDimension(
            TypedValue.COMPLEX_UNIT_SP, sizeInPX.toFloat(), context.resources.displayMetrics
        )
}