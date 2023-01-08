package cs10.apps.travels.tracer.ui.stops

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import cs10.apps.common.android.ui.CS_Fragment
import cs10.apps.travels.tracer.Utils
import cs10.apps.travels.tracer.adapter.StopAdapter
import cs10.apps.travels.tracer.databinding.FragmentStopsBinding
import cs10.apps.travels.tracer.viewmodel.LocationVM
import cs10.apps.travels.tracer.viewmodel.RootVM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StopListFragment : CS_Fragment() {
    private lateinit var binding: FragmentStopsBinding
    private var adapter = StopAdapter(mutableListOf()) { onEditStop(it.nombre) }

    // view models
    private lateinit var locationVM: LocationVM
    private lateinit var rootVM: RootVM


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
        // locationVM = ViewModelProvider(requireActivity()).get(LocationVM::class.java)

        rootVM = ViewModelProvider(requireActivity())[RootVM::class.java]
        locationVM = ViewModelProvider(requireActivity())[LocationVM::class.java]
    }

    private fun recalc(location: Location) {
        fillData(location)

    /*
        if (adapter.itemCount == 0) fillData(location)
        else {
            for (p in adapter.paradasList) p.updateDistance(location)
            adapter.paradasList.sort()
            //Utils.orderByProximity(adapter.getParadasList(), location.getLatitude(), location.getLongitude());
            doInForeground { adapter.notifyDataSetChanged() }
        }

         */
    }


    private fun fillData(location: Location) {

        lifecycleScope.launch(Dispatchers.IO){
            val db = rootVM.database
            val paradas = db.paradasDao().all
            Utils.orderByProximity(paradas, location.latitude, location.longitude)

            // find zones
            paradas.forEach {
                it.zone = db.zonesDao().findZonesIn(it.latitud, it.longitud).firstOrNull()
            }

            // show content
            doInForeground {
                val ogSize = adapter.itemCount
                val newSize = paradas.size

                adapter.paradasList = paradas
                if (ogSize == 0) adapter.notifyItemRangeInserted(0, newSize)
                else adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        locationVM.getLiveData().observe(viewLifecycleOwner) { recalc(it.location) }
    }

    private fun onEditStop(stopName: String) {
        val intent = Intent(activity, StopEditor::class.java)
        intent.putExtra("stopName", stopName)
        startActivity(intent)
    }
}