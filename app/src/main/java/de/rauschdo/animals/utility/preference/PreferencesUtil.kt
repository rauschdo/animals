package de.rauschdo.animals.utility.preference

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

/**
 * Entry Object to create Preference Documents
 * Each Section has their own Object for more clarity
 */
object PreferencesUtil {

    private const val LOCAL_DATA_IMPORTED = "importedLocal"

    fun typedPreferences(ctx: Context, name: String = "prefs"): SharedPreferences? {
        var prefs: SharedPreferences?

        prefs = ctx.getSharedPreferences(name, Context.MODE_PRIVATE)

        if (prefs == null) prefs = PreferenceManager.getDefaultSharedPreferences(ctx)
        return prefs
    }

    fun saveDataImport(ctx: Context?) {
        ctx?.let {
            typedPreferences(it)?.edit()
                ?.putBoolean(LOCAL_DATA_IMPORTED, true)?.apply()
        }
    }

    fun hasImportedData(ctx: Context) =
        typedPreferences(ctx)?.getBoolean(LOCAL_DATA_IMPORTED, false) ?: false

    fun clearDataImport(ctx: Context) =
        typedPreferences(ctx)?.edit()?.remove(LOCAL_DATA_IMPORTED)?.apply()

}