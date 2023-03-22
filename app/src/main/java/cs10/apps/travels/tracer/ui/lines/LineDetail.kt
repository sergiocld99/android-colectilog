package cs10.apps.travels.tracer.ui.lines

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import cs10.apps.common.android.ui.FormActivity
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.Utils
import cs10.apps.travels.tracer.adapter.BusRamalsAdapter
import cs10.apps.travels.tracer.constants.Extras
import cs10.apps.travels.tracer.databinding.ActivityLineDetailsBinding
import cs10.apps.travels.tracer.db.MiDB

class LineDetail : FormActivity(), ColorPickerDialogListener {
    private var llm: LinearLayoutManager? = null
    private var number: Int? = null

    // ViewModel
    private lateinit var binding: ActivityLineDetailsBinding

    // Adapter
    private val adapter: BusRamalsAdapter = BusRamalsAdapter(listOf()) {
        val intent = Intent(this, FilteredTravelsActivity::class.java)
        intent.putExtra("number", number!!)
        intent.putExtra("ramal", it.ramal)
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
    }

    private fun receiveExtras() {
        val number = intent.getIntExtra("number", -1)
        if (number == -1) finish()

        binding.toolbarLayout.title = "Linea $number"
        this.number = number

        doInBackground { fillData() }
    }

    private fun fillData() {
        val db = MiDB.getInstance(this)
        val data = db.linesDao().getRamalesFromLine(number!!)
        val line = db.linesDao().getByNumber(number!!)

        data.forEach {
            if (it.ramal != null){
                val stats = db.viajesDao().getRecentFinishedTravelsFromRamal(number!!, it.ramal)

                if (stats.isEmpty()) it.speed = null
                else {
                    var sum = 0.0

                    stats.forEach { stat -> sum += stat.calculateSpeedInKmH() }
                    it.speed = sum / stats.size
                }
            }
        }

        data.sort()

        doInForeground {
            binding.etAlternativeName.setText(line?.name)
            adapter.list = data
            adapter.notifyDataSetChanged()
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

            fillData()
        }
    }

    override fun onDialogDismissed(dialogId: Int) {}

    // --------------------------------- HOUR STATS -------------------------

    private fun openHourStats(lineNumber: Int){
        val intent = Intent(this, HourStatsActivity::class.java)
        intent.putExtra(Extras.LINE_NUMBER.name, lineNumber)
        startActivity(intent)
    }
}