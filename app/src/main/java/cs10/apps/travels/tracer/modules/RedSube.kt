package cs10.apps.travels.tracer.modules

import android.content.Context
import android.provider.ContactsContract.Data
import cs10.apps.travels.tracer.db.DatabaseFinder
import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.model.Viaje

class RedSube(context: Context) {
    private val df = DatabaseFinder(MiDB.getInstance(context))

    fun getLast2HoursQuantity(viaje: Viaje) : Int {
        return df.countTravels2HoursBefore(viaje, true)
        //return getLast2HoursQuantity(viaje.year, viaje.month, viaje.day, viaje.startHour, viaje.startMinute)
    }

    // Static methods
    companion object {
        fun getPercentageToPay(travelCount: Int) : Int {
            return when (travelCount) {
                0 -> 100
                1 -> 50
                else -> 25
            }
        }
    }
}