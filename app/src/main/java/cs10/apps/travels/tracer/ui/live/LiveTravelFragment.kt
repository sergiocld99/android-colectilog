package cs10.apps.travels.tracer.ui.live

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import cs10.apps.travels.tracer.Utils
import cs10.apps.travels.tracer.databinding.FragmentLiveTravelBinding
import cs10.apps.travels.tracer.viewmodel.LocationVM
import cs10.apps.travels.tracer.viewmodel.RootVM
import java.util.*

class LiveTravelFragment : Fragment() {

    // View Models
    private lateinit var rootVM: RootVM
    private lateinit var liveVM: LiveVM
    private lateinit var locationVM: LocationVM

    // View Binding
    private lateinit var binding: FragmentLiveTravelBinding


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentLiveTravelBinding.inflate(inflater, container, false)

        // view model observers
        rootVM = ViewModelProvider(requireActivity())[RootVM::class.java]
        liveVM = ViewModelProvider(requireActivity())[LiveVM::class.java]
        locationVM = ViewModelProvider(requireActivity())[LocationVM::class.java]

        rootVM.loading.observe(requireActivity()) { binding.root.isVisible = !it }

        liveVM.travel.observe(viewLifecycleOwner) {
            binding.buttonCard.setCardBackgroundColor(Utils.colorFor(it.linea, context))
            binding.lineSubtitle.text = it.linea.toString()
            binding.travelFrom.text = "Desde: " + it.nombrePdaInicio
            binding.travelTo.text = "Hasta: " + it.nombrePdaFin
        }

        liveVM.minutesFromStart.observe(viewLifecycleOwner) {
            if (it > 0) binding.startedMinAgo.text = "Inició hace $it minutos"
        }

        liveVM.speed.observe(viewLifecycleOwner) {
            binding.averageSpeed.text = "Velocidad: $it km/h"
            liveVM.calculateETA(it)
        }

        liveVM.minutesToEnd.observe(viewLifecycleOwner) {
            binding.minutesLeft.text = "$it'"
            binding.pb.progress = ((120 - it) * 100 / 120)

            val eta = Calendar.getInstance().apply {
                add(Calendar.MINUTE, it)
            }

            binding.etaInfo.text = "Llegarías a las " + Utils.hourFormat(eta)

            // show finish button
            binding.finishBtn.visibility = View.VISIBLE
            rootVM.disableLoading()
        }

        locationVM.location.observe(viewLifecycleOwner) {
            liveVM.recalculateDistances(rootVM.database, it) { rootVM.disableLoading() }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rootVM.enableLoading()

        // on finish travel
        binding.finishBtn.setOnClickListener {
            liveVM.finishTravel(Calendar.getInstance(), rootVM.database)
        }
    }

    override fun onResume() {
        super.onResume()

        liveVM.findLastTravel(rootVM.database, locationVM) { rootVM.disableLoading() }
    }
}