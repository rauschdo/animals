package de.rauschdo.animals.fragments.settings.order

import androidx.recyclerview.widget.RecyclerView

/**
 * Below functions
 * move or dismissal event from a {@link ItemTouchHelper.Callback}.
 */
interface ItemTouchInterface {

    /**
     * Called when an item has been dragged far enough to trigger a move. This is called every time
     * an item is shifted, and **not** at the end of a "drop" event.
     * Implementations should call [RecyclerView.Adapter.notifyItemMoved] after
     * adjusting the underlying data to reflect this move.
     *
     * @param fromPosition The start position of the moved item.
     * @param toPosition   Then resolved position of the moved item.
     * @return True if the item was moved to the new adapter position.
     *
     * @see RecyclerView.getAdapterPositionFor
     * @see RecyclerView.ViewHolder.getAdapterPosition
     */
    fun onItemMove(fromPosition: Int, toPosition: Int) {}


    /**
     * Called when an item has been dismissed by a swipe.
     * Implementations should call [RecyclerView.Adapter.notifyItemRemoved] after
     * adjusting the underlying data to reflect this removal.
     *
     * @param position The position of the item dismissed.
     *
     * @see RecyclerView.getAdapterPositionFor
     * @see RecyclerView.ViewHolder.getAdapterPosition
     */
    fun onItemDismiss(position: Int) {}

    /**
     * Called when the user interaction with an element is over
     * and it also completed its animation.
     */
    fun onFinish() {}
}
