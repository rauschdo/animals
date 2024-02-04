package de.rauschdo.animals.utility

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import de.rauschdo.animals.database.Animal
import de.rauschdo.animals.database.Category
import de.rauschdo.animals.database.CategoryWithAnimals
import de.rauschdo.animals.fragments.category.AnimalAdapter
import de.rauschdo.animals.fragments.category.CategoryAdapter
import de.rauschdo.animals.fragments.favourite.FavouriteAdapter
import de.rauschdo.animals.fragments.settings.order.OrderAdapter

@BindingAdapter("adapter")
fun bindAdapter(view: RecyclerView, adapter: RecyclerView.Adapter<*>) {
    view.adapter = adapter.apply {
        stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
    }
}

@BindingAdapter("categoryItems")
fun setCategoryItems(listView: RecyclerView, items: List<CategoryWithAnimals>?) {
    items?.let {
        (listView.adapter as? CategoryAdapter)?.submitList(items)
    }
}

@BindingAdapter("animalItems")
fun setAnimalItems(listView: RecyclerView, items: List<Animal>?) {
    items?.let {
        (listView.adapter as? AnimalAdapter)?.submitList(items)
    }
}

@BindingAdapter("favouriteItems")
fun setAnimalFavourites(listView: RecyclerView, items: List<Animal>?) {
    items?.let {
        (listView.adapter as? FavouriteAdapter)?.submitList(items)
    }
}

@BindingAdapter("orderItems")
fun setCategoriesForOrdering(listView: RecyclerView, items: List<Category>?) {
    items?.let {
        (listView.adapter as? OrderAdapter)?.submitList(items)
    }
}