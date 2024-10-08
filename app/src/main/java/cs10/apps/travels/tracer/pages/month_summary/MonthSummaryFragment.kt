package cs10.apps.travels.tracer.pages.month_summary

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import cs10.apps.common.android.ui.CS_Fragment
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.databinding.FragmentBusesBinding
import cs10.apps.travels.tracer.databinding.ViewCircularPbWithLegendBinding
import cs10.apps.travels.tracer.databinding.ViewLineIndicatorBinding
import cs10.apps.travels.tracer.pages.month_summary.adapter.LineTimeSpentAdapter
import cs10.apps.travels.tracer.pages.month_summary.model.LineStat
import cs10.apps.travels.tracer.pages.month_summary.model.Stat
import cs10.apps.travels.tracer.pages.month_summary.viewmodel.StatsVM
import cs10.apps.travels.tracer.utils.Utils
import cs10.apps.travels.tracer.viewmodel.RootVM
import java.util.Calendar

class MonthSummaryFragment : CS_Fragment() {
    private lateinit var binding: FragmentBusesBinding
    private lateinit var statsVM: StatsVM
    private lateinit var rootVM: RootVM
    private val adapter = LineTimeSpentAdapter()
    private var selectedTab = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentBusesBinding.inflate(inflater, container, false)
        binding.rvTimeByLine.adapter = adapter

        statsVM = ViewModelProvider(this)[StatsVM::class.java]
        statsVM.balance.observe(viewLifecycleOwner) { updateCurrency(it) }
        statsVM.busStat.observe(viewLifecycleOwner) { updateTypeStat(it, binding.busPb) }
        statsVM.trainStat.observe(viewLifecycleOwner) { updateTypeStat(it, binding.trainsPb) }
        statsVM.metroStat.observe(viewLifecycleOwner) { updateTypeStat(it, binding.coffeePb) }
        statsVM.bus1Stat.observe(viewLifecycleOwner) { updateLineStat(it, binding.line1.busPb, binding.line1.vli)}
        statsVM.bus2Stat.observe(viewLifecycleOwner) { updateLineStat(it, binding.line2.busPb, binding.line2.vli)}
        statsVM.bus3Stat.observe(viewLifecycleOwner) { updateLineStat(it, binding.line3.busPb, binding.line3.vli) }

        statsVM.timeStats.observe(viewLifecycleOwner) {
            adapter.updateData(it)
        }

        rootVM = ViewModelProvider(requireActivity())[RootVM::class.java]
        rootVM.loading.observe(requireActivity()) { binding.root.isVisible = !it }

        // get current month
        var currentMonthNumber = Calendar.getInstance().get(Calendar.MONTH) + 1

        // repeat 3 times: add tab with month name starting from current month and going back
        for (i in 0..2) {
            val month = Utils.getShortMonthName(currentMonthNumber)
            binding.roundedTabs.addTab(binding.roundedTabs.newTab().setText(month))
            currentMonthNumber--
            if (currentMonthNumber == 0) currentMonthNumber = 12
        }

        return binding.root
    }

    private fun updateCurrency(value: Double) {
        binding.title.text = getString(R.string.current_money, Utils.priceFormat(value))
    }

    private fun updateTypeStat(stat: Stat, v: ViewCircularPbWithLegendBinding) {
        v.pb.progress = stat.porcentage
        v.porcentage.text = getString(R.string.porcentage_value, stat.porcentage)
        v.legendAtBottom.text = Utils.priceFormat(stat.spent)
        v.root.visibility = View.VISIBLE
    }

    private fun updateLineStat(stat: LineStat?, v1: ViewCircularPbWithLegendBinding, v2: ViewLineIndicatorBinding) {
        if (stat == null){
            v1.root.visibility = View.INVISIBLE
            v2.root.visibility = View.INVISIBLE
            return
        }

        updateTypeStat(stat, v1)
        Utils.paintBusColor(stat.color, v2.root)
        v2.textLineNumber.text = stat.line.toString()
        v2.root.visibility = View.VISIBLE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.busPb.pb.progressDrawable = ContextCompat.getDrawable(view.context, R.drawable.circle_bus)
        binding.trainsPb.pb.progressDrawable = ContextCompat.getDrawable(view.context, R.drawable.circle_bus)
        binding.coffeePb.pb.progressDrawable = ContextCompat.getDrawable(view.context, R.drawable.circle_bus)
        binding.line2.busPb.pb.progressDrawable = ContextCompat.getDrawable(view.context, R.drawable.circle_yellow)
        binding.line3.busPb.pb.progressDrawable = ContextCompat.getDrawable(view.context, R.drawable.circle_green)

        rootVM.enableLoading()

        binding.ivEdit.setOnClickListener {
            startActivity(Intent(context, EditBalanceActivity::class.java))
        }

        // listen period tabs
        binding.roundedTabs.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                selectedTab = tab?.position ?: 0
                updateUI()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    // update UI with the selected tab
    private fun updateUI(){
        statsVM.fillData(rootVM, selectedTab)
    }
}