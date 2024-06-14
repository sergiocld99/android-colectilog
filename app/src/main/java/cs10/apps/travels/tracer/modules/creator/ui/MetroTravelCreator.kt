package cs10.apps.travels.tracer.modules.creator.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.tabs.TabLayout
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.Utils
import cs10.apps.travels.tracer.databinding.ActivityTravelCreatorBinding
import cs10.apps.travels.tracer.databinding.ContentBusTravelCreatorBinding
import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.enums.TransportType
import cs10.apps.travels.tracer.model.Parada
import cs10.apps.travels.tracer.model.Viaje
import cs10.apps.travels.tracer.modules.RedSube.Companion.getPercentageToPay
import cs10.apps.travels.tracer.modules.creator.viewmodel.CreatorVM
import cs10.apps.travels.tracer.ui.stops.StopCreator
import java.util.*
import kotlin.math.roundToInt

class MetroTravelCreator : CommonTravelCreator() {
    private lateinit var content: ContentBusTravelCreatorBinding
    private lateinit var startAdapter: ArrayAdapter<out Parada>
    private lateinit var endAdapter: ArrayAdapter<out Parada>
    private lateinit var onStartPlaceSelected: AdapterView.OnItemSelectedListener
    private lateinit var onEndPlaceSelected: AdapterView.OnItemSelectedListener
    private lateinit var client: FusedLocationProviderClient
    private var startIndex = 0
    private var endIndex = 0

    private lateinit var creatorVM: CreatorVM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityTravelCreatorBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        Utils.loadMetroBanner(binding.appbarImage)
        binding.toolbarLayout.title = getString(R.string.new_travel)

        content = binding.contentTravelCreator
        onStartPlaceSelected = OnStartPlaceSelected()
        onEndPlaceSelected = OnEndPlaceSelected()

        creatorVM = ViewModelProvider(this)[CreatorVM::class.java]
        defineObservers()

        // default config init
        super.setDoneFabBehavior(binding.fab)
        super.setCurrentTime(content.etDate, content.etStartHour, content.redSubeHeader)
        content.etEndHour.isEnabled = false

        // hide line and ramal
        content.etLine.isVisible = false
        content.etRamal.isVisible = false
        content.tvLine.isVisible = false
        content.tvRamal.isVisible = false

        // order stops by last location
        client = LocationServices.getFusedLocationProviderClient(this)
        getLocation()

        // listeners
        content.etDate.setOnClickListener { createDatePicker() }
        content.priceTabs.addOnTabSelectedListener(TabsListener())

        binding.fabStop.setOnClickListener {
            startActivity(Intent(this, StopCreator::class.java))
        }
    }


    @SuppressLint("MissingPermission")
    private fun getLocation(){
        if (Utils.checkPermissions(this)){
            client.lastLocation.addOnSuccessListener {
                //loadStops(it)
                creatorVM.loadInBackground(it, TransportType.METRO)
            }
        }
    }

    private fun defineObservers(){
        creatorVM.startParadas.observe(this) {
            startAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, it)
            startAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            content.selectorStartPlace.adapter = startAdapter
            content.selectorStartPlace.onItemSelectedListener = onStartPlaceSelected
        }

        creatorVM.endParadas.observe(this) {
            endAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, it)
            endAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            content.selectorEndPlace.adapter = endAdapter
            content.selectorEndPlace.onItemSelectedListener = onEndPlaceSelected
        }

        creatorVM.likelyTravel.observe(this) { lt ->
            if (lt == null) return@observe

            lt.viaje.linea?.let { content.etLine.setText(it.toString()) }
            lt.viaje.ramal?.let { content.etRamal.setText(it) }

            startIndex = lt.startIndex
            endIndex = lt.endIndex

            // update spinners selection
            content.selectorStartPlace.setSelection(startIndex, true)
            content.selectorEndPlace.setSelection(endIndex, true)
        }
    }

    override fun onCheckEntries(viaje: Viaje): Int {
        val startParadas = creatorVM.startParadas.value
        val endParadas = creatorVM.endParadas.value

        if (startParadas.isNullOrEmpty() || endParadas.isNullOrEmpty()) return 6

        val date = content.etDate.text.toString().trim()
        val startHour = content.etStartHour.text.toString().trim()
        val peopleCount = content.etPeopleCount.text.toString().trim()
        val price = content.etPrice.text.toString().trim()

        val startPlace = startParadas[startIndex]
        val endPlace = endParadas[endIndex]

        if (date.isEmpty() || startHour.isEmpty() || peopleCount.isEmpty()) return 1
        if (startPlace.nombre == endPlace.nombre) return 2

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
            viaje.tipo = TransportType.METRO.ordinal
            viaje.startHour = hourParams[0].toInt()
            viaje.startMinute = hourParams[1].toInt()
            viaje.day = dateParams[0].toInt()
            viaje.month = dateParams[1].toInt()
            viaje.year = dateParams[2].toInt()
            viaje.nombrePdaInicio = startPlace.nombre
            viaje.nombrePdaFin = endPlace.nombre
            Utils.setWeekDay(viaje)
            viaje.linea = null
            viaje.ramal = null
            viaje.peopleCount = peopleCount.toInt()
            if (viaje.peopleCount <= 0 || viaje.peopleCount >= 10) return 7
            if (price.isNotEmpty()) viaje.costo = price.toDouble()

            // save rating if defined
            content.ratingBar.rating.let { if (it > 0) viaje.rate = it.roundToInt() }

        } catch (e: Exception) {
            e.printStackTrace()
            return 5
        }

        return 0
    }

    fun updatePrice() {
        val startParadas = creatorVM.startParadas.value
        val endParadas = creatorVM.endParadas.value

        if (startParadas.isNullOrEmpty() || endParadas.isNullOrEmpty()) return

        doInBackground {
            val dao = MiDB.getInstance(applicationContext).viajesDao()
            val maxP = dao.getMaxPrice(startParadas[startIndex].nombre, endParadas[endIndex].nombre)

            doInForeground {
                // remove old ones
                content.priceTabs.removeAllTabs()
                content.priceOptions.isVisible = true

                if (maxP != null) {
                    val price = maxP * getPercentageToPay(redSubeCount) / 100
                    content.etPrice.setText(price.toString())

                    // options
                    content.priceTabs.let {
                        // build new ones
                        val tab1 = it.newTab().apply { text = Utils.priceFormat(price * 0.45) }
                        val tab2 = it.newTab().apply { text = Utils.priceFormat(price) }
                        val tab3 = it.newTab().setText("Otro")

                        // add new ones
                        it.addTab(tab2)     // tarifa normal
                        it.addTab(tab1)     // tarifa social
                        it.addTab(tab3)
                    }
                } else {
                    content.etPrice.text = null
                    content.etPrice.isEnabled = true
                    content.priceOptions.isVisible = false
                }
            }
        }
    }

    override fun onDateSet(day: Int, month: Int, year: Int) {
        content.etDate.setText(Utils.dateFormat(day, month, year))
    }

    // ======================== PRICE TABS ======================== //

    private inner class TabsListener : TabLayout.OnTabSelectedListener {

        override fun onTabSelected(tab: TabLayout.Tab) {
            // called when a tab is selected
            tab.text?.let {
                if (it.toString().lowercase(Locale.getDefault()) == "otro"){
                    content.etPrice.isEnabled = true
                } else {
                    content.etPrice.isEnabled = false
                    content.etPrice.setText(it.toString().substring(1))
                }
            }
        }

        override fun onTabUnselected(tab: TabLayout.Tab) {
            // called when a tab is unselected
        }

        override fun onTabReselected(tab: TabLayout.Tab) {
            // called when a tab is reselected
        }
    }

    // ======================== STOPS SPINNERS ========================== //

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
