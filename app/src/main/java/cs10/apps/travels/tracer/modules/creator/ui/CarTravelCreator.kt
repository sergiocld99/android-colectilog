package cs10.apps.travels.tracer.modules.creator.ui

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.Utils
import cs10.apps.travels.tracer.databinding.ActivityTrainTravelCreatorBinding
import cs10.apps.travels.tracer.databinding.ContentTrainTravelCreatorBinding
import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.enums.TransportType
import cs10.apps.travels.tracer.model.Parada
import cs10.apps.travels.tracer.model.Viaje
import java.util.*

class CarTravelCreator : CommonTravelCreator() {
    private lateinit var content: ContentTrainTravelCreatorBinding
    private lateinit var startAdapter: ArrayAdapter<out Parada>
    private lateinit var endAdapter: ArrayAdapter<out Parada>
    private lateinit var onStartPlaceSelected: AdapterView.OnItemSelectedListener
    private lateinit var onEndPlaceSelected: AdapterView.OnItemSelectedListener
    private lateinit var client: FusedLocationProviderClient
    private var paradas = mutableListOf<Parada>()
    private var endParadas = mutableListOf<Parada>()
    private var startIndex = 0
    private var endIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityTrainTravelCreatorBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        Utils.loadBusBanner(binding.appbarImage)
        binding.toolbarLayout.title = getString(R.string.new_travel_by_car)

        content = binding.content
        onStartPlaceSelected = OnStartPlaceSelected()
        onEndPlaceSelected = OnEndPlaceSelected()

        // default config init
        super.setDoneFabBehavior(binding.fab)
        super.setCurrentTime(content.etDate, content.etStartHour, null)
        content.etPrice.setText(String.format("0.00"))

        // order stops by last location
        client = LocationServices.getFusedLocationProviderClient(this)
        getLocation()

        // listeners
        content.etDate.setOnClickListener { createDatePicker() }

        /*
        binding.fabStop.setOnClickListener {
            startActivity(Intent(this, StopCreator::class.java))
        }*/
    }


    @SuppressLint("MissingPermission")
    private fun getLocation(){
        if (Utils.checkPermissions(this)){
            client.lastLocation.addOnSuccessListener {
                loadStops(it)
            }
        }
    }

    private fun loadStops(location: Location?) {
        doInBackground {
            val db = MiDB.getInstance(this)

            // get ordered by name and travel count
            paradas = db.paradasDao().all
            endParadas = db.paradasDao().allOrderedByTravelCount

            // control
            if (paradas.size != endParadas.size) throw Exception("Paradas count dismatch")

            // order start
            location?.let { Utils.orderByProximity(paradas, it.latitude, it.longitude) }

            // update spinners
            doInForeground { setSpinners() }

            // part 2: autocomplete likely travel
            val currentHour = Calendar.getInstance()[Calendar.HOUR_OF_DAY]
            if (paradas.isNotEmpty()) {
                val viaje = db.viajesDao().getLikelyTravelFrom(paradas.first().nombre, currentHour)
                viaje?.let { runOnUiThread { autoFillLikelyTravel(it) } }
            }
        }
    }

    private fun autoFillLikelyTravel(viaje: Viaje) {

        // find end index
        var endIndex = 0
        for (p in endParadas) {
            if (p.nombre == viaje.nombrePdaFin) break else endIndex++
        }

        content.selectorEndPlace.setSelection(endIndex, true)
    }

    private fun setSpinners() {
        startAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, paradas)
        endAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, endParadas)

        startAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        endAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        content.selectorStartPlace.adapter = startAdapter
        content.selectorEndPlace.adapter = endAdapter
        content.selectorStartPlace.onItemSelectedListener = onStartPlaceSelected
        content.selectorEndPlace.onItemSelectedListener = onEndPlaceSelected
    }

    override fun onCheckEntries(viaje: Viaje): Int {
        if (paradas.isEmpty()) return 6

        val date = content.etDate.text.toString().trim()
        val startHour = content.etStartHour.text.toString().trim()
        val endHour = content.etEndHour.text.toString().trim()
        val peopleCount = content.etPeopleCount.text.toString().trim()
        val price = content.etPrice.text.toString().trim()

        val startPlace = paradas[startIndex]
        val endPlace = endParadas[endIndex]

        if (date.isEmpty() || startHour.isEmpty() || peopleCount.isEmpty()) return 1
        if (startPlace == endPlace) return 2

        val startTimeParams = startHour.split(":").toTypedArray()
        if (startTimeParams.size != 2) {
            content.etStartHour.error = "Ingrese una hora válida"
            return 3
        }

        val endTimeParams = endHour.split(":").toTypedArray()
        if (endHour.isNotEmpty() && endTimeParams.size != 2) {
            content.etEndHour.error = "Ingrese hora valida o deje vacío"
            return 3
        }

        val dateParams = date.split("/").toTypedArray()
        if (dateParams.size != 3) {
            content.etDate.error = "Ingrese una fecha válida"
            return 4
        }

        try {
            viaje.tipo = TransportType.CAR.ordinal
            viaje.nombrePdaInicio = startPlace.nombre
            viaje.nombrePdaFin = endPlace.nombre
            viaje.peopleCount = peopleCount.toInt()
            if (viaje.peopleCount <= 0 || viaje.peopleCount >= 10) return 7
            if (price.isNotEmpty()) viaje.costo = price.toDouble()

            startTimeParams.also {
                viaje.startHour = it[0].toInt()
                viaje.startMinute = it[1].toInt()
            }

            endTimeParams.also {
                if (it.size == 2){
                    viaje.endHour = it[0].toInt()
                    viaje.endMinute = it[1].toInt()
                }
            }

            dateParams.also {
                viaje.day = it[0].toInt()
                viaje.month = it[1].toInt()
                viaje.year = it[2].toInt()
            }

            Utils.setWeekDay(viaje)

            // save rating if defined
            // content.ratingBar.rating.let { if (it > 0) viaje.rate = it.roundToInt() }

        } catch (e: Exception) {
            e.printStackTrace()
            return 5
        }

        return 0
    }


    override fun onDateSet(day: Int, month: Int, year: Int) {
        content.etDate.setText(Utils.dateFormat(day, month, year))
    }

    // ======================== STOPS SPINNERS ========================== //

    private inner class OnStartPlaceSelected : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, l: Long) {
            startIndex = i
            // updatePrice()
        }

        override fun onNothingSelected(adapterView: AdapterView<*>?) {}
    }

    private inner class OnEndPlaceSelected : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, l: Long) {
            endIndex = i
            // updatePrice()
        }

        override fun onNothingSelected(adapterView: AdapterView<*>?) {}
    }
}
