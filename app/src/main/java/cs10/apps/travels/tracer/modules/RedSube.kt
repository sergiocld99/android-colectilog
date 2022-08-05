package cs10.apps.travels.tracer.modules

import android.content.Context
import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.model.Viaje

class RedSube(context: Context) {
    private val db = MiDB.getInstance(context)

    fun getPorcentageToPay(viaje: Viaje) : Double {
        return getPorcentageToPay(viaje.year, viaje.month, viaje.day, viaje.startHour, viaje.startMinute)
    }

    fun getPorcentageToPay(year : Int, month: Int, day: Int, hour: Int, minute: Int) : Double {
        val dao = db.viajesDao()

        return when (dao.last2HoursQuantity(year, month, day, hour, minute)) {
            0 -> 1.00
            1 -> 0.5
            else -> 0.25
        }
    }
}