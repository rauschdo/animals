package de.rauschdo.animals.utility

object Utils {

    var isInitMain = false

    var isCurrentlyNightMode = false

    var firstEnterCategory = true

    fun mapValueFromRangeToRange(
        value: Double,
        fromLow: Double,
        fromHigh: Double,
        toLow: Double,
        toHigh: Double
    ): Double {
        return toLow + (value - fromLow) / (fromHigh - fromLow) * (toHigh - toLow)
    }

    fun clamp(value: Double, low: Double, high: Double): Double {
        return Math.min(Math.max(value, low), high)
    }
}
