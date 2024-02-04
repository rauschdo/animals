package de.rauschdo.animals.fragments.category

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.selection.Selection
import androidx.recyclerview.selection.SelectionTracker
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.card.MaterialCardView
import com.google.android.material.transition.MaterialSharedAxis
import de.rauschdo.animals.Constants
import de.rauschdo.animals.R
import de.rauschdo.animals.activities.MainActivity
import de.rauschdo.animals.activities.detail.DetailActivity
import de.rauschdo.animals.architecture.BaseActivity
import de.rauschdo.animals.architecture.BaseFragment
import de.rauschdo.animals.data.viewmodel.AnimalViewModel
import de.rauschdo.animals.database.Animal
import de.rauschdo.animals.databinding.FragmentCategoryBinding
import de.rauschdo.animals.utility.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size

class CategoryFragment :
    BaseFragment<FragmentCategoryBinding>(R.layout.fragment_category),
    ActionMode.Callback {

    private val viewModel by activityViewModels<AnimalViewModel>()

    private var actionMode: ActionMode? = null

    private var currentSelection: Selection<Long>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MotionUtil.createAxisTransition(
            startId = R.id.splashRootAnimStart,
            endId = R.id.categoryRootAnimEnd,
            axis = MaterialSharedAxis.Z,
            entering = true
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)

        (activity as? MainActivity)?.let { activity ->
            activity.showBottomNavigation()

            activity.addMenuProvider(object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.menu_main, menu)
                    menu.showOptionalIcons()
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    when (menuItem.itemId) {
                        R.id.action_confetti -> {
                            triggerConfetti(
                                colors = intArrayOf(
                                    ContextCompat.getColor(activity, R.color.blueAccent),
                                    ContextCompat.getColor(activity, R.color.textColorPrimary),
                                    ContextCompat.getColor(activity, R.color.greyParicle)
                                ),
                                shapes = arrayOf(
                                    Shape.DrawableShape(
                                        ContextCompat.getDrawable(
                                            activity,
                                            R.drawable.ic_triangle
                                        )!!
                                    ),
                                    Shape.DrawableShape(
                                        ContextCompat.getDrawable(
                                            activity,
                                            R.drawable.ic_octagram
                                        )!!
                                    ),
                                    Shape.DrawableShape(
                                        ContextCompat.getDrawable(
                                            activity,
                                            R.drawable.ic_star
                                        )!!
                                    )
                                )
                            )
                        }
                        R.id.action_sheet -> {
                            (activity as? BaseActivity)?.openBottomSheet()
                        }
                        R.id.action_reset -> {
                            launch(Dispatchers.IO) {
                                DataUtil.readDataAndInsertInDB(resources, viewModel)
                            }
                        }
                        R.id.action_license -> {
                            startActivity(
                                Intent(
                                    this@CategoryFragment.context,
                                    OssLicensesMenuActivity::class.java
                                )
                            )
                        }
                    }
                    // Handle the menu selection
                    activity.showSnackbar(menuItem.title)
                    return true
                }
            }, viewLifecycleOwner, Lifecycle.State.RESUMED)

            binding {
                val adapterRef = CategoryAdapter(
                    listener = object : AnimalAdapter.ClickListener {
                        override fun onAnimalClicked(
                            animal: Animal,
                            originView: MaterialCardView
                        ) {
                            Log.e("8652", animal.name)
                            actionMode?.finish()

                            (activity as? MainActivity)?.let { activity ->
                                val intent = Intent(activity, DetailActivity::class.java).apply {
                                    putExtra(Constants.EXTRA_ANIMAL_ID, animal.id)
                                    putExtra(Constants.EXTRA_CATEGORY_ID, animal.animalCategoryId)
                                    putExtra(Constants.EXTRA_ENABLE_PAGER, true)
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

                        //Triggers ofc for each category, not optimal
                        //keep in mind for future to look for better solution
                        override fun onTrackerMapChanged(map: MutableMap<Int, SelectionTracker<Long>>) {
                            map.values.forEach {
                                it.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
                                    @SuppressLint("NotifyDataSetChanged")
                                    override fun onSelectionChanged() {
                                        if (it.selection.size() > 0) {
                                            currentSelection = it.selection
                                            triggerActionMode(activity, it.selection.size())
                                        } else if (actionMode != null) {
                                            actionMode?.finish()
                                        }
                                    }
                                })
                            }
                        }
                    }
                )

                animalCategoryRecycler.setHasFixedSize(true)
                adapter = adapterRef
                viewModel.itemList.observe(viewLifecycleOwner, observerOfNonNull {
                    Log.i("8652", "observer triggered")
                    vm = viewModel
                })

                // Entering after splash everything behaves as expected but adapter won't show
                // No issue identified in adapter workaround is another screen entering
                if (Utils.firstEnterCategory) {
                    Utils.firstEnterCategory = false
                    launch {
                        delay(250)
                        withContext(Dispatchers.Main) {
                            mainBinding {
                                navigation.selectedItemId = R.id.categoryFragment
                            }
                        }
                    }
                }
            }
        }
    }

    private fun triggerConfetti(colors: IntArray, shapes: Array<Shape>) {
        binding {
            confettiView.build()
                .addColors(*colors)
                .setDirection(0.0, 359.0)
                .setSpeed(0.5f, 4f)
                .setRotationEnabled(true)
                .setFadeOutEnabled(true)
                .setTimeToLive(5000L)
                .addShapes(*shapes)
                .addSizes(Size(12), Size(16, 6f))
                .setPosition(-50f, binding.confettiView.width + 50f, -50f, -50f)
                .streamFor(300, 5000L)
        }
    }

    //
    private fun triggerActionMode(activity: Activity, selectionSize: Int) {
        if (actionMode == null) {
            actionMode = activity.startActionMode(this@CategoryFragment)
        }
        actionMode?.apply {
            title = getString(R.string.menu_action_title, selectionSize)
            setSubtitle(R.string.menu_action_subtitle)
        }
    }

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        activity?.menuInflater?.inflate(
            R.menu.menu_main_action_mode,
            menu
        )
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean = false
    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_mode_delete -> {
                currentSelection?.let {
                    launch(Dispatchers.IO) {
                        it.forEach { key ->
                            //key is database id of animal
                            viewModel.deleteAnimal(key)
                        }
                    }
                    onDestroyActionMode(actionMode)
                }
            }
        }
        return true
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        mode?.finish()
        actionMode = null
        currentSelection = null
    }
}
