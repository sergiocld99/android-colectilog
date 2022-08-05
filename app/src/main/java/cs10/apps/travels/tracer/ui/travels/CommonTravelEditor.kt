package cs10.apps.travels.tracer.ui.travels

import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.floatingactionbutton.FloatingActionButton
import cs10.apps.travels.tracer.databinding.ModuleRedSubeBinding
import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.model.Parada
import cs10.apps.travels.tracer.model.Viaje
import cs10.apps.travels.tracer.modules.RedSube
import kotlinx.coroutines.*

abstract class CommonTravelEditor : AppCompatActivity() {

    protected lateinit var viaje: Viaje
    protected var paradas: List<Parada> = mutableListOf()
    protected lateinit var startAdapter: ArrayAdapter<Parada>
    protected lateinit var endAdapter: ArrayAdapter<Parada>

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
        val travelId = intent.getLongExtra("travelId", -1)
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

            viaje = db.viajesDao().getById(travelId)

            val b = async {
                val count = RedSube(applicationContext).getLast2HoursQuantity(viaje)
                CoroutineScope(Dispatchers.Main).launch{ updateRedSubeHeader(count, moduleRedSubeBinding) }
            }

            awaitAll(a)

            CoroutineScope(Dispatchers.Main).launch {
                setSpinners()
                retrieve()
            }
        }
    }

    abstract fun retrieve()
    abstract fun setSpinners()
    abstract fun onCheckEntries(viaje: Viaje) : Int

    protected fun setFabBehavior(fab: FloatingActionButton) = fab.setOnClickListener { performDone() }

    private fun updateRedSubeHeader(count: Int, moduleRedSubeBinding: ModuleRedSubeBinding){
        moduleRedSubeBinding.root.isVisible = count > 0
        if (count == 0) return

        moduleRedSubeBinding.title.text = when (count) {
            1 -> "Descuento del 50%"
            else -> "Descuento del 25%"
        }

        moduleRedSubeBinding.description.text = when(count){
            1 -> "Se realizó 1 viaje en las últimas 2 horas"
            else -> "Se realizaron 2 viajes en las últimas 2 horas"
        }
    }

    private fun performDone(){
        val result = onCheckEntries(viaje)

        if (result == 0) CoroutineScope(Dispatchers.IO).launch {
            val dao = MiDB.getInstance(applicationContext).viajesDao()
            dao.update(viaje)
            CoroutineScope(Dispatchers.Main).launch { finish() }
        }

        Toast.makeText(applicationContext, messages[result], Toast.LENGTH_LONG).show()
    }
}