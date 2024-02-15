package cs10.apps.travels.tracer.modules.stops.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import cs10.apps.common.android.ui.CS_Fragment
import cs10.apps.travels.tracer.databinding.FragmentStopsBinding
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
            rootVM.disableLoading()
            adapter.paradasList = it
            adapter.notifyDataSetChanged()
        }
    }

    override fun onResume() {
        super.onResume()

        locationVM.getLiveData().observe(viewLifecycleOwner) {
            if (adapter.itemCount == 0) {
                rootVM.enableLoading()
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
}