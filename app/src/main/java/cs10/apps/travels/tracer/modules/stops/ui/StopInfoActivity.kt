package cs10.apps.travels.tracer.modules.stops.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import cs10.apps.common.android.ui.FormActivity
import cs10.apps.travels.tracer.Utils
import cs10.apps.travels.tracer.adapter.LocatedArrivalAdapter
import cs10.apps.travels.tracer.databinding.ActivityStopInfoBinding
import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.enums.TransportType
import cs10.apps.travels.tracer.model.joins.ColoredTravel
import cs10.apps.travels.tracer.model.roca.ArriboTren
import cs10.apps.travels.tracer.ui.service.ServiceDetail
import cs10.apps.travels.tracer.ui.stops.StopEditor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

class StopInfoActivity : FormActivity(), OnItemSelectedListener {
    private lateinit var binding: ActivityStopInfoBinding
    private lateinit var stopName: String
    private var hours = emptyList<Int>()
    private var selectedIndex = 0

    private val adapter = LocatedArrivalAdapter(mutableListOf(), false) {
        val intent = Intent(this, ServiceDetail::class.java)
        intent.putExtra("id", it.serviceId)
        intent.putExtra("ramal", it.ramal)
        intent.putExtra("station", this.stopName)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStopInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        // prepare recycler
        val llm = LinearLayoutManager(this)
        binding.recycler.adapter = this.adapter
        binding.recycler.layoutManager = llm

        // receive extras
        this.stopName = intent.getStringExtra("stopName") ?: ""
        binding.toolbarLayout.title = stopName

        when (intent.getIntExtra("type", -1)) {
            TransportType.BUS.ordinal -> showBusAppearance()
            TransportType.TRAIN.ordinal -> showTrainAppearance(this.stopName)
        }

        // fab action
        binding.fab.setOnClickListener {
            val intent = Intent(this, StopEditor::class.java)
            intent.putExtra("stopName", stopName)
            startActivity(intent)
        }
    }

    private fun showTrainAppearance(stopName: String){
        Utils.loadTrainBanner(binding.appbarImage)

        lifecycleScope.launch(Dispatchers.IO){
            val db = MiDB.getInstance(this@StopInfoActivity)
            hours = db.trainsDao().getAvailableHours(stopName);
            val hoursPrintable = hours.map {
                if (it == 0) "12 A.M."
                else if (it < 12) "$it A.M."
                else if (it == 12) "12 P.M."
                else "${it-12} P.M."
            }

            // populate spinner
            val spinnerAdapter = ArrayAdapter(this@StopInfoActivity, android.R.layout.simple_spinner_item, hoursPrintable)
            val currentHour = Calendar.getInstance()[Calendar.HOUR_OF_DAY]
            hours.indexOf(currentHour).let { if (it != 1) selectedIndex = it }

            this.launch(Dispatchers.Main) {
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerHour.adapter = spinnerAdapter
                binding.spinnerHour.onItemSelectedListener = this@StopInfoActivity
                binding.spinnerHour.setSelection(selectedIndex, false)
                showTrainTimetables()
            }
        }
    }

    private fun showBusAppearance(){
        Utils.loadBusBanner(binding.appbarImage)
    }

    private fun showTrainTimetables(){
        val prevCount = adapter.itemCount
        if (prevCount > 0){
            adapter.list = mutableListOf()
            adapter.notifyItemRangeRemoved(0, prevCount)
        }

        lifecycleScope.launch(Dispatchers.IO){
            val db = MiDB.getInstance(this@StopInfoActivity)
            val timetable = db.trainsDao().findArrivalsAt(stopName, hours[selectedIndex])
            val arrivals = mutableListOf<ColoredTravel>()

            timetable.forEach { tren ->
                val end = db.servicioDao().getFinalStation(tren.service)
                val v = ArriboTren()

                v.tipo = 1
                v.ramal = tren.ramal
                v.startHour = tren.hour
                v.startMinute = tren.minute
                v.serviceId = tren.service
                v.nombrePdaFin = Utils.simplify(end.station)
                v.nombrePdaInicio = tren.cabecera
                v.endHour = end.hour
                v.endMinute = end.minute
                v.recorrido = mutableListOf()
                v.recorridoDestino = mutableListOf()

                arrivals.add(v)
            }

            delay(400)

            this.launch(Dispatchers.Main){
                adapter.list = arrivals
                adapter.notifyItemRangeInserted(0, arrivals.size)
            }
        }
    }

    // -------- LISTENERS

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        if (selectedIndex != p2){
            selectedIndex = p2
            showTrainTimetables()
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }
}