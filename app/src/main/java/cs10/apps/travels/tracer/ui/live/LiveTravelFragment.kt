package cs10.apps.travels.tracer.ui.live

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import cs10.apps.travels.tracer.Utils
import cs10.apps.travels.tracer.databinding.FragmentLiveTravelBinding
import cs10.apps.travels.tracer.databinding.SimpleImageBinding
import cs10.apps.travels.tracer.viewmodel.LiveVM
import cs10.apps.travels.tracer.viewmodel.LocationVM
import cs10.apps.travels.tracer.viewmodel.RootVM
import java.util.*
import kotlin.math.roundToInt

class LiveTravelFragment : Fragment() {

    // View Models
    private lateinit var rootVM: RootVM
    private lateinit var liveVM: LiveVM
    private lateinit var locationVM: LocationVM

    // View Binding
    private lateinit var binding: FragmentLiveTravelBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentLiveTravelBinding.inflate(inflater, container, false)

        // view model observers
        rootVM = ViewModelProvider(requireActivity())[RootVM::class.java]
        liveVM = ViewModelProvider(requireActivity())[LiveVM::class.java]
        locationVM = ViewModelProvider(requireActivity())[LocationVM::class.java]

        rootVM.loading.observe(requireActivity()) { binding.root.isVisible = !it }

        liveVM.travel.observe(viewLifecycleOwner) {
            if (it == null) resetViews()
            else {
                binding.buttonCard.setCardBackgroundColor(Utils.colorFor(it.linea, context))
                binding.lineSubtitle.text = it.linea.toString()
                binding.travelFrom.text = "Desde: " + it.nombrePdaInicio
                binding.travelTo.text = "Hasta: " + it.nombrePdaFin
                binding.buttonDrawing.setImageDrawable(Utils.getTypeDrawable(it.tipo, binding.root.context))
            }
        }

        liveVM.minutesFromStart.observe(viewLifecycleOwner) {
            if (it > 0) binding.startedMinAgo.text = "Inició hace $it minutos"
        }

        liveVM.speed.observe(viewLifecycleOwner) {
            val formated = (it * 10).roundToInt() / 10.0
            binding.averageSpeed.text = "Velocidad: $formated km/h"
        }

        liveVM.minutesToEnd.observe(viewLifecycleOwner) {
            binding.minutesLeft.text = "$it'"
            binding.pb.progress = ((120 - it) * 100 / 120)

            val eta = Calendar.getInstance().apply { add(Calendar.MINUTE, it) }

            binding.etaInfo.text = "Llegarías a las " + Utils.hourFormat(eta)

            // show finish button
            binding.finishBtn.isVisible = it < 10
            rootVM.disableLoading()

            // auto-finish
            if (it == 0) finishCurrentTravel()
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
        binding.finishBtn.setOnClickListener { finishCurrentTravel() }
    }

    override fun onResume() {
        super.onResume()

        liveVM.findLastTravel(rootVM.database, locationVM) { rootVM.disableLoading() }
    }


    // ------------------------------- DONE -------------------------------

    private fun resetViews() {
        binding.finishBtn.isVisible = false
        binding.etaInfo.text = null
        binding.averageSpeed.text = null
        binding.minutesLeft.text = null
        binding.startedMinAgo.text = null
        binding.travelTo.text = null
        binding.travelFrom.text = null
        binding.lineSubtitle.text = null
        rootVM.disableLoading()
    }

    private fun finishCurrentTravel() {
        liveVM.finishTravel(Calendar.getInstance(), rootVM.database)
        showDoneAnimation()
    }

    private fun showDoneAnimation() {

        val iv2 = SimpleImageBinding.inflate(layoutInflater, null, false)
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(iv2.root)

        Glide.with(requireActivity())
            .load("https://media.geeksforgeeks.org/wp-content/uploads/20201129221326/abc.gif")
            .into(iv2.iv)

        val dialog = builder.create()
        dialog.show()

        // dismiss dialog and clear views
        Handler(Looper.getMainLooper()).postDelayed({
            dialog.cancel()
            resetViews()
        }, 2000)
    }
}