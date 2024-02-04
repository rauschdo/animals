package de.rauschdo.animals

object Constants {

    const val NOTIFICATION_CHANNEL_ID = "animal_notifications"

    // Since there is only one Notification from this App
    const val NOTIFICATION_ID = 42069
    const val NOTIFICATION_ALARM_RC = "NOTIFICATION_ALARM_RC"
    const val NOTIFICATION_ALARM_MILLIS = "NOTIFICATION_ALARM_MILLIS"
    const val NOTIFICATION_ANIMAL = "NOTIFICATION_ANIMAL"

    const val CHANNEL_NAME = "Tierwelt Benachrichtigungen"
    const val CHANNEL_DESCRIPTION = "Täglicher Tierfakt"

    const val DB_NAME = "animal_db"

    const val SPLASH_ROOT_ANIM_ID = R.id.splashRootAnimStart

    const val pandaID = "Pandas"
    const val turtleID = "Schildkröten"
    const val dogID = "Hunde"
    const val birdID = "Vögel"
    const val otherID = "Andere"

    const val EXTRA_URL = "url"
    const val EXTRA_USES_NAV_HOST = "useNavHost"
    const val EXTRA_ANIMAL_ID = "detailAnimalId"
    const val EXTRA_CATEGORY_ID = "detailCategoryId"
    const val EXTRA_POSITION = "categoryPosition"
    const val EXTRA_ENABLE_PAGER = "enablePager"
    const val EXTRA_TRANSITION = "shared_transition"
}