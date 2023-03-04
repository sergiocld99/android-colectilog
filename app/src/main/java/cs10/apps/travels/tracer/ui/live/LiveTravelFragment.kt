package cs10.apps.travels.tracer.ui.live

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import cs10.apps.common.android.Calendar2
import cs10.apps.common.android.Emoji
import cs10.apps.common.android.NumberUtils
import cs10.apps.common.android.ui.CS_Fragment
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.Utils
import cs10.apps.travels.tracer.adapter.NearStopAdapter
import cs10.apps.travels.tracer.databinding.FragmentLiveTravelBinding
import cs10.apps.travels.tracer.databinding.SimpleImageBinding
import cs10.apps.travels.tracer.notification.NotificationCenter
import cs10.apps.travels.tracer.ui.service.ServiceDetail
import cs10.apps.travels.tracer.ui.travels.BusTravelEditor
import cs10.apps.travels.tracer.ui.travels.TrainTravelEditor
import cs10.apps.travels.tracer.viewmodel.LocationVM
import cs10.apps.travels.tracer.viewmodel.RootVM
import cs10.apps.travels.tracer.viewmodel.live.LiveVM
import cs10.apps.travels.tracer.viewmodel.live.WaitingVM
import java.util.*
import kotlin.math.abs
import kotlin.math.roundToInt


class LiveTravelFragment : CS_Fragment() {

    // View Models
    private lateinit var rootVM: RootVM
    private lateinit var liveVM: LiveVM
    private lateinit var locationVM: LocationVM
    private lateinit var waitingVM: WaitingVM

    // View Binding
    private lateinit var binding: FragmentLiveTravelBinding

    // adapters
    private val nearStopAdapter = NearStopAdapter(mutableListOf()) {
        val intent = Intent(activity, ServiceDetail::class.java)
        intent.putExtra("station", it.station)
        intent.putExtra("ramal", it.ramal)
        intent.putExtra("id", it.service)
        startActivity(intent)
    }

    // Custom Views
    private lateinit var liveWaitingView: LiveWaitingView

    /* ====================== MAIN FUNCTIONS ==================================== */

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentLiveTravelBinding.inflate(inflater, container, false)

        // inflate custom views
        liveWaitingView = LiveWaitingView(binding.waitingLayout)

        // view model observers
        rootVM = ViewModelProvider(requireActivity())[RootVM::class.java]
        liveVM = ViewModelProvider(requireActivity())[LiveVM::class.java]
        locationVM = ViewModelProvider(requireActivity())[LocationVM::class.java]
        waitingVM = ViewModelProvider(requireActivity())[WaitingVM::class.java]

        // progress bar
        rootVM.loading.observe(requireActivity()) { binding.root.isVisible = !it }

        observeLiveVM()

        waitingVM.stopHere.observe(viewLifecycleOwner) {
            // liveWaitingView.setVisibility(it != null)
            liveWaitingView.setStopHere(it)
        }

        locationVM.getLiveData().observe(viewLifecycleOwner) {
            // updating animation
            if (binding.lineSubtitle.text.length < 7){
                binding.updatingView.root.isVisible = true
                Handler(Looper.getMainLooper()).postDelayed({ binding.updatingView.root.isVisible = false}, 3000)
            }

            liveVM.recalculateDistances(it.location) { rootVM.disableLoading() }
            if (liveWaitingView.isVisible()) waitingVM.updateLocation(it.location)

            //val zone = ZoneData.getZoneUppercase(it.location)
            //binding.zoneInfo.text = zone
        }

        // OCT 2022
        locationVM.setSpeedObserver(viewLifecycleOwner) { speedKmH ->
            if (speedKmH > 100) binding.speedometerText.text = "--"
            else binding.speedometerText.text = NumberUtils.round(speedKmH, 5).toString()
        }

        return binding.root
    }

    private fun observeLiveVM() {
        liveVM.travel.observe(viewLifecycleOwner) {
            binding.travellingLayout.isVisible = it != null
            liveWaitingView.setVisibility(it == null)

            if (it == null) {
                resetViews()
            } else {
                val bgColor = it.color ?: Utils.colorFor(it.linea, context)
                binding.buttonCard.setCardBackgroundColor(bgColor)

                binding.travelFrom.text = "Desde: " + it.nombrePdaInicio
                binding.travelTo.text = "Hasta: " + it.nombrePdaFin
                binding.buttonDrawing.setImageDrawable(
                    Utils.getTypeDrawable(
                        it.tipo,
                        binding.root.context
                    )
                )
                rootVM.disableLoading()
            }
        }

        liveVM.toggle.observe(viewLifecycleOwner) {
            liveVM.travel.value?.let { t ->
                if (it && t.ramal != null) {
                    if (binding.lineSubtitle.text.toString().length < 7) binding.lineSubtitle.isSelected = true
                    binding.lineSubtitle.textSize = 24f
                    binding.lineSubtitle.text = t.ramal
                    binding.lineSubtitle.setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.yellow
                        )
                    )
                } else {
                    binding.lineSubtitle.text = t.lineSimplified
                    binding.lineSubtitle.textSize = 30f
                    binding.lineSubtitle.setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.white
                        )
                    )
                }

                if (it) liveVM.progress.value?.let { prog ->
                    binding.minutesLeft.text = prog.times(100).roundToInt().toString() + "%"
                }
                else liveVM.minutesToEnd.value?.let { minutes ->
                    binding.minutesLeft.text = "$minutes'"
                }
            }
        }

        liveVM.minutesFromStart.observe(viewLifecycleOwner) {
            if (it == null || it <= 0.0) binding.startedMinAgo.text = null
            else binding.startedMinAgo.text = "Inició hace ${it.roundToInt()} minutos"
        }

        liveVM.averageDuration.observe(viewLifecycleOwner) {
            if (it == null || it.totalMinutes == 0) binding.averageDuration.text = null
            else {
                if (it.fromAverage) binding.averageDuration.text =
                    "Duración promedio: ${it.totalMinutes} min. (${it.speed} km/h)"
                else binding.averageDuration.text =
                    "Duración esperada: ${it.totalMinutes} min. (${it.speed} km/h)"
            }
        }

        liveVM.speed.observe(viewLifecycleOwner) {
            if (it == null) binding.averageSpeed.text = null
            else {
                // val formated = (it * 10).roundToInt() / 10.0
                // binding.averageSpeed.text = "Velocidad: $formated km/h"
            }
        }

        liveVM.progress.observe(viewLifecycleOwner) { prog ->
            when (prog) {
                null -> binding.pb.progress = 0
                // prog > 0.97 -> finishCurrentTravel()
                else -> binding.pb.progress = (prog * 100).roundToInt()
            }

            val avgD = liveVM.averageDuration.value
            val currentTime = liveVM.minutesFromStart.value

            if (prog != null && avgD != null && currentTime != null) {
                val estimation = avgD.totalMinutes * prog
                val error = abs(currentTime - estimation).roundToInt()

                binding.estimationError.isVisible = error > 3
                binding.estimationError.text = "Estimación con error de $error minutos"
            } else binding.estimationError.isVisible = false
        }

        liveVM.endDistance.observe(viewLifecycleOwner) {
            if (it != null) {
                if (it < 0.8) finishCurrentTravel()
                else {
                    // show below elapsed time tv
                    binding.averageSpeed.text = String.format("Destino a %.2f km", it)
                }
            }
        }

        liveVM.nearArrivals.observe(viewLifecycleOwner) {
            binding.nearBoxInfo.isVisible = it.isNotEmpty()
            nearStopAdapter.list = it
            nearStopAdapter.notifyDataSetChanged()
        }

        liveVM.minutesToEnd.observe(viewLifecycleOwner) {
            binding.shareBtn.isVisible = it != null
            binding.editBtn.isVisible = it != null

            if (it == null) binding.minutesLeft.text = null
            else {
                val eta = Calendar.getInstance().apply { add(Calendar.MINUTE, it) }

                // ui
                binding.minutesLeft.text = "$it'"
                binding.etaInfo.text = "Llegarías a las " + Utils.hourFormat(eta)
                binding.finishBtn.isVisible = it < 10

                // arrival notification
                liveVM.travel.value?.id?.let { id ->
                    val prefs = requireContext().getSharedPreferences("eta_notified", Context.MODE_PRIVATE)
                    val scheduled = prefs.getLong("last_id", -1)
                    if (scheduled != id) {
                        NotificationCenter().scheduleAskNotification(requireContext(), it * 60000L)
                        prefs.edit().putLong("last_id", id).apply()
                    }
                }


                // next combination
                liveVM.nextTravel.value?.let { nextT ->
                    val etaNext = Calendar2.getETA(eta, nextT.duration + 15)
                    binding.nextTravelInfo.text =
                        "Combinación a ${nextT.nombrePdaFin} (${Utils.hourFormat(etaNext)})"
                }
            }
        }

        liveVM.customZone.observe(viewLifecycleOwner) {
            if (it == null) binding.zoneInfo.text = "Zona desconocida"
            else binding.zoneInfo.text = it.name
        }

        // DEC 2022: EXPECTED RATING
        liveVM.rate.observe(viewLifecycleOwner) {
            if (it == null) binding.rateText.text = "--"
            else binding.rateText.text = Utils.rateFormat(it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rootVM.enableLoading()

        // on share travel
        binding.shareBtn.setOnClickListener { shareCurrentTravel() }

        // on finish travel (by user)
        binding.finishBtn.setOnClickListener { finishCurrentTravel() }

        // on edit travel
        binding.editBtn.setOnClickListener { editCurrentTravel() }

        // near stops adapter
        binding.nearStopsRecycler.adapter = nearStopAdapter
        binding.nearStopsRecycler.layoutManager = LinearLayoutManager(requireActivity())

    }

    override fun onResume() {
        super.onResume()

        resetViews()
        liveVM.findLastTravel(locationVM) { rootVM.disableLoading() }
    }

    override fun onStop() {
        super.onStop()
        liveVM.eraseAll()
    }


    // ------------------------------- DONE -------------------------------

    private fun resetViews() {
        binding.travellingLayout.isVisible = false
        binding.finishBtn.isVisible = false
        binding.etaInfo.text = null
        binding.averageSpeed.text = null
        binding.travelTo.text = null
        binding.travelFrom.text = null
        binding.lineSubtitle.text = null
        binding.averageDuration.text = null
        binding.nextTravelInfo.text = null
        binding.minutesLeft.text = "..."
        binding.speedometerText.text = "--"
        binding.pb.progress = 0
        binding.rateText.text = "--"
        rootVM.disableLoading()
    }

    private fun shareCurrentTravel() {

        liveVM.minutesToEnd.value?.let {
            val line = liveVM.travel.value?.linea
            val ramal = liveVM.travel.value?.ramal
            val destination = liveVM.travel.value?.nombrePdaFin

            doInBackground {
                val sb = StringBuilder("${Emoji.getBusEmoji()} ")

                if (line != null){
                    val lineDetails = rootVM.database.linesDao().getByNumber(line)

                    if (!lineDetails?.name.isNullOrEmpty()) sb.append("${lineDetails?.name}")
                    else sb.append("Linea $line")
                } else sb.append("Tren Roca")

                if (ramal != null) sb.append(" - $ramal \n")
                else sb.append("\n")

                sb.append("${Emoji.getGlobeEmoji()} Destino: $destination \n")
                sb.append("${Emoji.getClockEmoji()} Llego a las ${getETA(it)}")

                val shareBody = sb.toString().trim()
                val intent = Intent(Intent.ACTION_SEND)

                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_SUBJECT, "Travel Tracer")
                intent.putExtra(Intent.EXTRA_TEXT, shareBody)

                doInForeground {
                    startActivity(Intent.createChooser(intent, "Compartir via..."))
                }
            }
        }
    }

    private fun finishCurrentTravel() {
        liveVM.finishTravel(Calendar.getInstance(), layoutInflater, requireActivity())
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
            // showRateDialog()
            resetViews()
        }, 2000)
    }

    /*
    private fun showRateDialog() {
        val rater = HappyRater()
        rater.doneCallback = { rate -> liveVM.saveRating(rate) }
        rater.cancelCallback = { liveVM.eraseAll() }
        rater.create(requireContext(), layoutInflater)
    }

     */

    private fun getETA(minutesToEnd: Int) : CharSequence {
        val eta = Calendar.getInstance().apply { add(Calendar.MINUTE, minutesToEnd) }
        return Utils.hourFormat(eta)
    }

    // ------------------------ EDIT -------------------------------

    private fun editCurrentTravel() {
        liveVM.travel.value?.let { viaje ->
            val intent = Intent(activity,
                if (viaje.tipo == 0) BusTravelEditor::class.java else TrainTravelEditor::class.java
            )

            intent.putExtra("travelId", viaje.id)
            startActivity(intent)
        }
    }
}