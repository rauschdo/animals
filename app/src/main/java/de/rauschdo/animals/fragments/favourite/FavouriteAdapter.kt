package de.rauschdo.animals.fragments.favourite

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.skydoves.bindables.BindingListAdapter
import com.skydoves.bindables.binding
import de.rauschdo.animals.R
import de.rauschdo.animals.custom.LikeButtonView
import de.rauschdo.animals.database.Animal
import de.rauschdo.animals.databinding.ItemFavouriteBinding
import de.rauschdo.animals.utility.loadImageNetwork

class FavouriteAdapter(private val listener: ClickListener) :
    BindingListAdapter<Animal, FavouriteAdapter.FavouriteViewHolder>(diffUtil) {

    companion object {
        private val diffUtil = object : DiffUtil.ItemCallback<Animal>() {
            override fun areItemsTheSame(
                oldItem: Animal,
                newItem: Animal
            ): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: Animal,
                newItem: Animal
            ): Boolean =
                oldItem.favourite && newItem.favourite
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavouriteViewHolder =
        parent.binding<ItemFavouriteBinding>(R.layout.item_favourite).let(::FavouriteViewHolder)

    override fun onBindViewHolder(holder: FavouriteViewHolder, position: Int) =
        holder.bindFavourite(getItem(position), position)

    inner class FavouriteViewHolder constructor(
        private val binding: ItemFavouriteBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bindFavourite(animal: Animal, position: Int) {
            with(binding) {
                this.animal = animal

                executePendingBindings()

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
                    listener.onAnimalClicked(
                        animal,
                        binding.animalCardRoot
                    )
                }
            }
        }
    }

    private fun getCategoryIconForAnimal(animal: Animal): Int {
        return R.drawable.ic_component_placeholder
    }

    interface ClickListener {
        fun onAnimalClicked(animal: Animal, originView: MaterialCardView)
        fun onFavouriteClicked(animalId: Long, favourite: Boolean)
    }
}
