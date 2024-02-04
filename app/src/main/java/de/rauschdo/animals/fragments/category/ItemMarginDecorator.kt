package de.rauschdo.animals.fragments.category

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Add vertical and/or horizontal spacing to
 * [RecyclerView]'s Items
 */
class ItemMarginDecorator(
    private val horizontalMargin: Int = 0,
    private val verticalMargin: Int = 0
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.right = horizontalMargin
        outRect.left = horizontalMargin
        if (parent.getChildLayoutPosition(view) == 0) {
            outRect.top = verticalMargin
        }
        outRect.bottom = verticalMargin
    }
}