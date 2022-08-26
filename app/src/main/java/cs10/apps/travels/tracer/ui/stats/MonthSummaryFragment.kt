package cs10.apps.travels.tracer.ui.stats

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import cs10.apps.common.android.CS_Fragment
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.Utils
import cs10.apps.travels.tracer.databinding.FragmentBusesBinding
import cs10.apps.travels.tracer.databinding.ViewCircularPbWithLegendBinding
import cs10.apps.travels.tracer.databinding.ViewLineIndicatorBinding
import cs10.apps.travels.tracer.ui.balance.EditBalanceActivity
import cs10.apps.travels.tracer.viewmodel.RootVM
import cs10.apps.travels.tracer.viewmodel.stats.LineStat
import cs10.apps.travels.tracer.viewmodel.stats.Stat
import cs10.apps.travels.tracer.viewmodel.stats.StatsVM

class MonthSummaryFragment : CS_Fragment() {
    private lateinit var binding: FragmentBusesBinding
    private lateinit var statsVM: StatsVM
    private lateinit var rootVM: RootVM

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentBusesBinding.inflate(inflater, container, false)

        statsVM = ViewModelProvider(this)[StatsVM::class.java]
        statsVM.balance.observe(viewLifecycleOwner) { updateCurrency(it) }
        statsVM.busStat.observe(viewLifecycleOwner) { updateTypeStat(it, binding.busPb) }
        statsVM.trainStat.observe(viewLifecycleOwner) { updateTypeStat(it, binding.trainsPb) }
        statsVM.coffeeStat.observe(viewLifecycleOwner) { updateTypeStat(it, binding.coffeePb) }
        statsVM.bus1Stat.observe(viewLifecycleOwner) { updateLineStat(it, binding.bus1Pb, binding.vli1)}
        statsVM.bus2Stat.observe(viewLifecycleOwner) { updateLineStat(it, binding.bus2Pb, binding.vli2)}
        statsVM.bus3Stat.observe(viewLifecycleOwner) { updateLineStat(it, binding.bus3Pb, binding.vli3) }

        rootVM = ViewModelProvider(requireActivity())[RootVM::class.java]
        rootVM.loading.observe(requireActivity()) { binding.root.isVisible = !it }

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

    private fun updateLineStat(stat: LineStat, v1: ViewCircularPbWithLegendBinding, v2: ViewLineIndicatorBinding) {
        updateTypeStat(stat, v1)
        v2.textLineNumber.text = stat.line.toString()
        v2.root.setCardBackgroundColor(ContextCompat.getColor(v2.root.context, Utils.colorFor(stat.line)))
        v2.root.visibility = View.VISIBLE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.busPb.pb.progressDrawable = ContextCompat.getDrawable(view.context, R.drawable.circle_bus)
        binding.trainsPb.pb.progressDrawable = ContextCompat.getDrawable(view.context, R.drawable.circle_bus)
        binding.bus2Pb.pb.progressDrawable = ContextCompat.getDrawable(view.context, R.drawable.circle_yellow)
        binding.bus3Pb.pb.progressDrawable = ContextCompat.getDrawable(view.context, R.drawable.circle_green)

        rootVM.enableLoading()
        statsVM.fillData(rootVM)

        binding.ivEdit.setOnClickListener {
            startActivity(Intent(context, EditBalanceActivity::class.java))
        }
    }
}