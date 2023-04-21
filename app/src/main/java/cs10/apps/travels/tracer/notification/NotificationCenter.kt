package cs10.apps.travels.tracer.notification

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import cs10.apps.common.CanFail
import cs10.apps.travels.tracer.DrawerActivity
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.db.MiDB
import java.util.*
import java.util.concurrent.TimeUnit

class NotificationCenter {
    companion object {
        const val CHANNEL_ID = "TravelTracer"
        const val NEW_TRAVEL_NOTIF_ID = 1
        const val ASK_FINISH_NOTIF_ID = 2
        const val BALANCE_SUMMARY_ID = 3
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

    private fun scheduleNotification(context: Context, delayMs: Long, id: Int){
        val intent = Intent(context.applicationContext, NotificationBroadcast::class.java)
        intent.putExtra(NotificationBroadcast.NOTIFICATION_ID_KEY, id)

        val pendingIntent = PendingIntent.getBroadcast(
            context.applicationContext,
            0,
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

    fun scheduleAskNotification(context: Context, delayMs: Long){
        scheduleNotification(context, delayMs, ASK_FINISH_NOTIF_ID)
    }

    /**
     * Schedules a notification that shows your current balance at 11 PM. If the notification
     * is already scheduled for today, it does nothing
     */
    fun scheduleBalanceSummary(context: Context) {
        val c = Calendar.getInstance()
        // val h = c[Calendar.HOUR_OF_DAY]
        val currentLd = c[Calendar.DAY_OF_MONTH]

        val prefs = context.getSharedPreferences("notif-cache", Context.MODE_PRIVATE)
        val lastDay = prefs.getInt("ld", -1)

        if (lastDay != currentLd){
            val delayMs = TimeUnit.HOURS.toMillis(3)
            scheduleNotification(context, delayMs, BALANCE_SUMMARY_ID)
            prefs.edit().putInt("ld", currentLd).apply()
        }
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

    // -------------------------  CALLED BY BROADCAST --------------------------------

    @CanFail
    fun createBalanceSummaryNotification(context: Context){
        val prefs = context.getSharedPreferences("balance", Context.MODE_PRIVATE)
        val travelId = prefs.getLong("travelId", 0)
        val coffeeId = prefs.getLong("coffeeId", 0)
        val chargeId = prefs.getLong("chargeId", 0)
        val savedBalance = prefs.getFloat("balance", 0f)

        Thread {
            val database = MiDB.getInstance(context)
            val sinceBuses = database.viajesDao().getSpentInBusesSince(travelId)
            val sinceTrains = database.viajesDao().getSpentInTrainsSince(travelId)
            val sinceCoffee = database.coffeeDao().getSpentSince(coffeeId)
            val charges = database.recargaDao().getTotalChargedSince(chargeId)
            val money = (savedBalance - sinceBuses - sinceTrains - sinceCoffee + charges).toInt()

            // build notification (allowed from any thread)
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            builder.apply {
                setContentTitle("Informe de saldo")
                setContentText(context.getString(R.string.current_money_info, money))
                setSmallIcon(R.drawable.ic_savings)
                setAutoCancel(true)
                priority = NotificationCompat.PRIORITY_DEFAULT
            }

            val notif = builder.build()
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(ASK_FINISH_NOTIF_ID, notif)
        }.start()
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