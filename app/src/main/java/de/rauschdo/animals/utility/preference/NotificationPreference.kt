package de.rauschdo.animals.utility.preference

import android.content.Context
import de.rauschdo.animals.utility.preference.PreferencesUtil.typedPreferences

object NotificationPreference {

    private const val HAS_NOTIFICATION_ENABLED = "HAS_NOTIFICATION_ENABLED"

    private const val NOTIFICATION_TIME = "NOTIFICATION_TIME"

    private const val NOTIFICATION_USE_FAVORITES = "NOTIFICATION_USE_FAVORITES"

    fun saveNotificationEnabled(ctx: Context?, enabled: Boolean) {
        ctx?.let {
            typedPreferences(it)?.edit()
                ?.putBoolean(HAS_NOTIFICATION_ENABLED, enabled)?.apply()
        }
    }

    fun hasNotificationEnabled(ctx: Context) =
        typedPreferences(ctx)?.getBoolean(HAS_NOTIFICATION_ENABLED, false) ?: false

    fun saveNotificationTime(ctx: Context?, millis: Long) {
        ctx?.let {
            typedPreferences(it)?.edit()
                ?.putLong(NOTIFICATION_TIME, millis)?.apply()
        }
    }

    fun getNotificationTime(ctx: Context) =
        typedPreferences(ctx)?.getLong(NOTIFICATION_TIME, 0L) ?: 0L

    fun saveNotificationUseFavorites(ctx: Context?, useFavorites: Boolean) {
        ctx?.let {
            typedPreferences(it)?.edit()
                ?.putBoolean(NOTIFICATION_USE_FAVORITES, useFavorites)?.apply()
        }
    }

    fun shouldNotificationUseFavorites(ctx: Context) =
        typedPreferences(ctx)?.getBoolean(NOTIFICATION_USE_FAVORITES, false) ?: false


}