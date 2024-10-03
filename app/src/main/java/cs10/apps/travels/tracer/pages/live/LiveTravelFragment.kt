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
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import cs10.apps.common.android.Compass
import cs10.apps.common.android.Emoji
import cs10.apps.common.android.ui.CS_Fragment
import cs10.apps.rater.HappyRater
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.common.enums.TransportType
import cs10.apps.travels.tracer.databinding.FragmentLiveTravelBinding
import cs10.apps.travels.tracer.databinding.SimpleImageBinding
import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.model.joins.ColoredTravel
import cs10.apps.travels.tracer.notification.NotificationCenter
import cs10.apps.travels.tracer.pages.live.adapter.StagesAdapter
import cs10.apps.travels.tracer.pages.live.components.BasicSwitcher
import cs10.apps.travels.tracer.pages.live.components.LiveWaitingView
import cs10.apps.travels.tracer.pages.live.model.SwitcherText
import cs10.apps.travels.tracer.pages.live.viewmodel.LiveVM
import cs10.apps.travels.tracer.pages.live.viewmodel.WaitingVM
import cs10.apps.travels.tracer.pages.registry.editor.BusTravelEditor
import cs10.apps.travels.tracer.pages.registry.editor.CarTravelEditor
import cs10.apps.travels.tracer.pages.registry.editor.MetroTravelEditor
import cs10.apps.travels.tracer.pages.registry.editor.TrainTravelEditor
import cs10.apps.travels.tracer.utils.ColorUtils
import cs10.apps.travels.tracer.utils.Utils
import cs10.apps.travels.tracer.viewmodel.LocationVM
import cs10.apps.travels.tracer.viewmodel.RootVM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit
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

    // Custom Views
    private lateinit var liveWaitingView: LiveWaitingView

    // Switchers
    private lateinit var basicSwitcher: BasicSwitcher
    private lateinit var zoneSwitcher: BasicSwitcher

    // stages
    private val stagesAdapter = StagesAdapter()

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

        // tabs
        binding.roundedTabs.addOnTabSelectedListener(TabsListener())

        // other objects
        basicSwitcher = BasicSwitcher(binding.topBannerSwitcher)
        zoneSwitcher = BasicSwitcher(binding.zoneSwitcher, false)

        // live data
        observeLiveVM()

        waitingVM.stopHere.observe(viewLifecycleOwner) {
            liveWaitingView.setStopHere(it, requireActivity())
        }

        locationVM.getLiveData().observe(viewLifecycleOwner) {
            // updating animation
            if (basicSwitcher.getCurrentIteration() == 0){
                binding.updatingView.root.isVisible = true
                Handler(Looper.getMainLooper()).postDelayed({ binding.updatingView.root.isVisible = false}, 3000)
            }

            liveVM.recalculateDistances(it.location) { rootVM.disableLoading() }
            //if (liveWaitingView.isVisible())
            waitingVM.updateLocation(it.location)

            //val zone = ZoneData.getZoneUppercase(it.location)
            //binding.zoneInfo.text = zone
        }

        return binding.root
    }

    private fun showWaitingView(forceTabMov: Boolean = true){
        binding.travellingLayout.isVisible = false
        liveWaitingView.setVisibility(true)
        if (forceTabMov) updateTabs(true)

        resetViews()
        basicSwitcher.stop()
    }

    private fun showTravellingView(t: ColoredTravel){
        binding.travellingLayout.isVisible = true
        liveWaitingView.setVisibility(false)
        updateTabs(false)

        basicSwitcher.start()

        binding.linearProgressContent.adapter = stagesAdapter
        binding.linearProgressContent.layoutManager = LinearLayoutManager(requireContext())
        binding.lineTitle.text = t.lineInformation
        binding.buttonDrawing.setImageDrawable(Utils.getTypeDrawable(t.tipo, context))
        binding.topCardView.setCardBackgroundColor(t.color ?:
            ContextCompat.getColor(binding.root.context, ColorUtils.colorFor(t.linea, t.tipo, t.nombrePdaInicio))
        )

        rootVM.disableLoading()
    }

    private fun observeLiveVM() {
        liveVM.travel.observe(viewLifecycleOwner) {
            // si no estoy viajando, mostrar "esperando"
            if (it == null) showWaitingView()
            else showTravellingView(it)
        }

        liveVM.estData.observe(viewLifecycleOwner) {
            if (it == null || it.totalMinutes == 0) binding.trafficSub.text = null
            else {
                binding.trafficSub.text = String.format("El viaje normal dura %d minutos", it.totalMinutes)
                basicSwitcher.replaceContent(SwitcherText("speed", String.format("Histórico: %.1f km/h", it.speed)))
            }
        }

        liveVM.stages.observe(viewLifecycleOwner) {
            stagesAdapter.data.clear()
            stagesAdapter.data.addAll(it)
            stagesAdapter.notifyDataSetChanged()
        }

        liveVM.progress.observe(viewLifecycleOwner) { prog ->
            when (prog) {
                null -> {
                    //binding.linearPbar.isIndeterminate = true
                    //binding.pbar.progress = 0
                }
                // prog > 0.97 -> finishCurrentTravel()
                else -> {
                    //binding.linearPbar.isIndeterminate = false
                    val norm = prog.times(100).roundToInt()
                    //binding.linearPbar.progress = norm
                    binding.progressCardText.text = String.format("%d%%", norm)
                }
            }

            val avgD = liveVM.estData.value
            val currentTime = liveVM.minutesFromStart.value

            if (prog != null && avgD != null && currentTime != null) {
                val estimation = avgD.totalMinutes * prog
                val error = (currentTime - estimation).roundToInt()
                val absError = abs(error)

                if (absError > 3){
                    binding.trafficBanner.isVisible = true
                    //binding.deviationBanner.isVisible = false

                    if (error > 0){
                        binding.trafficTitle.text = String.format("Tráfico: %d minutos", absError)
                        binding.trafficCard.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.bus_414))
                        binding.trafficSub.setTextColor(ContextCompat.getColor(binding.root.context, android.R.color.holo_red_light))
                    } else {
                        binding.trafficTitle.text = String.format("Ventaja: %d minutos", absError)
                        binding.trafficCard.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.bus_500))
                        binding.trafficSub.setTextColor(ContextCompat.getColor(binding.root.context, android.R.color.holo_green_light))
                    }
                } else binding.trafficBanner.isVisible = false
            } else binding.trafficBanner.isVisible = false
        }

        /*liveVM.progressEntries.observe(viewLifecycleOwner) {
            binding.progressChart.chart?.axisValuesOverrider = AxisValuesOverrider.fixed(minY = 0.0f, maxY = 1.0f, minX = 0.0f, maxX = 1.0f)
            binding.progressChart.setModel(entryModelOf(it))
        }*/

        liveVM.endDistance.observe(viewLifecycleOwner) {
            //basicSwitcher.replaceContent(String.format("Destino a %.1f km", it ?: 0.0), 3)
            basicSwitcher.replaceContent(SwitcherText("endDist", String.format("Destino a %.1f km", it ?: 0.0)))

            liveVM.stagedTravel?.let { st ->
                val linearSpeed = st.getLinearSpeed()
                if (linearSpeed != null) {
                    basicSwitcher.replaceContent(SwitcherText("linearSpeed", String.format("Velocidad: %.1f km/h", linearSpeed)))
                }
            }
        }

        liveVM.finishData.observe(viewLifecycleOwner) {
            if (it) finishCurrentTravel()
        }

        liveVM.countdown.liveData.observe(viewLifecycleOwner) {
            if (it < 600) binding.nearMeTitle.text = String.format("Llegando en %d' %d\"", it / 60, it % 60)
            else binding.nearMeTitle.text = String.format("Llegando en %d min." , it / 60)
        }

        liveVM.angle.observe(viewLifecycleOwner) {
            if (it == null) binding.compass.isVisible = false
            else {
                binding.compass.rotation = 45f - it.toFloat()
                binding.compass.isVisible = true

                if (Compass.isForward(it)) {
                    binding.compassText.text = "Sigue derecho"
                } else if (it > 0 && it < 90) {
                    val angleToRight = 90f - it.toFloat()
                    binding.compassText.text = String.format("Girar a la derecha %.0f°", angleToRight)
                } else if (it > 90 && it < 180) {
                    val angleToLeft = it.toFloat() - 90f
                    binding.compassText.text = String.format("Girar a la izquierda %.0f°", angleToLeft)
                }
            }
        }

        liveVM.minutesToEnd.observe(viewLifecycleOwner) {
            binding.shareBtn.isVisible = it != null
            binding.editBtn.isVisible = it != null

            if (it == null) binding.nearMeTitle.text = null
            else {
                val seconds = (it * 60).roundToInt()
                val eta = Calendar.getInstance().apply { add(Calendar.SECOND, seconds) }

                // ui
                // binding.minutesLeft.text = String.format("%d' %.0f\"", it.toInt(), (it % 1.0) * 60)
                // binding.etaInfo.text = "Llegarías a las " + Utils.hourFormat(eta)
                binding.finishBtn.isVisible = it < 10

                // new design
                //binding.nearMeTitle.text = String.format("Llegarías a las %s", Utils.hourFormat(eta))
                //binding.endTime.text = Utils.hourFormat(eta)

                // arrival notification
                liveVM.travel.value?.id?.let { id ->
                    val prefs = requireContext().getSharedPreferences("eta_notified", Context.MODE_PRIVATE)
                    val scheduled = prefs.getLong("last_id", -1)
                    if (scheduled != id) {
                        NotificationCenter().scheduleAskNotification(requireContext(), TimeUnit.MINUTES.toMillis(it.toLong() - 4L))
                        if (it > 20) NotificationCenter().scheduleNotifyOthersReminder(requireContext(), TimeUnit.MINUTES.toMillis(it.toLong() + 10L))
                        prefs.edit().putLong("last_id", id).apply()
                    }
                }

                // next combination
                //binding.nextTravelInfo.isVisible = false

                /*
                liveVM.nextTravel.value?.let { nextT ->
                    val etaNext = Calendar2.getETA(eta, nextT.duration + 15)
                    binding.nextTravelInfo.text = "Combinación a ${nextT.nombrePdaFin} (${Utils.hourFormat(etaNext)})"
                }*/
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
            zoneSwitcher.clear()

            if (it.isNullOrEmpty()) zoneSwitcher.purge()

            //it?.forEach { nz ->
            //    zoneSwitcher.addContent("En ${nz.minutesLeft}' por ${nz.zone.name}")
            //}

            waitingVM.stopHere.value?.let { p ->
                //zoneSwitcher.replaceContent("Ahora: ${p.nombre}", 0)
                zoneSwitcher.replaceContent(SwitcherText("now", "Ahora: ${p.nombre}"), 0)
                it?.removeFirstOrNull()
                liveVM.vibrate(140)

                // check if add
                liveVM.mediumStopsManager?.let { msm ->
                    val candidate = p.nombre
                    val check = msm.checkIfCanAdd(candidate, liveVM.angle.value)
                    val currentStage = liveVM.stagedTravel?.getCurrentStage()

                    if (check && currentStage != null) lifecycleScope.launch(Dispatchers.IO) {
                        val db = MiDB.getInstance(activity)
                        val question = msm.getAddQuestion(candidate, currentStage, db)

                        launch(Dispatchers.Main) {
                            val builder = AlertDialog.Builder(activity)
                                .setTitle("Sugerencia")
                                .setMessage(question)
                                .setNeutralButton("No", null)
                                .setPositiveButton("Sí") { _, _ ->
                                    // lifecycleScope es obligatorio para que se ejecute
                                    lifecycleScope.launch(Dispatchers.IO) {
                                        val success = msm.add(candidate, currentStage, db)

                                        launch(Dispatchers.Main) {
                                            if (success) {
                                                Toast.makeText(activity, "Añadido con éxito", Toast.LENGTH_SHORT).show()
                                                reloadData()
                                            } else Toast.makeText(activity, "Fallo al agregar intermedio", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }

                            builder.show()
                        }
                    }
                }
            }

            it?.firstOrNull()?.let {nz ->
                zoneSwitcher.addContent("En ${nz.minutesLeft}' por ${nz.zone.name}")
            }

            zoneSwitcher.start()
        }

        // DEC 2022: EXPECTED RATING
        liveVM.rate.observe(viewLifecycleOwner) {
            if (it == null) binding.rateText.text = "--"
            else binding.rateText.text = String.format("%.2f", it)

            // emoji icon
            val face = HappyRater.getDrawableByRating(it?.toInt() ?: 3)
            binding.trafficIv.setImageDrawable(ContextCompat.getDrawable(binding.root.context, face))
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

    }

    override fun onResume() {
        super.onResume()

        resetViews()
        reloadData()

        // New design
        basicSwitcher.start()
    }

    override fun onStop() {
        super.onStop()
        liveVM.eraseAll()
        waitingVM.reset()
        basicSwitcher.clear()
        zoneSwitcher.purge()
    }

    // ------------------------------- DONE -------------------------------

    private fun reloadData(){
        liveVM.findLastTravel(locationVM) { rootVM.disableLoading() }
    }

    private fun resetViews() {
        binding.travellingLayout.isVisible = false
        binding.finishBtn.isVisible = false
        binding.nearMeTitle.text = null
        zoneSwitcher.clear()
        basicSwitcher.clear()
        binding.trafficBanner.isVisible = false
        //binding.linearPbar.isIndeterminate = true
        binding.rateText.text = "--"
        binding.progressCardText.text = "--"
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

                if (line != null){
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
            // if (viaje.tipo == TransportType.CAR.ordinal) return

            val intent = Intent(activity,
                when(TransportType.fromOrdinal(viaje.tipo)){
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

    private fun updateTabs(waiting: Boolean){
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

        fun checkPosition(tab: TabLayout.Tab){
            if (tab.position > 0 && liveVM.travel.value == null) {
                binding.roundedTabs.getTabAt(0)?.select()
                //binding.roundedTabs.setScrollPosition(0, 0f, true)
            }

            if (tab.position == 0 && liveVM.travel.value != null) {
                binding.roundedTabs.getTabAt(1)?.select()
                //binding.roundedTabs.setScrollPosition(1, 0f, true)
            }
        }
    }
}