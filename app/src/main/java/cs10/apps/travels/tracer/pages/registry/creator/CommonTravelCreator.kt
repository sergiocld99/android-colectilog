package cs10.apps.travels.tracer.pages.registry.creator

import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import cs10.apps.common.android.ui.DatePickerFragment
import cs10.apps.common.android.ui.FormActivity
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.utils.Utils
import cs10.apps.travels.tracer.common.constants.ResultCodes
import cs10.apps.travels.tracer.databinding.ModuleRedSubeBinding
import cs10.apps.travels.tracer.db.DatabaseFinder
import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.model.Viaje
import cs10.apps.travels.tracer.pages.registry.utils.RedSube.Companion.getPercentageToPay
import cs10.apps.travels.tracer.notification.NotificationCenter
import java.util.Calendar

abstract class CommonTravelCreator : FormActivity() {

    var redSubeCount = 0

    private val messages = arrayOf(
        "Viaje creado con éxito",
        "Por favor complete los campos para continuar",
        "La parada inicial no puede coincidir con la parada final",
        "Formato de hora incorrecto",
        "Formato de fecha incorrecto",
        "Error general de formato",
        "No hay paradas guardadas",
        "La cantidad de pasajeros debe estar entre 1 y 9"
    )

    protected fun setDoneFabBehavior(fab: FloatingActionButton) {
        fab.setOnClickListener { performDone() }
    }

    private fun updateRedSubeHeader(moduleRedSubeBinding: ModuleRedSubeBinding, count: Int) {
        if (count == 0) {
            moduleRedSubeBinding.root.visibility = View.GONE
            return
        }
        redSubeCount = count
        val ptp = getPercentageToPay(count)
        moduleRedSubeBinding.root.visibility = View.VISIBLE
        moduleRedSubeBinding.title.text = getString(R.string.you_pay_percent, ptp)
        moduleRedSubeBinding.description.text =
            if (count == 1) "Se realizó 1 viaje en las últimas 2 horas"
            else "Se realizaron $count viajes en las últimas 2 horas"
    }

    protected fun setCurrentTime(
        etDate: EditText,
        etStartHour: EditText,
        subeHeader: ModuleRedSubeBinding?
    ) {
        // set today values
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE, -1)
        val day = calendar[Calendar.DAY_OF_MONTH]
        val month = calendar[Calendar.MONTH] + 1
        val year = calendar[Calendar.YEAR]
        val hour = calendar[Calendar.HOUR_OF_DAY]
        val minute = calendar[Calendar.MINUTE]
        etDate.setText(Utils.dateFormat(day, month, year))
        etStartHour.setText(Utils.hourFormat(hour, minute))

        // sube header
        subeHeader?.let {
           doInBackground {
               val df = DatabaseFinder(MiDB.getInstance(this))
               val count = df.countTravelsLast2Hours(true)
               doInForeground { updateRedSubeHeader(it, count) }
           }
        }
    }

    private fun getMessage(index: Int): String {
        return messages[index]
    }

    private fun performDone() {
        val viaje = Viaje()
        val result = onCheckEntries(viaje)

        if (result == 0) doInBackground {
            val dao = MiDB.getInstance(this).viajesDao()

            // Update DEC 2022: now you can create travels of +1 people
            for (i in 1..viaje.peopleCount) dao.insert(viaje)

            // create notification (only if travel is unfinished and day equals current)
            val today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

            if (viaje.endHour == null && viaje.day == today) with(NotificationCenter()){
                createChannel(this@CommonTravelCreator)
                createNewStartedTravelNotification(this@CommonTravelCreator)
                setResult(ResultCodes.OPEN_LIVE_FRAGMENT)
            }

            doInForeground { finish() }
        }

        Toast.makeText(applicationContext, getMessage(result), Toast.LENGTH_LONG).show()
    }

    abstract fun onCheckEntries(viaje: Viaje): Int

    // ---------------------- PICKER FRAGMENTS --------------------

    fun createDatePicker(){
        val c = Calendar.getInstance()
        val cDay = c[Calendar.DAY_OF_MONTH]
        val cMonth = c[Calendar.MONTH]
        val cYear = c[Calendar.YEAR]

        val picker = DatePickerFragment(cDay, cMonth+1, cYear) { day, month, year ->
            onDateSet(day, month, year)
        }

        picker.show(supportFragmentManager, "DatePicker")
    }

    abstract fun onDateSet(day: Int, month: Int, year: Int)

}