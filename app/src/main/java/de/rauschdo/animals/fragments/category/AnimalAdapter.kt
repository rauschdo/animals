package de.rauschdo.animals.fragments.category

import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.skydoves.bindables.BindingListAdapter
import com.skydoves.bindables.binding
import de.rauschdo.animals.Constants
import de.rauschdo.animals.R
import de.rauschdo.animals.database.Animal
import de.rauschdo.animals.databinding.ItemAnimalBinding
import de.rauschdo.animals.fragments.category.holder.AnimalHolder
import de.rauschdo.animals.fragments.category.holder.EmptyHolder
import kotlin.math.roundToInt

class AnimalAdapter(
    private val categoryName: String,
    private val listener: ClickListener
) : BindingListAdapter<Animal, RecyclerView.ViewHolder>(diffUtil) {

    private val viewTypePanda = 1
    private val viewTypeTurtle = 2
    private val viewTypeDog = 3
    private val viewTypeBird = 4
    private val viewTypeOther = 5

    private var hasInitParentDimensions = false
    private var maxCardWidth: Int = 0
    private var maxTurtleCardWidth: Int = 0

    lateinit var selectionTracker: SelectionTracker<Long>

    companion object {
        private val diffUtil = object : DiffUtil.ItemCallback<Animal>() {
            override fun areItemsTheSame(
                oldItem: Animal,
                newItem: Animal
            ): Boolean =
                oldItem.name == newItem.name

            override fun areContentsTheSame(
                oldItem: Animal,
                newItem: Animal
            ): Boolean =
                oldItem.favourite == newItem.favourite
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AnimalHolder -> {
                holder.bind(getItem(position), position)
            }
            else -> Unit
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // At this point [parent] has been measured and has valid width
        // width can still be 0 so use measuredWidth
        if (!hasInitParentDimensions) {
            maxCardWidth = (parent.measuredWidth * 0.80f).roundToInt()
            maxTurtleCardWidth = (parent.measuredWidth * 0.45f).roundToInt()
            hasInitParentDimensions = true
        }
        return when (viewType) {
            viewTypePanda -> {
                AnimalHolder(
                    parent.binding<ItemAnimalBinding>(R.layout.item_animal),
                    maxCardWidth,
                    listener,
                    selectionTracker = selectionTracker
                )
            }
            viewTypeTurtle -> {
                AnimalHolder(
                    parent.binding<ItemAnimalBinding>(R.layout.item_animal),
                    maxCardWidth,
                    listener,
                    selectionTracker = selectionTracker
                )
            }
            viewTypeDog -> {
                AnimalHolder(
                    parent.binding<ItemAnimalBinding>(R.layout.item_animal),
                    maxCardWidth,
                    listener,
                    selectionTracker = selectionTracker
                )
            }
            viewTypeBird -> {
                AnimalHolder(
                    parent.binding<ItemAnimalBinding>(R.layout.item_animal),
                    maxCardWidth,
                    listener,
                    selectionTracker = selectionTracker
                )
            }
            viewTypeOther -> {
                AnimalHolder(
                    parent.binding<ItemAnimalBinding>(R.layout.item_animal),
                    maxCardWidth,
                    listener,
                    selectionTracker = selectionTracker
                )
            }
            else -> EmptyHolder(LinearLayout(parent.context))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (categoryName) {
            Constants.pandaID -> viewTypePanda
            Constants.turtleID -> viewTypeTurtle
            Constants.dogID -> viewTypeDog
            Constants.birdID -> viewTypeBird
            Constants.otherID -> viewTypeOther
            else -> super.getItemViewType(position)
        }
    }

    // Selection Key does representate the animalID
    fun removeItem(selectionKey: Long) {
        val index = currentList.indexOfFirst { it.animalCategoryId == selectionKey }
        if (index != -1) {
            val newList = currentList.apply {
                removeAt(index)
            }
            submitList(newList)
        }
    }

    interface ClickListener {
        fun onAnimalClicked(animal: Animal, originView: MaterialCardView)
        fun onFavouriteClicked(animalId: Long, favourite: Boolean)
        fun onTrackerMapChanged(map: MutableMap<Int, SelectionTracker<Long>>)
    }

    // Selection specific
    // Commented return values would allow easier delete but
    // kill native display of checkmark in MaterialCardView
    // the update which would trigger natively doesn't anymore - didn't find reason yet
    class Details : ItemDetailsLookup.ItemDetails<Long>() {
        var position: Long = 0
        var mSelectionKey: Long = 0
        override fun getPosition(): Int = position.toInt()
        override fun getSelectionKey(): Long = mSelectionKey //position //mSelectionKey
    }

    class DetailsLookup(private val recyclerView: RecyclerView) : ItemDetailsLookup<Long>() {
        override fun getItemDetails(e: MotionEvent): ItemDetails<Long>? {
            val view = recyclerView.findChildViewUnder(e.x, e.y)
            if (view != null) {
                val holder = recyclerView.getChildViewHolder(view)
                if (holder is AnimalHolder) {
                    return holder.getItemDetails()
                }
            }
            return null
        }
    }

    class KeyProvider(private val adapter: AnimalAdapter) :
        ItemKeyProvider<Long>(SCOPE_MAPPED) {
        override fun getKey(position: Int): Long =
            adapter.getItem(position).id //position.toLong() //adapter.getItem(position).id

        override fun getPosition(key: Long): Int = key.toInt()
    }
}
