package de.rauschdo.animals

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.facebook.stetho.Stetho
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.hilt.android.HiltAndroidApp
import de.rauschdo.animals.Constants.CHANNEL_DESCRIPTION
import de.rauschdo.animals.Constants.CHANNEL_NAME
import de.rauschdo.animals.Constants.NOTIFICATION_CHANNEL_ID
import de.rauschdo.animals.workers.Notification
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        Stetho.initializeWithDefaults(this)
        Notification.setupNotificationChannel(this)
    }
}