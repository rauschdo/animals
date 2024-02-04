package de.rauschdo.animals.utility.preference

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import de.rauschdo.animals.utility.preference.PreferencesUtil.typedPreferences

object AppPreference {

    private const val SHOW_SHEET_ON_RECREATE = "SHOW_SHEET_ON_RECREATE"

    private const val PREFERED_UI_MODE_SETTING = "PREFERED_UI_MODE_SETTING"

    private const val HAS_SHOWN_INTRO = "HAS_SHOWN_INTRO"

    fun rememberBottomSheetRecreate(ctx: Context?) {
        ctx?.let {
            typedPreferences(it)?.edit()
                ?.putBoolean(SHOW_SHEET_ON_RECREATE, true)?.apply()
        }
    }

    fun shouldShowBottomSheet(ctx: Context) =
        typedPreferences(ctx)?.getBoolean(SHOW_SHEET_ON_RECREATE, false) ?: false

    fun deleteBottomSheetReminder(ctx: Context?) {
        ctx?.let {
            typedPreferences(it)
                ?.edit()
                ?.remove(SHOW_SHEET_ON_RECREATE)
                ?.apply()
        }
    }

    fun savePreferedUiMode(ctx: Context?, constant: Int) {
        ctx?.let {
            typedPreferences(it)?.edit()
                ?.putInt(PREFERED_UI_MODE_SETTING, constant)?.apply()
        }
    }

    fun getPreferedUiMode(ctx: Context) =
        typedPreferences(ctx)?.getInt(
            PREFERED_UI_MODE_SETTING,
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        ) ?: AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM

    fun hasShownIntro(ctx: Context) =
        typedPreferences(ctx)?.getBoolean(HAS_SHOWN_INTRO, false) ?: false

    fun saveIntroShown(ctx: Context?) {
        ctx?.let {
            typedPreferences(it)?.edit()
                ?.putBoolean(HAS_SHOWN_INTRO, true)?.apply()
        }
    }
}