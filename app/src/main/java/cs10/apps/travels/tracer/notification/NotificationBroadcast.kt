package cs10.apps.travels.tracer.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationBroadcast : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        NotificationCenter().createAskForFinishedTravelNotification(context)
    }
}