package de.rauschdo.animals.fragments.settings.order

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.skydoves.bindables.BindingListAdapter
import com.skydoves.bindables.binding
import de.rauschdo.animals.R
import de.rauschdo.animals.database.Category
import de.rauschdo.animals.databinding.ItemOrderCategoryBinding
import java.util.*

class OrderAdapter(private val listener: ClickListener) :
    BindingListAdapter<Category, OrderAdapter.OrderViewHolder>(diffUtil),
    ItemTouchInterface {

    private var mOrderList: List<Category>? = null

    companion object {
        private val diffUtil = object : DiffUtil.ItemCallback<Category>() {
            override fun areItemsTheSame(
                oldItem: Category,
                newItem: Category
            ): Boolean =
                oldItem.categoryId == newItem.categoryId

            override fun areContentsTheSame(
                oldItem: Category,
                newItem: Category
            ): Boolean =
                oldItem.orderIndex == newItem.orderIndex
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder =
        parent.binding<ItemOrderCategoryBinding>(R.layout.item_order_category)
            .let(::OrderViewHolder)

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) =
        holder.bind(getItem(position), position)

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        val newList = this.currentList.toMutableList()
        Collections.swap(newList, fromPosition, toPosition)

        mOrderList = newList.mapIndexed { index, category ->
            Category(
                category.categoryId,
                category.name,
                assetId = category.assetId,
                orderIndex = index + 1
            )
        }.toMutableList()

        submitList(newList)
    }

    override fun onFinish() {
        super.onFinish()
        listener.commitUpdate(mOrderList)
    }

    class OrderViewHolder constructor(
        private val binding: ItemOrderCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(category: Category, position: Int) {
            with(binding) {
                this.category = category
                executePendingBindings()
            }
        }
    }

    interface ClickListener {
        fun commitUpdate(updatedList: List<Category>?)
    }
}
