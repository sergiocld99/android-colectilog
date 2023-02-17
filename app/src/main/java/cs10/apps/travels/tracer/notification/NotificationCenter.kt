package cs10.apps.travels.tracer.notification

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import cs10.apps.travels.tracer.DrawerActivity
import cs10.apps.travels.tracer.R
import java.util.*

class NotificationCenter {
    companion object {
        const val CHANNEL_ID = "TravelTracer"
        const val NEW_TRAVEL_NOTIF_ID = 1
        const val ASK_FINISH_NOTIF_ID = 2
    }

    fun createChannel(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Travel Tracer Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            channel.apply { description = "Canal de notificaciones de Travel Tracer" }

            with(activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager){
                createNotificationChannel(channel)
            }
        }
    }

    fun scheduleAskNotification(context: Context, delayMs: Long){
        val intent = Intent(context.applicationContext, NotificationBroadcast::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context.applicationContext,
            ASK_FINISH_NOTIF_ID,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            Calendar.getInstance().timeInMillis + delayMs,
            pendingIntent
        )

    }


    /** -------------------------- custom notifications --------------------- **/

    fun createNewStartedTravelNotification(context: Context){
        // create intent to open screen when user touches notification
        val intent = Intent(context, DrawerActivity::class.java)
        intent.apply {
            putExtra("openLive", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // create pending intent that uses previous created intent
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        else 0

        val pendingIntent = PendingIntent.getActivity(context, 0, intent, flag)

        // build notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        builder.apply {
            setContentTitle("Nuevo viaje comenzado")
            setContentText("Puede hacer un seguimiento del mismo a través de la sección \"En vivo\"")
            setSmallIcon(R.drawable.ic_bus)
            setContentIntent(pendingIntent)
            setAutoCancel(true)
            priority = NotificationCompat.PRIORITY_DEFAULT
        }

        // show notification
        with(NotificationManagerCompat.from(context)){
            notify(NEW_TRAVEL_NOTIF_ID, builder.build())
        }
    }

    fun createAskForFinishedTravelNotification(context: Context){
        // create intent to open screen when user touches notification
        val intent = Intent(context, DrawerActivity::class.java)
        intent.apply {
            putExtra("openLive", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // create pending intent that uses previous created intent
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        else 0

        val pendingIntent = PendingIntent.getActivity(context, 0, intent, flag)

        // build notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        builder.apply {
            setContentTitle("Posible viaje terminado")
            setContentText("Ya pasó el tiempo estimado de llegada. " +
                    "Marca el viaje como finalizado desde la sección \"En vivo\"")
            setSmallIcon(R.drawable.ic_bus)
            setContentIntent(pendingIntent)
            setAutoCancel(true)
            priority = NotificationCompat.PRIORITY_DEFAULT
        }

        val notif = builder.build()
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(ASK_FINISH_NOTIF_ID, notif)
    }

}