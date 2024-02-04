package de.rauschdo.animals.workers

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import de.rauschdo.animals.Constants
import de.rauschdo.animals.R
import de.rauschdo.animals.activities.MainActivity
import de.rauschdo.animals.utility.DateUtil
import de.rauschdo.animals.utility.preference.NotificationPreference
import de.rauschdo.animals.utility.toMillis
import org.threeten.bp.LocalDateTime
import java.util.*

object Notification {

    val TAG = "Notification"

    fun setAlarm(context: Context) {
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

        Log.i(TAG, "Next Alarm is due at $dueDate")

        (context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager)?.also { manager ->
            val rc = Constants.NOTIFICATION_ID
            Intent(context, AlarmReceiver::class.java).apply {
                putExtra(Constants.NOTIFICATION_ALARM_RC, rc)
                putExtra(Constants.NOTIFICATION_ALARM_MILLIS, dueDate.toMillis())
            }.also { intent ->
                PendingIntent.getBroadcast(
                    context,
                    rc,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )?.also { pending ->
                    setAlarmIfInFuture(manager, pending, dueDate.toMillis())
                }
            }
        }
    }

    private fun setAlarmIfInFuture(
        alarm: AlarmManager,
        alarmIntent: PendingIntent,
        triggerMillis: Long
    ) {
        if (triggerMillis > (Date().time)) {
            alarm.setExact(AlarmManager.RTC_WAKEUP, triggerMillis, alarmIntent)
        }
    }

    fun cancelAlarm(context: Context) {
        Log.i(TAG, "Alarm requested to be canceled")
        (context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager)?.let { alarmManager ->
            val intent = Intent(context, AlarmReceiver::class.java)
            val alarmIntent = PendingIntent.getBroadcast(
                context,
                Constants.NOTIFICATION_ID,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            alarmManager.cancel(alarmIntent)
            Log.i(TAG, "Alarm should be canceled")
        }
    }

    fun sendNotification(
        context: Context,
        bitmap: Bitmap?,
        animalName: String,
        randomFact: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID).apply {
            setSmallIcon(R.drawable.ic_notification)
            setContentTitle(context.getString(R.string.notification_title, animalName))
            setContentText(randomFact)

            color = ContextCompat.getColor(context, R.color.green_main)

            if (bitmap != null) {
                setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(bitmap)
                )
            } else {
                setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(randomFact)
                )
            }

            // Intent that will fire when the user taps the notification
            setContentIntent(pendingIntent)
            setAutoCancel(true)
        }

        with(NotificationManagerCompat.from(context)) {
            notify(Constants.NOTIFICATION_ID, builder.build())
        }
    }

    fun setupNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)?.also { manager ->
                NotificationChannel(
                    Constants.NOTIFICATION_CHANNEL_ID,
                    Constants.CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).also { chan ->
                    chan.description = Constants.CHANNEL_DESCRIPTION
                    chan.enableLights(true)
                    chan.lightColor = ContextCompat.getColor(context, R.color.green_main)
                    chan.vibrationPattern = longArrayOf(500, 500)
                    chan.enableVibration(true)
                    manager.createNotificationChannel(chan)
                }
            }
        }
    }
}
