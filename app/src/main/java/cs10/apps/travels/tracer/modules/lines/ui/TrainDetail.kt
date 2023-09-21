package cs10.apps.travels.tracer.modules.lines.ui

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import cs10.apps.common.android.ui.FormActivity
import cs10.apps.travels.tracer.Utils
import cs10.apps.travels.tracer.databinding.ActivityTrainDetailsBinding
import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.enums.TransportType
import cs10.apps.travels.tracer.modules.lines.adapter.CommonLineInfoAdapter
import cs10.apps.travels.tracer.modules.lines.model.TrainInfo
import cs10.apps.travels.tracer.modules.lines.utils.SpeedCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TrainDetail : FormActivity(), TabLayout.OnTabSelectedListener {

    // view binding
    private lateinit var binding: ActivityTrainDetailsBinding

    // Adapter
    private val adapter: CommonLineInfoAdapter = CommonLineInfoAdapter(listOf()) {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTrainDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        Utils.loadTrainBanner(binding.appbarImage)

        binding.toolbarLayout.title = "Línea Roca"

        // switch tabs
        binding.lineTabs.addOnTabSelectedListener(this)

        // set adapter
        binding.recycler.adapter = adapter
        binding.recycler.layoutManager = LinearLayoutManager(this)

        if (adapter.list.isEmpty()) lifecycleScope.launch(Dispatchers.IO) { fillDestinationsData() }
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        tab?.let {
            when (it.position) {
                0 -> lifecycleScope.launch(Dispatchers.IO) { fillDestinationsData() }
                1 -> lifecycleScope.launch(Dispatchers.IO) { fillByDayData() }
                else -> {}
            }
        }
    }

    private suspend fun fillByDayData() {
        val db = MiDB.getInstance(this)
        val data = db.linesDao().getDayStatsForTrain()

        data.forEach {
            val stats = db.linesDao().getRecentFinishedTravelsOn(it.wd, TransportType.TRAIN.ordinal)
            SpeedCalculator.calculate(it, stats)
        }

        sortAndPost(data)
    }

    private fun fillDestinationsData() {
        sortAndPost(mutableListOf())
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {

    }

    override fun onTabReselected(tab: TabLayout.Tab?) {

    }

    private fun sortAndPost(list: MutableList<out TrainInfo>) {
        list.sort()

        lifecycleScope.launch(Dispatchers.Main){
            adapter.list = list
            adapter.notifyDataSetChanged()
        }
    }
}