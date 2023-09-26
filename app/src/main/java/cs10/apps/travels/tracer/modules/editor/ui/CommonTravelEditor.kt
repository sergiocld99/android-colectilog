package cs10.apps.travels.tracer.modules.editor.ui

import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.android.material.floatingactionbutton.FloatingActionButton
import cs10.apps.common.android.ui.DatePickerFragment
import cs10.apps.common.android.ui.FormActivity
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.databinding.ModuleRedSubeBinding
import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.model.Parada
import cs10.apps.travels.tracer.model.Viaje
import cs10.apps.travels.tracer.modules.RedSube
import cs10.apps.travels.tracer.modules.editor.model.MeasuredTravel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

abstract class CommonTravelEditor : FormActivity() {

    protected lateinit var mt: MeasuredTravel
    protected var paradas: List<Parada> = mutableListOf()
    protected lateinit var startAdapter: ArrayAdapter<Parada>
    protected lateinit var endAdapter: ArrayAdapter<Parada>
    private var travelId: Long = -1

    private val messages = arrayOf (
        "Viaje actualizado con éxito",
        "Por favor complete los campos para continuar",
        "La parada inicial no puede coincidir con la parada final",
        "Formato de hora incorrecto",
        "Formato de fecha incorrecto",
        "Error general de formato",
        "No hay paradas guardadas"
    )

    protected fun prepare(onGetParadas: (MiDB) -> List<Parada>, moduleRedSubeBinding: ModuleRedSubeBinding){

        // extra
        travelId = intent.getLongExtra("travelId", -1)
        if (travelId < 0) return

        CoroutineScope(Dispatchers.IO).launch {
            val db = MiDB.getInstance(applicationContext)

            val a = async {
                paradas = onGetParadas.invoke(db)

                // hay que usar el contexto de la actividad, sino no funciona
                startAdapter = ArrayAdapter(this@CommonTravelEditor, android.R.layout.simple_spinner_item, paradas)
                endAdapter = ArrayAdapter(this@CommonTravelEditor, android.R.layout.simple_spinner_item, paradas)
                startAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                endAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }

            val viaje = db.viajesDao().getById(travelId)
            val count = RedSube(applicationContext).getLast2HoursQuantity(viaje)

            // calcular distancia y velocidad
            val td = db.viajesDao().getTravelDistanceFromId(viaje.id)
            this@CommonTravelEditor.mt = MeasuredTravel(viaje, td.distance)

            awaitAll(a)

            CoroutineScope(Dispatchers.Main).launch {
                updateRedSubeHeader(count, moduleRedSubeBinding)
                setSpinners()
                retrieve()
            }
        }
    }

    /**
     * A partir del viaje recuperado de la DB, actualiza los text fields y demas vistas
     */
    abstract fun retrieve()

    abstract fun setSpinners()
    abstract fun onCheckEntries(viaje: Viaje) : Int

    protected fun setFabBehavior(fab: FloatingActionButton) = fab.setOnClickListener { performDone() }

    private fun updateRedSubeHeader(count: Int, moduleRedSubeBinding: ModuleRedSubeBinding){
        moduleRedSubeBinding.root.isVisible = count > 0
        if (count == 0) return

        moduleRedSubeBinding.title.text = when (count) {
            1 -> getString(R.string.you_pay_percent, 50)
            else -> getString(R.string.you_pay_percent, 25)
        }

        moduleRedSubeBinding.description.text = when(count){
            1 -> "Se realizó 1 viaje en las últimas 2 horas"
            else -> "Se realizaron 2 viajes en las últimas 2 horas"
        }
    }

    private fun performDone(){
        val result = onCheckEntries(getViaje())

        if (result == 0) CoroutineScope(Dispatchers.IO).launch {
            val dao = MiDB.getInstance(applicationContext).viajesDao()
            dao.update(getViaje())
            CoroutineScope(Dispatchers.Main).launch { finish() }
        }

        Toast.makeText(applicationContext, messages[result], Toast.LENGTH_LONG).show()
    }

    fun getViaje(): Viaje {
        return mt.viaje
    }

    // ---------------------- PICKER FRAGMENTS --------------------

    fun createDatePicker(){
        val viaje = getViaje()
        val picker = DatePickerFragment(viaje.day, viaje.month, viaje.year) { day, month, year ->
            onDateSet(day, month, year)
            viaje.day = day
            viaje.month = month
            viaje.year = year
        }

        picker.show(supportFragmentManager, "DatePicker")
    }

    abstract fun onDateSet(day: Int, month: Int, year: Int)

    // -------------------------- TOP MENU ------------------

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_travel_editor, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_delete) {
            doInBackground {
                val db = MiDB.getInstance(this)
                db.viajesDao().delete(travelId)

                doInForeground {
                    Toast.makeText(this, "Viaje eliminado con éxito", Toast.LENGTH_LONG).show()
                    finish()
                }
            }

            return true
        }
        
        return super.onOptionsItemSelected(item)
    }
}