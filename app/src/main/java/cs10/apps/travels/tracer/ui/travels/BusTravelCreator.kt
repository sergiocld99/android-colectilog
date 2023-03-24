package cs10.apps.travels.tracer.ui.travels

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.Utils
import cs10.apps.travels.tracer.databinding.ActivityTravelCreatorBinding
import cs10.apps.travels.tracer.databinding.ContentBusTravelCreatorBinding
import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.model.Parada
import cs10.apps.travels.tracer.model.Viaje
import cs10.apps.travels.tracer.modules.RedSube.Companion.getPercentageToPay
import cs10.apps.travels.tracer.ui.stops.StopCreator
import java.util.*
import kotlin.math.roundToInt

class BusTravelCreator : CommonTravelCreator() {
    private lateinit var content: ContentBusTravelCreatorBinding
    private lateinit var startAdapter: ArrayAdapter<out Parada>
    private lateinit var endAdapter: ArrayAdapter<out Parada>
    private lateinit var onStartPlaceSelected: AdapterView.OnItemSelectedListener
    private lateinit var onEndPlaceSelected: AdapterView.OnItemSelectedListener
    private lateinit var client: FusedLocationProviderClient
    private var paradas: MutableList<Parada> = mutableListOf()
    private var startIndex = 0
    private var endIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityTravelCreatorBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        Utils.loadBusBanner(binding.appbarImage)
        binding.toolbarLayout.title = getString(R.string.new_travel)

        content = binding.contentTravelCreator
        onStartPlaceSelected = OnStartPlaceSelected()
        onEndPlaceSelected = OnEndPlaceSelected()

        // default config init
        super.setDoneFabBehavior(binding.fab)
        super.setCurrentTime(content.etDate, content.etStartHour, content.redSubeHeader)
        content.etEndHour.isEnabled = false

        // hint values
        autoFillRamals()

        // order stops by last location
        client = LocationServices.getFusedLocationProviderClient(this)
        getLocation()

        // listeners
        content.etDate.setOnClickListener { createDatePicker() }
        binding.fabStop.setOnClickListener {
            startActivity(Intent(this, StopCreator::class.java))
        }
    }


    @SuppressLint("MissingPermission")
    private fun getLocation(){
        if (Utils.checkPermissions(this)){
            client.lastLocation.addOnSuccessListener {
                loadStops(it)
            }
        }
    }

    private fun autoFillRamals() {
        doInBackground {
            val ramals = MiDB.getInstance(this).viajesDao().allRamals

            doInForeground {
                val ra = ArrayAdapter(this, android.R.layout.simple_list_item_1, ramals)
                content.etRamal.setAdapter(ra)
            }
        }
    }

    private fun loadStops(location: Location?) {
        doInBackground {
            val db = MiDB.getInstance(this)
            paradas = db.paradasDao().all

            location?.let { Utils.orderByProximity(paradas, it.latitude, it.longitude) }
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
        viaje.linea?.let { content.etLine.setText(it.toString()) }
        viaje.ramal?.let { content.etRamal.setText(it) }

        // find end index
        var endIndex = 0
        for (p in paradas) {
            if (p.nombre == viaje.nombrePdaFin) break else endIndex++
        }

        content.selectorEndPlace.setSelection(endIndex, true)
    }

    private fun setSpinners() {
        startAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, paradas)
        endAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, paradas)

        startAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        endAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        content.selectorStartPlace.adapter = startAdapter
        content.selectorEndPlace.adapter = endAdapter
        content.selectorStartPlace.onItemSelectedListener = onStartPlaceSelected
        content.selectorEndPlace.onItemSelectedListener = onEndPlaceSelected
    }

    override fun onCheckEntries(viaje: Viaje): Int {
        if (paradas.isEmpty()) return 6

        val line = content.etLine.text.toString().trim()
        val ramal = content.etRamal.text.toString().trim()
        val date = content.etDate.text.toString().trim()
        val startHour = content.etStartHour.text.toString().trim()
        val peopleCount = content.etPeopleCount.text.toString().trim()
        val price = content.etPrice.text.toString().trim()

        val startPlace = paradas[startIndex]
        val endPlace = paradas[endIndex]

        if (date.isEmpty() || startHour.isEmpty() || line.isEmpty() || peopleCount.isEmpty()) return 1
        if (startPlace == endPlace) return 2

        val hourParams = startHour.split(":").toTypedArray()
        if (hourParams.size != 2) {
            content.etStartHour.error = "Ingrese una hora válida"
            return 3
        }

        val dateParams = date.split("/").toTypedArray()
        if (dateParams.size != 3) {
            content.etDate.error = "Ingrese una fecha válida"
            return 4
        }

        try {
            viaje.startHour = hourParams[0].toInt()
            viaje.startMinute = hourParams[1].toInt()
            viaje.day = dateParams[0].toInt()
            viaje.month = dateParams[1].toInt()
            viaje.year = dateParams[2].toInt()
            viaje.nombrePdaInicio = startPlace.nombre
            viaje.nombrePdaFin = endPlace.nombre
            Utils.setWeekDay(viaje)
            viaje.linea = line.toInt()
            viaje.peopleCount = peopleCount.toInt()
            if (viaje.peopleCount <= 0 || viaje.peopleCount >= 10) return 7
            if (price.isNotEmpty()) viaje.costo = price.toDouble()
            if (ramal.isNotEmpty()) viaje.ramal = ramal

            // save rating if defined
            content.ratingBar.rating.let { if (it > 0) viaje.rate = it.roundToInt() }

        } catch (e: Exception) {
            e.printStackTrace()
            return 5
        }

        return 0
    }

    fun updatePrice() {
        if (paradas.isNotEmpty()) doInBackground {
            val dao = MiDB.getInstance(applicationContext).viajesDao()
            val maxP = dao.getMaxPrice(paradas[startIndex].nombre, paradas[endIndex].nombre)
            doInForeground {
                if (maxP != null) {
                    val price = maxP * getPercentageToPay(redSubeCount) / 100
                    content.etPrice.setText(price.toString())
                } else content.etPrice.text = null
            }
        }
    }

    override fun onDateSet(day: Int, month: Int, year: Int) {
        content.etDate.setText(Utils.dateFormat(day, month, year))
    }

    private inner class OnStartPlaceSelected : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, l: Long) {
            startIndex = i
            updatePrice()
        }

        override fun onNothingSelected(adapterView: AdapterView<*>?) {}
    }

    private inner class OnEndPlaceSelected : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, l: Long) {
            endIndex = i
            updatePrice()
        }

        override fun onNothingSelected(adapterView: AdapterView<*>?) {}
    }
}