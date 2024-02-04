package de.rauschdo.animals.activities

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import de.rauschdo.animals.R
import de.rauschdo.animals.architecture.BaseActivity
import de.rauschdo.animals.data.viewmodel.AnimalViewModel
import de.rauschdo.animals.databinding.ActivityMainBinding
import de.rauschdo.animals.utility.*

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    val binding by viewBinding(ActivityMainBinding::inflate)

    internal val animalViewModel: AnimalViewModel by viewModels()

    private lateinit var appBarConfiguration: AppBarConfiguration

    private val destinationChangeListener =
        NavController.OnDestinationChangedListener { controller, destination, arguments ->
            when (destination.id) {
                else -> Unit
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController.apply {
            removeOnDestinationChangedListener(destinationChangeListener)
            addOnDestinationChangedListener(destinationChangeListener)
        }

        if (!Utils.isInitMain) {
            Utils.isInitMain = true
            Utils.isCurrentlyNightMode = isNightMode()
        } else {
            // Hacky workaround as system does something weird with insets or paddings
            // not seeable through layout inspector
            if (isNightMode() != Utils.isCurrentlyNightMode) {
                Utils.isCurrentlyNightMode = isNightMode()
                binding.navHostFragment.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    updateMargins(bottom = 56.dpToPx)
                }
            }
        }

        with(binding.toolbar) {
            setSupportActionBar(this)
            setNavigationOnClickListener {
                onSupportNavigateUp()
            }
        }
        // This handles the display automatically of title and back button
        // but does not automatically do the back navigation therefore listener above
        appBarConfiguration = AppBarConfiguration(
            topLevelDestinationIds = setOf(
                R.id.splashFragment,
                R.id.categoryFragment,
                R.id.favouriteFragment,
                R.id.settingsFragment
            ),
            fallbackOnNavigateUpListener = ::onSupportNavigateUp
        )
        NavigationUI.setupActionBarWithNavController(
            this,
            navController,
            appBarConfiguration
        )

        // Only way to use custom animations with the bottom navigation bar
        val options = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setEnterAnim(R.anim.nav_enter_anim)
            .setExitAnim(R.anim.nav_exit_anim)
            .setPopEnterAnim(R.anim.nav_pop_enter_anim)
            .setPopExitAnim(R.anim.nav_pop_exit_anim)
            .setPopUpTo(R.id.nav_graph, true)
            .build()

        with(binding) {
            navigation.apply {
                setOnItemSelectedListener { item ->
                    when (item.itemId) {
                        R.id.categoryFragment ->
                            navController.navigate(R.id.categoryFragment, null, options)
                        R.id.favouriteFragment ->
                            navController.navigate(R.id.favouriteFragment, null, options)
                        R.id.settingsFragment ->
                            navController.navigate(R.id.settingsFragment, null, options)
                    }
                    true
                }
                setOnItemReselectedListener { item ->
                    val lastScreenId = getCurrentDestinationId()
                    val reselectOptions = navOptions {
                        popUpTo(lastScreenId ?: item.itemId) {
                            inclusive = true
                        }
                    }

                    when (item.itemId) {
                        R.id.categoryFragment ->
                            navController.navigate(R.id.categoryFragment, null, reselectOptions)
                        R.id.favouriteFragment ->
                            navController.navigate(R.id.favouriteFragment, null, reselectOptions)
                        R.id.settingsFragment ->
                            navController.navigate(R.id.settingsFragment, null, reselectOptions)
                    }
                }
            }
        }
//        NotificationWorker.scheduleWork(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(findNavController(R.id.navHostFragment), appBarConfiguration)
    }

    fun showBottomNavigation() {
        with(binding) {
            if (navigation.visibility != View.VISIBLE) {
                navigation.visibility = View.VISIBLE
            }
        }
    }

    fun hideBottomNavigation() {
        with(binding) {
            navigation.visibility = View.GONE
        }
    }

    fun showSnackbar(text: CharSequence) {
        with(binding) {
            Snackbar.make(mainCoordinator, text, Snackbar.LENGTH_SHORT)
                .setAnchorView(navigation)
                .show()
        }
    }
}