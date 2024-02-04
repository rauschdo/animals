package de.rauschdo.animals.fragments.settings.order

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.transition.MaterialFadeThrough
import de.rauschdo.animals.R
import de.rauschdo.animals.architecture.BaseActivity
import de.rauschdo.animals.architecture.BaseFragment
import de.rauschdo.animals.data.viewmodel.AnimalViewModel
import de.rauschdo.animals.database.Category
import de.rauschdo.animals.databinding.FragmentOrderBinding
import de.rauschdo.animals.fragments.category.ItemMarginDecorator
import de.rauschdo.animals.utility.dpToPx
import de.rauschdo.animals.utility.getVisibility
import de.rauschdo.animals.utility.observerOfNonNull
import de.rauschdo.animals.utility.showOptionalIcons
import java.util.*

class OrderFragment : BaseFragment<FragmentOrderBinding>(R.layout.fragment_order) {

    private val viewModel by activityViewModels<AnimalViewModel>()

    private var syncTimer: Timer? = null

    private var timeExcecutionDelay = 1000L

    private lateinit var modifiedOrderList: List<Category>

    private lateinit var mAdapter: OrderAdapter

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
            mAdapter = OrderAdapter(
                listener = object : OrderAdapter.ClickListener {
                    override fun commitUpdate(updatedList: List<Category>?) {
                        updatedList?.let {
                            modifiedOrderList = it
                            startUpdater()
                        }
                    }
                }
            )
            adapter = mAdapter

            categoryOrderRecycler.addItemDecoration(
                ItemMarginDecorator(
                    verticalMargin = 4.dpToPx
                )
            )
            ItemTouchHelper(SimpleItemTouchHelperCallback(mAdapter))
                .attachToRecyclerView(categoryOrderRecycler)

            viewModel.categories.observe(viewLifecycleOwner, observerOfNonNull {
                vm = viewModel
                categoryOrderRecycler.visibility = it.isNotEmpty().getVisibility()
                emptyView.visibility = it.isEmpty().getVisibility()
            })
        }
    }

    private fun stopUpdater() {
        syncTimer?.cancel()
        syncTimer = null
    }

    private fun startUpdater() {
        stopUpdater()

        val timerTask = object : TimerTask() {
            override fun run() {
                modifiedOrderList.forEach { category ->
                    viewModel.updateCategoryOrderIndex(category.categoryId, category.orderIndex)
                }
            }
        }
        syncTimer = Timer()
        syncTimer?.schedule(timerTask, timeExcecutionDelay)
    }
}
