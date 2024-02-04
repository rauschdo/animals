package de.rauschdo.animals.fragments.category.holder

import androidx.core.view.updateLayoutParams
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.request.RequestOptions
import de.rauschdo.animals.R
import de.rauschdo.animals.custom.LikeButtonView
import de.rauschdo.animals.database.Animal
import de.rauschdo.animals.databinding.ItemAnimalBinding
import de.rauschdo.animals.fragments.category.AnimalAdapter
import de.rauschdo.animals.utility.GlideApp
import de.rauschdo.animals.utility.idOf
import de.rauschdo.animals.utility.loadImageNetwork

class AnimalHolder(
    private val binding: ItemAnimalBinding,
    private val maxCardWidth: Int,
    private val listener: AnimalAdapter.ClickListener,
    // Selection specific
    private val selectionTracker: SelectionTracker<Long>,
    private val details: AnimalAdapter.Details = AnimalAdapter.Details()
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(animal: Animal, position: Int) {
        with(binding) {
            this.animal = animal
            details.apply {
                this.position = position.toLong()
                mSelectionKey = animal.id
            }

            executePendingBindings()

            itemView.updateLayoutParams<RecyclerView.LayoutParams> {
                width = maxCardWidth
                marginStart = 0
                topMargin = 0
            }

            bindSelectedState()

            val iconRes = root.context.idOf(animal.iconId, "drawable")
            if (iconRes != 0) {
                GlideApp.with(root.context)
                    .load(iconRes)
                    .apply(RequestOptions().transform(CenterInside()))
                    .into(icon)
            }

            animal.imageUrls.getOrNull(0)?.let {
                primaryImage.loadImageNetwork(
                    root.context,
                    it,
                    R.drawable.ic_component_placeholder
                )
            }

            favouriteButton.apply {
                init(
                    initial = animal.favourite,
                    listener = object : LikeButtonView.Listener {
                        override fun onCheckedChanged(isChecked: Boolean) {
                            listener.onFavouriteClicked(
                                animalId = animal.id,
                                favourite = isChecked
                            )
                        }
                    })
            }

            root.setOnClickListener {
                listener.onAnimalClicked(animal, binding.animalCardRoot)
            }
        }
    }

    private fun bindSelectedState() {
        binding.animalCardRoot.isChecked = selectionTracker.isSelected(details.position)
    }

    fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> {
        return details
    }
}
