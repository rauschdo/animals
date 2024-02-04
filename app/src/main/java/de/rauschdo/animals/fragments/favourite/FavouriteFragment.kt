package de.rauschdo.animals.fragments.favourite

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import com.google.android.material.card.MaterialCardView
import com.google.android.material.transition.MaterialFadeThrough
import de.rauschdo.animals.Constants
import de.rauschdo.animals.R
import de.rauschdo.animals.activities.MainActivity
import de.rauschdo.animals.activities.detail.DetailActivity
import de.rauschdo.animals.architecture.BaseActivity
import de.rauschdo.animals.architecture.BaseFragment
import de.rauschdo.animals.data.viewmodel.AnimalViewModel
import de.rauschdo.animals.database.Animal
import de.rauschdo.animals.databinding.FragmentFavouriteBinding
import de.rauschdo.animals.fragments.category.ItemMarginDecorator
import de.rauschdo.animals.utility.dpToPx
import de.rauschdo.animals.utility.getVisibility
import de.rauschdo.animals.utility.observerOfNonNull
import de.rauschdo.animals.utility.showOptionalIcons
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FavouriteFragment : BaseFragment<FragmentFavouriteBinding>(R.layout.fragment_favourite) {

    private val viewModel by activityViewModels<AnimalViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(false)

        activity?.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_app_settings, menu)
                menu.showOptionalIcons()
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.action_sheet -> {
                        (activity as? BaseActivity)?.openBottomSheet()
                    }
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        binding {
            adapter = FavouriteAdapter(object : FavouriteAdapter.ClickListener {
                override fun onAnimalClicked(
                    animal: Animal,
                    originView: MaterialCardView
                ) {
                    (activity as? MainActivity)?.let { activity ->
                        val intent = Intent(activity, DetailActivity::class.java).apply {
                            putExtra(Constants.EXTRA_ANIMAL_ID, animal.id)
                            putExtra(Constants.EXTRA_CATEGORY_ID, animal.animalCategoryId)
                            putExtra(Constants.EXTRA_ENABLE_PAGER, false)
                            putExtra(Constants.EXTRA_TRANSITION, id)
                        }
                        startActivity(
                            intent,
                            ActivityOptionsCompat.makeSceneTransitionAnimation(
                                activity, originView, id.toString()
                            ).toBundle()
                        )
                    }
                }

                override fun onFavouriteClicked(animalId: Long, favourite: Boolean) {
                    launch {
                        withContext(Dispatchers.IO) {
                            viewModel.setAnimalFavourite(animalId, favourite)
                        }
                    }
                }
            })

            animalFavouriteRecycler.apply {
                addItemDecoration(
                    ItemMarginDecorator(
                        verticalMargin = 8.dpToPx
                    )
                )
            }

            viewModel.favourites.observe(viewLifecycleOwner, observerOfNonNull {
                vm = viewModel
                animalFavouriteRecycler.visibility = it.isNotEmpty().getVisibility()
                emptyView.visibility = it.isEmpty().getVisibility()
            })
        }
    }
}
