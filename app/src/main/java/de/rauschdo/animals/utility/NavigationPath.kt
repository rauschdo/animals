package de.rauschdo.animals.utility

import androidx.navigation.NavDirections
import de.rauschdo.animals.fragments.SplashFragmentDirections
import de.rauschdo.animals.fragments.category.CategoryFragmentDirections
import de.rauschdo.animals.fragments.settings.SettingsFragmentDirections

enum class NavigationPath {

    SPLASH_TO_CATEGORY,
    CATEGORY_TO_SETTINGS,
    SETTINGS_TO_ORDER;

    private fun getDirections(): NavDirections {
        return when (this) {
            SPLASH_TO_CATEGORY -> SplashFragmentDirections.splashFragmentToCategoryFragment()
            CATEGORY_TO_SETTINGS -> CategoryFragmentDirections.categoryFragmentToSettingsFragment()
            SETTINGS_TO_ORDER -> SettingsFragmentDirections.settingsFragmentToOrderFragment()
        }
    }

    operator fun invoke() = getDirections()
}