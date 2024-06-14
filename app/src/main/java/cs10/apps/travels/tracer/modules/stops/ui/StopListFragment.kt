package cs10.apps.travels.tracer.modules.stops.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import cs10.apps.common.android.ui.CS_Fragment
import cs10.apps.travels.tracer.databinding.FragmentStopsBinding
import cs10.apps.travels.tracer.enums.TransportType
import cs10.apps.travels.tracer.model.Parada
import cs10.apps.travels.tracer.modules.stops.adapter.StopAdapter
import cs10.apps.travels.tracer.modules.stops.viewmodel.StopsVM
import cs10.apps.travels.tracer.viewmodel.LocationVM
import cs10.apps.travels.tracer.viewmodel.RootVM

class StopListFragment : CS_Fragment() {
    private lateinit var binding: FragmentStopsBinding
    private var adapter = StopAdapter(mutableListOf()) { onClickStop(it) }

    // view models
    private lateinit var locationVM: LocationVM
    private lateinit var rootVM: RootVM
    private lateinit var stopsVM: StopsVM

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStopsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recycler.layoutManager = LinearLayoutManager(context)
        binding.recycler.adapter = adapter

        rootVM = ViewModelProvider(requireActivity())[RootVM::class.java]
        locationVM = ViewModelProvider(requireActivity())[LocationVM::class.java]
        stopsVM = ViewModelProvider(requireActivity())[StopsVM::class.java]

        stopsVM.getLiveData().observe(viewLifecycleOwner) {
            //rootVM.disableLoading()
            showContent()
            adapter.paradasList = it
            adapter.notifyDataSetChanged()
        }

        // filter
        binding.typeFilter.roundedTabs.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                showLoading()

                adapter.itemCount.let {
                    if (it > 0) {
                        adapter.paradasList = mutableListOf()
                        adapter.notifyItemRangeRemoved(0, it)
                    }
                }

                val constraint: TransportType? = when (tab!!.position) {
                    1 -> TransportType.BUS
                    2 -> TransportType.TRAIN
                    3 -> TransportType.METRO
                    else -> null
                }

                stopsVM.filter(constraint)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })
    }

    override fun onResume() {
        super.onResume()

        locationVM.getLiveData().observe(viewLifecycleOwner) {
            if (adapter.itemCount == 0 && binding.typeFilter.roundedTabs.selectedTabPosition == 0) {
                //rootVM.enableLoading()
                showLoading()
                stopsVM.findAll(it.location)
            } else {
                // POTENTIAL RISK: CONCURRENT MODIFICATION EXCEPTION
                stopsVM.updateDistances(it.location)
            }
        }
    }

    private fun onClickStop(item: Parada) {
        val intent = Intent(activity, StopInfoActivity::class.java)
        intent.putExtra("stopName", item.nombre)
        intent.putExtra("type", item.tipo)
        startActivity(intent)
    }

    private fun showContent() {
        binding.recycler.visibility = View.VISIBLE
        binding.viewLoading.visibility = View.GONE
        //binding.typeFilter.tabsBox.visibility = if (filterAvailable) View.VISIBLE else View.GONE
    }

    private fun showLoading() {
        binding.recycler.visibility = View.GONE
        binding.viewLoading.visibility = View.VISIBLE
    }
}