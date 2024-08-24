package cs10.apps.travels.tracer.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationBroadcast : BroadcastReceiver() {
    companion object {
        const val NOTIFICATION_ID_KEY = "notification-id"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val id = intent?.getIntExtra(NOTIFICATION_ID_KEY, 0)

        id?.let {
            val nc = NotificationCenter()

            when (it) {
                NotificationCenter.ASK_FINISH_NOTIF_ID -> nc.createAskForFinishedTravelNotification(context, "El viaje está por terminar. Puedes finalizarlo por adelantado desde la sección \"En vivo\"")
                NotificationCenter.NOTIFY_OTHERS_ABOUT_ARRIVAL -> nc.createAskForFinishedTravelNotification(context, "No olvides avisar que llegaste!")
                //NotificationCenter.BALANCE_SUMMARY_ID -> nc.createBalanceSummaryNotification(context)
            }
        }
    }
}