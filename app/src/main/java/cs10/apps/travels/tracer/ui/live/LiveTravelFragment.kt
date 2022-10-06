package cs10.apps.travels.tracer.ui.live

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import cs10.apps.common.android.Calendar2
import cs10.apps.common.android.Emoji
import cs10.apps.rater.HappyRater
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.Utils
import cs10.apps.travels.tracer.adapter.NearStopAdapter
import cs10.apps.travels.tracer.databinding.FragmentLiveTravelBinding
import cs10.apps.travels.tracer.databinding.SimpleImageBinding
import cs10.apps.travels.tracer.modules.ZoneData
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

    // adapters
    private val nearStopAdapter = NearStopAdapter(mutableListOf())


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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
                binding.travelFrom.text = "Desde: " + it.nombrePdaInicio
                binding.travelTo.text = "Hasta: " + it.nombrePdaFin
                binding.buttonDrawing.setImageDrawable(Utils.getTypeDrawable(it.tipo, binding.root.context))
                rootVM.disableLoading()
            }
        }

        liveVM.toggle.observe(viewLifecycleOwner) {
            liveVM.travel.value?.let { t ->
                if (it && t.ramal != null) {
                    binding.lineSubtitle.textSize = 24f
                    binding.lineSubtitle.text = t.ramal
                    binding.lineSubtitle.setTextColor(ContextCompat.getColor(binding.root.context, R.color.yellow))
                } else {
                    binding.lineSubtitle.text = t.lineSimplified
                    binding.lineSubtitle.textSize = 30f
                    binding.lineSubtitle.setTextColor(ContextCompat.getColor(binding.root.context, R.color.white))
                }

                if (it) liveVM.progress.value?.let { prog -> binding.minutesLeft.text = prog.times(100).roundToInt().toString() + "%" }
                else liveVM.minutesToEnd.value?.let { minutes -> binding.minutesLeft.text = "$minutes'" }
            }
        }

        liveVM.minutesFromStart.observe(viewLifecycleOwner) {
            if (it == null || it <= 0.0) binding.startedMinAgo.text = null
            else binding.startedMinAgo.text = "Inició hace ${it.roundToInt()} minutos"
        }

        liveVM.averageDuration.observe(viewLifecycleOwner) {
            if (it == null || it == 0) binding.averageDuration.text = null
            else binding.averageDuration.text = "Duración promedio: $it minutos"
        }

        liveVM.speed.observe(viewLifecycleOwner) {
            if (it == null) binding.averageSpeed.text = null
            else {
                val formated = (it * 10).roundToInt() / 10.0
                binding.averageSpeed.text = "Velocidad: $formated km/h"
            }
        }

        liveVM.progress.observe(viewLifecycleOwner) {
            when {
                it == null -> binding.pb.progress = 0
                it > 0.97 -> finishCurrentTravel()
                else -> binding.pb.progress = (it * 100).roundToInt()
            }
        }

        liveVM.nearArrivals.observe(viewLifecycleOwner) {
            binding.nearBoxInfo.isVisible = it.isNotEmpty()
            nearStopAdapter.list = it
            nearStopAdapter.notifyDataSetChanged()
        }

        liveVM.minutesToEnd.observe(viewLifecycleOwner) {
            binding.shareBtn.isVisible = it != null

            if (it == null) binding.minutesLeft.text = null
            else {
                val eta = Calendar.getInstance().apply { add(Calendar.MINUTE, it) }

                binding.minutesLeft.text = "$it'"
                binding.etaInfo.text = "Llegarías a las " + Utils.hourFormat(eta)
                binding.finishBtn.isVisible = it < 10

                // next combination
                liveVM.nextTravel.value?.let { nextT ->
                    val etaNext = Calendar2.getETA(eta, nextT.duration + 15)
                    binding.nextTravelInfo.text = "Combinación a ${nextT.nombrePdaFin} (${Utils.hourFormat(etaNext)})"
                }
            }
        }

        locationVM.location.observe(viewLifecycleOwner) {
            // updating animation
            binding.updatingView.root.isVisible = true
            Handler(Looper.getMainLooper()).postDelayed({ binding.updatingView.root.isVisible = false}, 3000)

            liveVM.recalculateDistances(rootVM.database, it) { rootVM.disableLoading() }

            val zone = ZoneData.getZoneUppercase(it)
            binding.zoneInfo.text = zone
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rootVM.enableLoading()

        // on share travel
        binding.shareBtn.setOnClickListener { shareCurrentTravel() }

        // on finish travel
        binding.finishBtn.setOnClickListener { finishCurrentTravel() }

        // near stops adapter
        binding.nearStopsRecycler.adapter = nearStopAdapter
        binding.nearStopsRecycler.layoutManager = LinearLayoutManager(requireActivity())

    }

    override fun onResume() {
        super.onResume()

        resetViews()
        liveVM.findLastTravel(rootVM.database)
    }

    override fun onStop() {
        super.onStop()

        liveVM.resetAllButTravel()
    }


    // ------------------------------- DONE -------------------------------

    private fun resetViews() {
        binding.finishBtn.isVisible = false
        binding.etaInfo.text = null
        binding.averageSpeed.text = null
        binding.travelTo.text = null
        binding.travelFrom.text = null
        binding.lineSubtitle.text = null
        binding.averageDuration.text = null
        binding.nextTravelInfo.text = null
        binding.minutesLeft.text = "..."
        binding.pb.progress = 0
        rootVM.disableLoading()
    }

    private fun shareCurrentTravel() {

        liveVM.minutesToEnd.value?.let {
            val line = liveVM.travel.value?.linea
            val destination = liveVM.travel.value?.nombrePdaFin

            /*This will be the actual content you wish you share.*/
            val shareBody = "Hola! ${Emoji.getHandEmoji()} \n\n" +
                    "${Emoji.getBusEmoji()} Estoy viajando en un $line \n" +
                    "${Emoji.getGlobeEmoji()} Destino: $destination \n" +
                    "${Emoji.getClockEmoji()} Llego a las ${getETA(it)} \n\n" +
                    "*Enviado desde Mi App*"

            /*Create an ACTION_SEND Intent*/
            val intent = Intent(Intent.ACTION_SEND)

            /*The type of the content is text, obviously.*/
            intent.type = "text/plain"

            /*Applying information Subject and Body.*/
            intent.putExtra(Intent.EXTRA_SUBJECT, "Travel Tracer")
            intent.putExtra(Intent.EXTRA_TEXT, shareBody)

            /*Fire!*/
            startActivity(Intent.createChooser(intent, "Compartir via..."))
        }
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
            showRateDialog()
            resetViews()
        }, 2000)
    }

    private fun showRateDialog() {
        val rater = HappyRater()
        rater.doneCallback = { rate -> liveVM.saveRating(rate, rootVM.database) }
        rater.cancelCallback = { liveVM.eraseAll() }
        rater.create(requireContext(), layoutInflater)
    }

    private fun getETA(minutesToEnd: Int) : CharSequence {
        val eta = Calendar.getInstance().apply { add(Calendar.MINUTE, minutesToEnd) }
        return Utils.hourFormat(eta)
    }
}