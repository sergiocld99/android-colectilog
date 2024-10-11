package cs10.apps.travels.tracer.pages.live

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import cs10.apps.common.android.Emoji
import cs10.apps.common.android.ui.CS_Fragment
import cs10.apps.travels.tracer.common.enums.TransportType
import cs10.apps.travels.tracer.databinding.FragmentLiveTravelBinding
import cs10.apps.travels.tracer.databinding.SimpleImageBinding
import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.model.joins.ColoredTravel
import cs10.apps.travels.tracer.notification.NotificationCenter
import cs10.apps.travels.tracer.pages.live.components.LiveTravellingView
import cs10.apps.travels.tracer.pages.live.components.LiveWaitingView
import cs10.apps.travels.tracer.pages.live.model.Stage
import cs10.apps.travels.tracer.pages.live.utils.MediumStopsManager
import cs10.apps.travels.tracer.pages.live.viewmodel.LiveVM
import cs10.apps.travels.tracer.pages.live.viewmodel.WaitingVM
import cs10.apps.travels.tracer.pages.registry.editor.BusTravelEditor
import cs10.apps.travels.tracer.pages.registry.editor.CarTravelEditor
import cs10.apps.travels.tracer.pages.registry.editor.MetroTravelEditor
import cs10.apps.travels.tracer.pages.registry.editor.TrainTravelEditor
import cs10.apps.travels.tracer.utils.Utils
import cs10.apps.travels.tracer.viewmodel.LocationVM
import cs10.apps.travels.tracer.viewmodel.RootVM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt


class LiveTravelFragment : CS_Fragment() {

    // View Models
    private lateinit var rootVM: RootVM
    private lateinit var liveVM: LiveVM
    private lateinit var locationVM: LocationVM
    private lateinit var waitingVM: WaitingVM

    // View Binding
    private lateinit var binding: FragmentLiveTravelBinding

    // Custom Views
    private lateinit var liveWaitingView: LiveWaitingView
    private lateinit var liveTravellingView: LiveTravellingView

    /* ====================== MAIN FUNCTIONS ==================================== */

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentLiveTravelBinding.inflate(inflater, container, false)

        // inflate custom views
        liveWaitingView = LiveWaitingView(binding.waitingLayout)
        liveTravellingView = LiveTravellingView(binding.travellingLayout)

        // view model observers
        rootVM = ViewModelProvider(requireActivity())[RootVM::class.java]
        liveVM = ViewModelProvider(requireActivity())[LiveVM::class.java]
        locationVM = ViewModelProvider(requireActivity())[LocationVM::class.java]
        waitingVM = ViewModelProvider(requireActivity())[WaitingVM::class.java]

        // progress bar
        rootVM.loading.observe(requireActivity()) { binding.root.isVisible = !it }

        // tabs
        binding.roundedTabs.addOnTabSelectedListener(TabsListener())

        // live data
        observeLiveVM()

        waitingVM.stopHere.observe(viewLifecycleOwner) {
            liveWaitingView.setStopHere(it, requireActivity())
        }

        locationVM.getLiveData().observe(viewLifecycleOwner) {
            liveTravellingView.onUpdateLocation()
            liveVM.recalculateDistances(it.location) { rootVM.disableLoading() }
            waitingVM.updateLocation(it.location)
        }

        return binding.root
    }

    private fun showWaitingView(forceTabMov: Boolean = true) {
        liveTravellingView.hide()
        liveWaitingView.show()
        if (forceTabMov) updateTabs(true)
        resetViews()
    }

    private fun showTravellingView(t: ColoredTravel) {
        liveTravellingView.show()
        liveWaitingView.hide()
        updateTabs(false)

        liveTravellingView.setTravel(t)
        rootVM.disableLoading()
    }

    private fun observeLiveVM() {
        liveVM.travel.observe(viewLifecycleOwner) {
            // si no estoy viajando, mostrar "esperando"
            if (it == null) showWaitingView()
            else showTravellingView(it)
        }

        liveVM.estData.observe(viewLifecycleOwner) {
            liveTravellingView.onGetHistorical(it)
        }

        liveVM.stages.observe(viewLifecycleOwner) {
            liveTravellingView.onUpdateStages(it)
        }

        liveVM.progress.observe(viewLifecycleOwner) {
            liveTravellingView.onUpdateProgress(it, liveVM.estData.value, liveVM.minutesFromStart.value)
        }

        liveVM.endDistance.observe(viewLifecycleOwner) {
            liveTravellingView.onUpdateDistanceToDestination(it, liveVM.stagedTravel)
        }

        liveVM.finishData.observe(viewLifecycleOwner) {
            if (it) finishCurrentTravel()
        }

        liveVM.countdown.liveData.observe(viewLifecycleOwner) {
            liveTravellingView.onUpdateCountdown(it)
        }

        liveVM.angle.observe(viewLifecycleOwner) {
            liveTravellingView.onUpdateAngle(it)
        }

        liveVM.minutesToEnd.observe(viewLifecycleOwner) {
            if (it == null) return@observe

            liveVM.travel.value?.id?.let { id ->
                val prefs =
                    requireContext().getSharedPreferences("eta_notified", Context.MODE_PRIVATE)
                val scheduled = prefs.getLong("last_id", -1)
                if (scheduled != id) {
                    NotificationCenter().scheduleAskNotification(
                        requireContext(),
                        TimeUnit.MINUTES.toMillis(it.toLong() - 4L)
                    )
                    if (it > 20) NotificationCenter().scheduleNotifyOthersReminder(
                        requireContext(),
                        TimeUnit.MINUTES.toMillis(it.toLong() + 10L)
                    )
                    prefs.edit().putLong("last_id", id).apply()
                }
            }
        }

        liveVM.customZone.observe(viewLifecycleOwner) {
            if (it == null) binding.zoneInfo.isVisible = false
            else {
                binding.zoneInfo.text = it.name
                binding.zoneInfo.isVisible = true
            }
        }

        liveVM.nextZones.observe(viewLifecycleOwner) {
            val stopHere = waitingVM.stopHere.value
            liveTravellingView.onUpdateNextZones(it, stopHere)

            stopHere?.let { p ->
                liveVM.vibrate(140)

                liveVM.mediumStopsManager?.let { msm ->
                    val candidate = p.nombre
                    val angle = liveVM.angle.value
                    val checkAdd = msm.checkIfCanAdd(candidate, angle)
                    val checkDel = !checkAdd && msm.checkIfShouldDelete(candidate, angle)
                    val currentStage = liveVM.stagedTravel?.getCurrentStage()

                    if (checkAdd && currentStage != null) lifecycleScope.launch(Dispatchers.IO) {
                        val db = MiDB.getInstance(activity)
                        val question = msm.getAddQuestion(candidate, currentStage, db)

                        launch(Dispatchers.Main) {
                            showAddMediumStopDialog(question, msm, candidate, currentStage, db)
                        }
                    }

                    if (checkDel) lifecycleScope.launch(Dispatchers.IO) {
                        msm.deleteIfShould(candidate, MiDB.getInstance(activity))
                        reloadData()
                    }
                }
            }
        }

        // DEC 2022: EXPECTED RATING
        liveVM.rate.observe(viewLifecycleOwner) {
            liveTravellingView.onUpdateRate(it)
        }
    }

    private fun showAddMediumStopDialog(
        question: String,
        msm: MediumStopsManager,
        candidate: String,
        currentStage: Stage,
        db: MiDB
    ) {
        val builder = AlertDialog.Builder(activity)
            .setTitle("Sugerencia")
            .setMessage(question)
            .setNeutralButton("No", null)
            .setPositiveButton("Sí") { _, _ ->
                // lifecycleScope es obligatorio para que se ejecute
                lifecycleScope.launch(Dispatchers.IO) {
                    val success = msm.add(candidate, currentStage, db)

                    launch(Dispatchers.Main) {
                        showAddMediumStopResult(success)
                    }
                }
            }

        builder.show()
    }

    private fun showAddMediumStopResult(success: Boolean) {
        if (success) {
            Toast.makeText(activity, "Añadido con éxito", Toast.LENGTH_SHORT).show()
            reloadData()
        } else Toast.makeText(
            activity,
            "Fallo al agregar intermedio",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rootVM.enableLoading()
        liveTravellingView.getShareBtn().setOnClickListener { shareCurrentTravel() }
        liveTravellingView.getDoneBtn().setOnClickListener { finishCurrentTravel() }
        liveTravellingView.getEditBtn().setOnClickListener { editCurrentTravel() }

    }

    override fun onResume() {
        super.onResume()
        resetViews()
        reloadData()
        liveTravellingView.onResume()
    }

    override fun onStop() {
        super.onStop()
        liveVM.eraseAll()
        waitingVM.reset()
        liveTravellingView.onStop()
    }

    // ------------------------------- DONE -------------------------------

    private fun reloadData() {
        liveVM.findLastTravel(locationVM) { rootVM.disableLoading() }
    }

    private fun resetViews() {
        liveTravellingView.hide()
        liveTravellingView.reset()
        rootVM.disableLoading()
    }

    private fun shareCurrentTravel() {

        liveVM.minutesToEnd.value?.let {
            val line = liveVM.travel.value?.linea
            val ramal = liveVM.travel.value?.ramal
            val destination = liveVM.travel.value?.nombrePdaFin
            val travel = liveVM.travel.value

            if (travel != null) doInBackground {
                val sb = StringBuilder()

                when (travel.tipo) {
                    TransportType.TRAIN.ordinal -> sb.append(Emoji.getTrainEmoji())
                    TransportType.CAR.ordinal -> sb.append(Emoji.getCarEmoji())
                    TransportType.BUS.ordinal -> sb.append(Emoji.getBusEmoji())
                    else -> sb.append(Emoji.getHandEmoji())
                }

                if (line != null) {
                    val lineDetails = rootVM.database.linesDao().getByNumber(line)

                    if (!lineDetails?.name.isNullOrEmpty()) sb.append("${lineDetails?.name}")
                    else sb.append("Linea $line")
                } else sb.append(travel.lineInformation)

                if (ramal != null) sb.append(" - $ramal \n")
                else sb.append("\n")

                sb.append("${Emoji.getGlobeEmoji()} Destino: $destination \n")
                sb.append("${Emoji.getClockEmoji()} Llego a las ${getETA(it.roundToInt())}")

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
            resetViews()
        }, 2000)
    }

    private fun getETA(minutesToEnd: Int): CharSequence {
        val eta = Calendar.getInstance().apply { add(Calendar.MINUTE, minutesToEnd) }
        return Utils.hourFormat(eta)
    }

    // ------------------------ EDIT -------------------------------

    private fun editCurrentTravel() {
        liveVM.travel.value?.let { viaje ->
            // if (viaje.tipo == TransportType.CAR.ordinal) return

            val intent = Intent(
                activity,
                when (TransportType.fromOrdinal(viaje.tipo)) {
                    TransportType.BUS -> BusTravelEditor::class.java
                    TransportType.CAR -> CarTravelEditor::class.java
                    TransportType.TRAIN -> TrainTravelEditor::class.java
                    TransportType.METRO -> MetroTravelEditor::class.java
                }
            )

            intent.putExtra("travelId", viaje.id)
            startActivity(intent)
        }
    }

    // ==================== TABS ===================================

    private fun updateTabs(waiting: Boolean) {
        //binding.roundedTabs.isEnabled = !waiting
        // binding.roundedTabs.touchables.forEach { it.isClickable = !waiting }
        if (waiting) binding.roundedTabs.getTabAt(0)?.select()
        else binding.roundedTabs.getTabAt(1)?.select()
    }

    private inner class TabsListener : TabLayout.OnTabSelectedListener {

        override fun onTabSelected(tab: TabLayout.Tab?) {
            tab?.let { checkPosition(it) }
        }

        override fun onTabReselected(tab: TabLayout.Tab?) {
            tab?.let { checkPosition(it) }
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {

        }

        fun checkPosition(tab: TabLayout.Tab) {
            if (tab.position > 0 && liveVM.travel.value == null) {
                binding.roundedTabs.getTabAt(0)?.select()
            }

            if (tab.position == 0 && liveVM.travel.value != null) {
                binding.roundedTabs.getTabAt(1)?.select()
            }
        }
    }
}