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
                NotificationCenter.ASK_FINISH_NOTIF_ID -> nc.createAskForFinishedTravelNotification(context)
                NotificationCenter.BALANCE_SUMMARY_ID -> nc.createBalanceSummaryNotification(context)
            }
        }
    }
}