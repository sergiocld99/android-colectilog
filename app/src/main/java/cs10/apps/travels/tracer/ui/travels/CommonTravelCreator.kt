package cs10.apps.travels.tracer.ui.travels

import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import cs10.apps.common.android.CSActivity
import cs10.apps.common.android.ui.DatePickerFragment
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.Utils
import cs10.apps.travels.tracer.databinding.ModuleRedSubeBinding
import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.model.Viaje
import cs10.apps.travels.tracer.modules.RedSube.Companion.getPercentageToPay
import java.util.*

abstract class CommonTravelCreator : CSActivity() {

    var redSubeCount = 0

    private val messages = arrayOf(
        "Viaje creado con éxito",
        "Por favor complete los campos para continuar",
        "La parada inicial no puede coincidir con la parada final",
        "Formato de hora incorrecto",
        "Formato de fecha incorrecto",
        "Error general de formato",
        "No hay paradas guardadas"
    )

    override fun setSupportActionBar(toolbar: Toolbar?) {
        super.setSupportActionBar(toolbar)
        if (supportActionBar != null) supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

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
        subeHeader: ModuleRedSubeBinding
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
        doInBackground {
            val count = MiDB.getInstance(this).viajesDao()
                .last2HoursQuantity(year, month, day, hour, minute)

            doInForeground { updateRedSubeHeader(subeHeader, count) }
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
            dao.insert(viaje)

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

    // -------------------------- TOP MENU --------------------------

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}