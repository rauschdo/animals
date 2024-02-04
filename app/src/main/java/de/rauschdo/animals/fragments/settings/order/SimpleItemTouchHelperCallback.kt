package de.rauschdo.animals.fragments.settings.order

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class SimpleItemTouchHelperCallback(
    private val mInterface: ItemTouchInterface
) : ItemTouchHelper.Callback() {

    override fun isLongPressDragEnabled(): Boolean {
        return true
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return false
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return if (viewHolder is OrderAdapter.OrderViewHolder) {
            val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
            makeMovementFlags(dragFlags, 0)
        } else {
            makeMovementFlags(0, 0)
        }
    }

    override fun onMove(
        recyclerView: RecyclerView,
        source: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        mInterface.onItemMove(source.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, i: Int) {
        mInterface.onItemDismiss(viewHolder.adapterPosition)
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        // Action finished
        mInterface.onFinish()
    }
}
