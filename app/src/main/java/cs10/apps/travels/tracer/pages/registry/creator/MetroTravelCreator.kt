package cs10.apps.travels.tracer.pages.registry.creator

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.tabs.TabLayout
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.common.components.Dropdown
import cs10.apps.travels.tracer.common.enums.TransportType
import cs10.apps.travels.tracer.databinding.ActivityTravelCreatorBinding
import cs10.apps.travels.tracer.databinding.ContentBusTravelCreatorBinding
import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.model.Viaje
import cs10.apps.travels.tracer.pages.registry.creator.viewmodel.CreatorVM
import cs10.apps.travels.tracer.pages.registry.utils.RedSube.Companion.getPercentageToPay
import cs10.apps.travels.tracer.pages.stops.creator.StopCreator
import cs10.apps.travels.tracer.utils.Utils
import java.util.*
import kotlin.math.roundToInt

class MetroTravelCreator : CommonTravelCreator() {
    private lateinit var content: ContentBusTravelCreatorBinding
    private lateinit var client: FusedLocationProviderClient
    private lateinit var creatorVM: CreatorVM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityTravelCreatorBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        Utils.loadMetroBanner(binding.appbarImage)
        binding.toolbarLayout.title = getString(R.string.new_travel)

        content = binding.contentTravelCreator

        creatorVM = ViewModelProvider(this)[CreatorVM::class.java]
        defineObservers()

        // default config init
        super.setDoneFabBehavior(binding.fab)
        super.setCurrentTime(content.etDate, content.etStartHour, content.redSubeHeader)

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
            startDropdown = Dropdown(content.selectorStartPlace, it) {
                updateStartHour(content.etStartHour)
                updatePrice()
            }
        }

        creatorVM.endParadas.observe(this) {
            endDropdown = Dropdown(content.selectorEndPlace, it) { updatePrice() }
        }

        creatorVM.likelyTravel.observe(this) { lt ->
            if (lt == null) return@observe

            lt.viaje.linea?.let { content.etLine.setText(it.toString()) }
            lt.viaje.ramal?.let { content.etRamal.setText(it) }

            startDropdown.select(lt.startIndex)
            endDropdown.select(lt.endIndex)
        }
    }

    override fun onCheckEntries(viaje: Viaje): Int {
        if (!startDropdown.isValidSelection() || !endDropdown.isValidSelection()) return 6

        val date = content.etDate.text.toString().trim()
        val startHour = content.etStartHour.text.toString().trim()
        val endHour = content.etEndHour.text.toString().trim()
        val peopleCount = content.etPeopleCount.text.toString().trim()
        val price = content.etPrice.text.toString().trim()

        val startPlace = startDropdown.getSelectedItem()
        val endPlace = endDropdown.getSelectedItem()

        if (date.isEmpty() || startHour.isEmpty() || peopleCount.isEmpty()) return 1
        if (startPlace.nombre == endPlace.nombre) return 2

        // mandatory start time
        startHour.split(":").toTypedArray().let {
            if (it.size != 2 || it[0].toIntOrNull() == null || it[1].toIntOrNull() == null) {
                content.etStartHour.error = "Ingrese una hora válida"
                return 3
            }

            viaje.startHour = it[0].toInt()
            viaje.startMinute = it[1].toInt()
        }

        // if user entered end hour...
        if (endHour.isNotEmpty()) {
            endHour.split(":").toTypedArray().let {
                if (it.size != 2 || it[0].toIntOrNull() == null || it[1].toIntOrNull() == null) {
                    content.etEndHour.error = "Dejar vacío o ingresar hora válida"
                    return 3
                }

                viaje.endHour = it[0].toInt()
                viaje.endMinute = it[1].toInt()
            }
        }

        // mandatory date
        date.split("/").toTypedArray().let {
            if (it.size != 3 || it[0].toIntOrNull() == null || it[1].toIntOrNull() == null || it[2].toIntOrNull() == null) {
                content.etDate.error = "Ingrese una fecha válida"
                return 4
            }

            viaje.day = it[0].toInt()
            viaje.month = it[1].toInt()
            viaje.year = it[2].toInt()
        }

        try {
            viaje.tipo = TransportType.METRO.ordinal
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

    private fun updatePrice() {
        if (!startDropdown.isValidSelection() || !endDropdown.isValidSelection()) return

        doInBackground {
            val dao = MiDB.getInstance(applicationContext).viajesDao()
            val maxP = dao.getMaxPrice(startDropdown.getSelectedItem().nombre, endDropdown.getSelectedItem().nombre)

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
}
