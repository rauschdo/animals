package de.rauschdo.animals.utility

import com.google.android.material.transition.MaterialSharedAxis

object MotionUtil {

    fun createAxisTransition(
        startId: Int,
        endId: Int,
        axis: Int, //MaterialSharedAxis.X/Y/Z
        entering: Boolean,
    ) = MaterialSharedAxis(axis, entering).apply {
        // Add targets for this transition to explicitly run transitions only on these views. Without
        // targeting, a MaterialSharedAxis transition would be run for every view in the
        // Fragment's layout.
        addTarget(startId)
        addTarget(endId)
    }
}