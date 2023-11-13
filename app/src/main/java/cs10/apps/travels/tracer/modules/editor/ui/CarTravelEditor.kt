package cs10.apps.travels.tracer.modules.editor.ui

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.core.view.isVisible
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.Utils
import cs10.apps.travels.tracer.databinding.ActivityTrainTravelEditorBinding
import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.enums.TransportType
import cs10.apps.travels.tracer.model.Parada
import cs10.apps.travels.tracer.model.Viaje
import cs10.apps.travels.tracer.utils.SafeUtils

class CarTravelEditor : CommonTravelEditor() {

    // podemos usar la misma vista que para trenes
    private lateinit var binding: ActivityTrainTravelEditorBinding

    // indices
    private var startIndex = 0
    private var endIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTrainTravelEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // top image and title
        setSupportActionBar(binding.toolbar)
        binding.toolbarLayout.title = getString(R.string.edit_travel)
        Utils.loadBusBanner(binding.appbarImage)

        // content
        with (binding.contentTravelCreator){
            this.etPrice.isEnabled = false
            this.etPrice.isEnabled = false
            this.tvPrice.isVisible = false
            this.etPrice.isVisible = false
            this.tvTimes.isVisible = false
            this.etPeopleCount.isVisible = false
        }

        // fab
        super.setFabBehavior(binding.fab)

        // load from intent
        super.prepare({ loadStops(it) } , binding.contentTravelCreator.redSubeHeader)
    }

    private fun loadStops(db: MiDB): List<Parada> = db.paradasDao().all

    override fun retrieve(){

        // encontrar viaje
        val v = getViaje()

        // distance and speed info
        binding.distanceText.text = getString(R.string.linear_distance_km, mt.distanceKm)
        binding.speedText.text = getString(R.string.speed_kmh, mt.getSpeedKmH())

        // fecha y hora
        binding.contentTravelCreator.etDate.setText(Utils.dateFormat(v.day, v.month, v.year))
        binding.contentTravelCreator.etStartHour.setText(Utils.hourFormat(v.startHour, v.startMinute))
        binding.contentTravelCreator.etEndHour.setText(SafeUtils.hourFormat(v.endHour, v.endMinute))

        // inicio y fin
        startIndex = paradas.indexOfFirst { it.nombre == v.nombrePdaInicio }
        endIndex = paradas.indexOfFirst { it.nombre == v.nombrePdaFin }
        binding.contentTravelCreator.selectorStartPlace.setSelection(startIndex)
        binding.contentTravelCreator.selectorEndPlace.setSelection(endIndex)
    }

    override fun setSpinners() {
        binding.contentTravelCreator.selectorStartPlace.adapter = startAdapter
        binding.contentTravelCreator.selectorEndPlace.adapter = endAdapter

        // listeners de selectores...
        binding.contentTravelCreator.selectorStartPlace.onItemSelectedListener = onStartPlaceSelect
        binding.contentTravelCreator.selectorEndPlace.onItemSelectedListener = onEndPlaceSelect
    }

    override fun onCheckEntries(viaje: Viaje): Int {
        if (paradas.isEmpty()) return 6
        if (startIndex == endIndex) return 2

        // extraer inputs
        with(binding.contentTravelCreator) {
            val date = this.etDate.text.toString().trim()
            val startTime = this.etStartHour.text.toString().trim()
            val endTime = this.etEndHour.text.toString().trim()
            if (date.isEmpty() || startTime.isEmpty()) return 1

            startTime.split(":").let {
                if (it.size != 2 || it.contains("")) {
                    this.etStartHour.error = "Ingrese una hora válida"
                    return 3
                } else {
                    viaje.startHour = it[0].toInt()
                    viaje.startMinute = it[1].toInt()
                }
            }

            if (endTime.isNotEmpty()) endTime.split(":").let {
                if (it.size != 2 || it.contains("")){
                    this.etEndHour.error = "Ingrese una hora válida"
                    return 3
                } else {
                    viaje.endHour = it[0].toInt()
                    viaje.endMinute = it[1].toInt()
                }
            } else {
                viaje.endHour = null
                viaje.endMinute = null
            }

            date.split("/").let {
                if (it.size != 3 || it.contains("")){
                    this.etDate.error = "Ingrese una fecha válida"
                    return 4
                } else {
                    viaje.day = it[0].toInt()
                    viaje.month = it[1].toInt()
                    viaje.year = it[2].toInt()
                    Utils.setWeekDay(viaje)
                }
            }

            // actualizar resto de atributos
            viaje.nombrePdaInicio = paradas[startIndex].nombre
            viaje.nombrePdaFin = paradas[endIndex].nombre
            viaje.tipo = TransportType.CAR.ordinal
        }

        return 0
    }

    override fun onDateSet(day: Int, month: Int, year: Int) {
        binding.contentTravelCreator.etDate.setText(Utils.dateFormat(day, month, year))
    }

    private val onStartPlaceSelect = object : OnItemSelectedListener {
        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
            startIndex = p2
        }

        override fun onNothingSelected(p0: AdapterView<*>?) {
            // do nothing
        }
    }

    private val onEndPlaceSelect = object : OnItemSelectedListener {
        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
            endIndex = p2
        }

        override fun onNothingSelected(p0: AdapterView<*>?) {
            // do nothing
        }
    }
}