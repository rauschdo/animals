package de.rauschdo.animals.workers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import dagger.hilt.android.AndroidEntryPoint
import de.rauschdo.animals.Constants
import de.rauschdo.animals.database.AnimalDao
import de.rauschdo.animals.utility.GlideApp
import de.rauschdo.animals.utility.preference.NotificationPreference
import kotlinx.coroutines.*
import javax.inject.Inject

//FIXME at later point add ON BOOT FINISHED RESET OF ALARMS

@DelicateCoroutinesApi
@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var animalDao: AnimalDao

    override fun onReceive(context: Context, intent: Intent) {
        val requestCode = intent.getIntExtra(Constants.NOTIFICATION_ALARM_RC, 0)
        val requestMillis = intent.getLongExtra(Constants.NOTIFICATION_ALARM_MILLIS, -1L)

        Log.i(Notification.TAG, "Received Alarm with code $requestCode for millis $requestMillis")

        if (requestCode == Constants.NOTIFICATION_ID &&
            NotificationPreference.hasNotificationEnabled(context)
        ) {
            GlobalScope.launch(Dispatchers.IO) {
                val animal = if (NotificationPreference.shouldNotificationUseFavorites(context)) {
                    animalDao.getFavorites()?.random()
                        ?: animalDao.getAll().random()
                } else {
                    animalDao.getAll().random()
                }

                val fact = animal.triviaPoints.random()

                withContext(Dispatchers.Main) {
                    GlideApp.with(context)
                        .asBitmap()
                        .load(animal.imageUrls.random())
                        .into(object : CustomTarget<Bitmap>() {
                            override fun onResourceReady(
                                resource: Bitmap,
                                transition: Transition<in Bitmap>?
                            ) {
                                Notification.sendNotification(
                                    context = context,
                                    bitmap = resource,
                                    animalName = animal.name,
                                    randomFact = fact
                                )
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                                // unused
                            }

                            override fun onLoadFailed(errorDrawable: Drawable?) {
                                Notification.sendNotification(
                                    context = context,
                                    bitmap = null,
                                    animalName = animal.name,
                                    randomFact = fact
                                )
                            }
                        })
                }
                Notification.setAlarm(context)
            }
        }
    }
}