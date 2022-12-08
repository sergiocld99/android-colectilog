package cs10.apps.travels.tracer.ui.lines

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import cs10.apps.common.android.CSActivity
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.Utils
import cs10.apps.travels.tracer.adapter.BusRamalsAdapter
import cs10.apps.travels.tracer.databinding.ActivityLineDetailsBinding
import cs10.apps.travels.tracer.db.MiDB

class LineDetail : CSActivity(), ColorPickerDialogListener {
    private var llm: LinearLayoutManager? = null
    private var number: Int? = null

    // ViewModel
    private lateinit var binding: ActivityLineDetailsBinding

    // Adapter
    private val adapter: BusRamalsAdapter = BusRamalsAdapter(listOf()) {

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

        // back button
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun receiveExtras() {
        val number = intent.getIntExtra("number", -1)
        if (number == -1) finish()

        binding.toolbarLayout.title = "Linea $number"
        this.number = number

        doInBackground { fillAdapter() }
    }

    private fun fillAdapter() {
        val db = MiDB.getInstance(this)
        val data = db.linesDao().getRamalesFromLine(number!!)

        doInForeground {
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

        return when (item.itemId) {
            R.id.action_palette -> {
                val dialog = ColorPickerDialog.newBuilder()
                dialog.show(this)
                true
            }
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // ---------------------------- COLOR PICKER ------------------------

    override fun onColorSelected(dialogId: Int, color: Int) {
        // lineManagerVM.updateColor(rootVM.database.linesDao(), color, rootVM)
        Log.i("COLOR PICKER", "Color selected")

        doInBackground {
            val db = MiDB.getInstance(this)
            val line = db.linesDao().getByNumber(number!!)
            line.color = color
            db.linesDao().update(line)

            fillAdapter()
        }
    }

    override fun onDialogDismissed(dialogId: Int) {}
}