package de.rauschdo.animals.fragments.category

import android.view.ViewGroup
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.skydoves.bindables.BindingListAdapter
import com.skydoves.bindables.binding
import de.rauschdo.animals.R
import de.rauschdo.animals.database.CategoryWithAnimals
import de.rauschdo.animals.databinding.ItemCategoryBinding
import de.rauschdo.animals.utility.dpToPx

class CategoryAdapter(private val listener: AnimalAdapter.ClickListener) :
    BindingListAdapter<CategoryWithAnimals, CategoryAdapter.AnimalCategoryViewHolder>(diffUtil) {

    val mapOfTrackers = mutableMapOf<Int, SelectionTracker<Long>>()

    private val itemDecor = ItemMarginDecorator(
        horizontalMargin = 4.dpToPx
    )

    companion object {
        private val diffUtil = object : DiffUtil.ItemCallback<CategoryWithAnimals>() {
            override fun areItemsTheSame(
                oldItem: CategoryWithAnimals,
                newItem: CategoryWithAnimals
            ): Boolean = oldItem.category.categoryId == newItem.category.categoryId

            override fun areContentsTheSame(
                oldItem: CategoryWithAnimals,
                newItem: CategoryWithAnimals
            ): Boolean = true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimalCategoryViewHolder =
        parent.binding<ItemCategoryBinding>(R.layout.item_category).let(::AnimalCategoryViewHolder)

    override fun onBindViewHolder(holder: AnimalCategoryViewHolder, position: Int) =
        holder.bindCategory(getItem(position), position)

    inner class AnimalCategoryViewHolder constructor(
        private val binding: ItemCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bindCategory(category: CategoryWithAnimals, position: Int) {
            with(binding) {
                val adapterRef = AnimalAdapter(
                    categoryName = category.category.name,
                    listener = listener
                )
                animalsRecycler.apply {
                    removeItemDecoration(itemDecor)
                    addItemDecoration(itemDecor)
                }
                adapter = adapterRef
                item = category
                executePendingBindings()

                val currentTracker = defineSelectionTracker(
                    rv = animalsRecycler,
                    mAdapter = adapterRef,
                    id = "tracker_$position"
                )
                mapOfTrackers[position] = currentTracker

                // Important to set selection tracker after setting adpater on RV
                adapterRef.selectionTracker = currentTracker
                listener.onTrackerMapChanged(mapOfTrackers)
            }
        }
    }

    fun defineSelectionTracker(
        rv: RecyclerView,
        mAdapter: AnimalAdapter,
        id: String
    ): SelectionTracker<Long> {
        return SelectionTracker.Builder(
            id,
            rv,
            AnimalAdapter.KeyProvider(mAdapter),
            AnimalAdapter.DetailsLookup(rv),
            StorageStrategy.createLongStorage()
        )
            .withSelectionPredicate(SelectionPredicates.createSelectAnything())
            .build()
    }
}
