package cs10.apps.travels.tracer.modules.lines.ui

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import cs10.apps.common.android.ui.FormActivity
import cs10.apps.travels.tracer.Utils
import cs10.apps.travels.tracer.constants.ErrorCodes
import cs10.apps.travels.tracer.constants.Extras
import cs10.apps.travels.tracer.databinding.ActivityHourStatsBinding
import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.modules.lines.adapter.HourChartAdapter
import cs10.apps.travels.tracer.modules.lines.model.HourChartData

class HourStatsActivity : FormActivity() {
    private lateinit var binding: ActivityHourStatsBinding
    private val adapter = HourChartAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHourStatsBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        Utils.loadBusBanner(binding.appbarImage)

        // load and show data
        binding.recycler.adapter = adapter
        binding.recycler.layoutManager = LinearLayoutManager(this)
        buildStats()
    }

    private fun buildStats(){
        val line = intent.getIntExtra(Extras.LINE_NUMBER.name, ErrorCodes.INVALID_LINE.code)
        if (line == ErrorCodes.INVALID_LINE.code) return

        binding.toolbarLayout.title = String.format("Linea %d", line)

        doInBackground {
            val dao = MiDB.getInstance(this).linesDao()
            val topStopsFrom = dao.getFrequentTravelsFromLine(line)
            //if (topStopsFrom.size < 2) return@doInBackground

            adapter.clear()

            topStopsFrom.forEach { frequentTravel ->
                val statList = dao.getHourStatsForTravel(line, frequentTravel.nombrePdaInicio, frequentTravel.nombrePdaFin)
                if (statList.size > 1) adapter.add(HourChartData(statList, frequentTravel))
            }

            doInForeground({ adapter.notifyDataSetChanged() })
        }
    }

}