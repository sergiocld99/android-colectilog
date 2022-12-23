package cs10.apps.travels.tracer.notification

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import cs10.apps.travels.tracer.DrawerActivity
import cs10.apps.travels.tracer.R

class NotificationCenter {
    companion object {
        const val CHANNEL_ID = "TravelTracer"
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
            notify(1, builder.build())
        }
    }

}