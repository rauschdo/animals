package de.rauschdo.animals.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import de.rauschdo.animals.utility.isLandscape

object WidgetDimenHelper {

    /**
     * NOTE: This is not an universal function as this can vary between devices.
     * Based on
     * https://developer.android.com/guide/topics/appwidgets/layouts#anatomy_determining_size
     *
     * @param context
     * @param size  Widget size in dp.
     * @param n     minimum allowed width columns
     *
     * @return Size in number of cells.
     */
    fun calculateWidthCells(context: Context, size: Int, n: Int = 2): Int {
        var m = n
        if (context.isLandscape()) {
            if (142 * n - 15 < size) {
                ++m
                calculateWidthCells(context, size, n = m)
            }
        } else {
            if (73 * n - 16 < size) {
                ++m
                calculateWidthCells(context, size, n = m)
            }
        }
        return n - 1
    }

    /**
     * NOTE: This is not an universal function as this can vary between devices.
     * Based on
     * https://developer.android.com/guide/topics/appwidgets/layouts#anatomy_determining_size
     *
     * @param context
     * @param size Widget size in dp.
     * @param n     minimum allowed height rows

     * @return Size in number of cells.
     */
    fun calculateHeightCells(context: Context, size: Int, n: Int = 1): Int {
        var m = n
        if (context.isLandscape()) {
            if (66 * n - 15 < size) {
                ++m
                calculateWidthCells(context, size, n = m)
            }
        } else {
            if (118 * n - 16 < size) {
                ++m
                calculateWidthCells(context, size, n = m)
            }
        }
        return n - 1
    }

    fun getWidgetsSize(
        context: Context,
        appWidgetManager: AppWidgetManager,
        widgetId: Int
    ): Pair<Int, Int> {
        val isPortrait = !context.isLandscape()
        val width = getWidgetWidth(appWidgetManager, isPortrait, widgetId)
        val height = getWidgetHeight(appWidgetManager, isPortrait, widgetId)
//        val widthInPx = context.dip(width)
//        val heightInPx = context.dip(height)
        return width to height
    }

    private fun getWidgetWidth(
        appWidgetManager: AppWidgetManager,
        isPortrait: Boolean,
        widgetId: Int
    ): Int = if (isPortrait) {
        getWidgetSizeInDp(appWidgetManager, widgetId, AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
    } else {
        getWidgetSizeInDp(appWidgetManager, widgetId, AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH)
    }

    private fun getWidgetHeight(
        appWidgetManager: AppWidgetManager,
        isPortrait: Boolean,
        widgetId: Int
    ): Int =
        if (isPortrait) {
            getWidgetSizeInDp(
                appWidgetManager,
                widgetId,
                AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT
            )
        } else {
            getWidgetSizeInDp(
                appWidgetManager,
                widgetId,
                AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT
            )
        }

    private fun getWidgetSizeInDp(
        appWidgetManager: AppWidgetManager,
        widgetId: Int,
        key: String
    ): Int =
        appWidgetManager.getAppWidgetOptions(widgetId).getInt(key, 0)

    private fun Context.dip(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}