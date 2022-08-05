package cs10.apps.travels.tracer.modules

import android.content.Context
import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.model.Viaje

class RedSube(context: Context) {
    private val db = MiDB.getInstance(context)

    fun getLast2HoursQuantity(viaje: Viaje) : Int {
        return getLast2HoursQuantity(viaje.year, viaje.month, viaje.day, viaje.startHour, viaje.startMinute)
    }

    fun getLast2HoursQuantity(year : Int, month: Int, day: Int, hour: Int, minute: Int) : Int {
        val dao = db.viajesDao()
        return dao.last2HoursQuantity(year, month, day, hour, minute)
    }
}