package cs10.apps.travels.tracer.ui.lines

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import cs10.apps.common.android.ui.FormActivity
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.Utils
import cs10.apps.travels.tracer.adapter.BusRamalsAdapter
import cs10.apps.travels.tracer.constants.Extras
import cs10.apps.travels.tracer.databinding.ActivityLineDetailsBinding
import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.model.info.BusDayInfo
import cs10.apps.travels.tracer.model.info.BusInfo
import cs10.apps.travels.tracer.model.joins.TravelStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LineDetail : FormActivity(), ColorPickerDialogListener, TabLayout.OnTabSelectedListener {
    private var llm: LinearLayoutManager? = null
    private var number: Int? = null

    // ViewModel
    private lateinit var binding: ActivityLineDetailsBinding

    // Adapter
    private val adapter: BusRamalsAdapter = BusRamalsAdapter(listOf()) {
        val intent = Intent(this, FilteredTravelsActivity::class.java)
        intent.putExtra("number", number!!)

        if (it is BusDayInfo) intent.putExtra(it.getTypeKey(), it.wd)
        else intent.putExtra(it.getTypeKey(), it.getIdentifier())

        startActivity(intent)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLineDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        Utils.loadBusBanner(binding.appbarImage)

        // adapter
        llm = LinearLayoutManager(this)

        // view model
        // serviceVM = ViewModelProvider(this).get(ServiceVM::class.java)

        // UI
        binding.recycler.adapter = adapter
        binding.recycler.layoutManager = llm
        receiveExtras()

        // save button
        binding.fab.setOnClickListener {
            val altName = binding.etAlternativeName.text.toString().trim()

            if (altName.isNotEmpty()) doInBackground {
                val db = MiDB.getInstance(this)
                val line = db.linesDao().getByNumber(number!!) ?: return@doInBackground

                line.name = altName
                db.linesDao().update(line)

                doInForeground { finish() }
            }
        }

        // switch tabs
        binding.lineTabs.addOnTabSelectedListener(this)

        if (adapter.list.isEmpty()) lifecycleScope.launch(Dispatchers.IO) { fillRamalsData() }
    }

    private fun receiveExtras() {
        val number = intent.getIntExtra("number", -1)
        if (number == -1) finish()

        binding.toolbarLayout.title = "Linea $number"
        this.number = number

        doInBackground { findName() }
    }

    private fun findName() {
        val db = MiDB.getInstance(this)
        val line = db.linesDao().getByNumber(number!!)

        doInForeground {
            binding.etAlternativeName.setText(line?.name)
        }
    }

    // ------------------------------ TOP MENU ---------------------------

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_line, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_palette) {
            val dialog = ColorPickerDialog.newBuilder()
            dialog.show(this)
            return true
        } else if (item.itemId == R.id.action_graph) {
            number?.let { n -> openHourStats(n) }
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    // ---------------------------- COLOR PICKER ------------------------

    override fun onColorSelected(dialogId: Int, color: Int) {
        // lineManagerVM.updateColor(rootVM.database.linesDao(), color, rootVM)
        Log.i("COLOR PICKER", "Color selected")

        doInBackground {
            val db = MiDB.getInstance(this)
            val line = db.linesDao().getByNumber(number!!) ?: return@doInBackground
            line.color = color
            db.linesDao().update(line)

            findName()
        }
    }

    override fun onDialogDismissed(dialogId: Int) {}

    // --------------------------------- HOUR STATS -------------------------

    private fun openHourStats(lineNumber: Int){
        val intent = Intent(this, HourStatsActivity::class.java)
        intent.putExtra(Extras.LINE_NUMBER.name, lineNumber)
        startActivity(intent)
    }

    // ------------------------------- TABS ----------------------------------

    override fun onTabSelected(tab: TabLayout.Tab?) {
        tab?.let {
            when (it.position) {
                0 -> doInBackground { fillRamalsData() }
                1 -> lifecycleScope.launch(Dispatchers.IO) { fillDestinationsData() }
                2 -> lifecycleScope.launch(Dispatchers.IO) { fillByDayData() }
                else -> {}
            }
        }
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {

    }

    override fun onTabReselected(tab: TabLayout.Tab?) {

    }

    // About "out": https://stackoverflow.com/questions/44298702/what-is-out-keyword-in-kotlin/45516961#45516961
    private fun sortAndPost(list: MutableList<out BusInfo>) {
        list.sort()

        lifecycleScope.launch(Dispatchers.Main){
            adapter.list = list
            adapter.notifyDataSetChanged()
        }
    }

    private fun calculateSpeed(item: BusInfo, stats: List<TravelStats>) {
        if (stats.isEmpty()) item.speed = null
        else {
            var sum = 0.0
            stats.forEach { stat -> sum += stat.calculateSpeedInKmH() }
            item.speed = sum / stats.size
        }
    }

    private suspend fun fillByDayData() {
        val db = MiDB.getInstance(this)
        val data = db.linesDao().getDayStatsForLine(number!!)

        data.forEach {
            val stats = db.viajesDao().getRecentFinishedTravelsOn(it.wd, number!!)
            calculateSpeed(it, stats)
        }

        sortAndPost(data)
    }

    private suspend fun fillDestinationsData() {
        val db = MiDB.getInstance(this)
        val data = db.linesDao().getDestinationStatsForLine(number!!)

        data.forEach {
            // ramal here is actually the end stop name
            val stats = db.viajesDao().getRecentFinishedTravelsTo(it.nombrePdaFin, number!!)
            calculateSpeed(it, stats)
        }

        sortAndPost(data)
    }

    private fun fillRamalsData() {
        val db = MiDB.getInstance(this)
        val data = db.linesDao().getRamalesFromLine(number!!)

        data.forEach {
            if (it.ramal != null){
                val stats = db.viajesDao().getRecentFinishedTravelsFromRamal(number!!, it.ramal)
                calculateSpeed(it, stats)
            }
        }

        sortAndPost(data)
    }
}