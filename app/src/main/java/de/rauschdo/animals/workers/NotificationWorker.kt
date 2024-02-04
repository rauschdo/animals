package de.rauschdo.animals.workers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import de.rauschdo.animals.database.AnimalDao
import de.rauschdo.animals.utility.DateUtil
import de.rauschdo.animals.utility.GlideApp
import de.rauschdo.animals.utility.preference.NotificationPreference
import de.rauschdo.animals.utility.toMillis
import org.threeten.bp.LocalDateTime
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

/**
 * Worker to send daily animal fact (TODO find API or create on own)
 * on the selected time (if set)
 *
 * https://developer.android.com/training/dependency-injection/hilt-jetpack#workmanager
 */
@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val animalDao: AnimalDao
) : CoroutineWorker(context, params) {

    companion object {
        val LOG_TAG_WORK = "NotificationWorker"
        val WORK_TAG = "AnimalFactWork"

        /**
         * Function to enqueue work
         */
        fun scheduleWork(context: Context) {
            WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG)

            val notificationTime = DateUtil.convertMillisecondsToLocalDateTime(
                NotificationPreference.getNotificationTime(context)
            )

            // avoid midnight problems by setting exection + 15 seconds
            val currentDate = LocalDateTime.now()
            var dueDate = currentDate
                .withHour(notificationTime.hour)
                .withMinute(notificationTime.minute)
                .withSecond(15)

            if (dueDate.isBefore(currentDate) || dueDate.toMillis() == currentDate.toMillis()) {
                dueDate = dueDate.plusHours(24)
            }

            Log.i(LOG_TAG_WORK, "Next work is due at $dueDate")

            val timeDiff = dueDate.toMillis() - currentDate.toMillis()

//            val hours = TimeUnit.MILLISECONDS.toHours(timeDiff)
//            Log.i(LOG_TAG_WORK, "Setup next work that should execute in $hours Hours")

            // (milliseconds / 1000) / 60;
            val minutes = TimeUnit.MILLISECONDS.toMinutes(timeDiff)
            Log.i(LOG_TAG_WORK, "Setup next work that should execute in $minutes Minutes")

            // (milliseconds / 1000);
//            val seconds = (TimeUnit.MILLISECONDS.toSeconds(timeDiff) % 60)
//            Log.i(LOG_TAG_WORK, "Setup next work that should execute in $seconds Seconds")

            val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                .build()
            WorkManager.getInstance(context).enqueue(workRequest)

            //UniqueWorkName is used to check duplicacy for works
            try {
                val workInfos = WorkManager
                    .getInstance(context)
                    .getWorkInfosByTag(WORK_TAG)
                    .get()

                if (workInfos.size > 0) {
                    workInfos.forEach { workInfo ->
                        Log.i(
                            LOG_TAG_WORK,
                            "Work ${workInfo.tags} is in state ${workInfo.state.name}"
                        )
                    }
                }
            } catch (e: ExecutionException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } finally {
                Log.i(LOG_TAG_WORK, "finally - WorkManager should be doing something")
            }
        }
    }

    override suspend fun doWork(): Result {
        try {
            if (NotificationPreference.hasNotificationEnabled(context)) {
                // fetch random animal from database - TODO later only from favorited Animals
                val animal = if (NotificationPreference.shouldNotificationUseFavorites(context)) {
                    animalDao.getFavorites()?.random()
                        ?: animalDao.getAll().random()
                } else {
                    animalDao.getAll().random()
                }

                val fact = animal.triviaPoints.random()

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
            scheduleWork(context)
        } catch (e: Exception) {
            return Result.retry()
        }
        return Result.success()
    }
}
