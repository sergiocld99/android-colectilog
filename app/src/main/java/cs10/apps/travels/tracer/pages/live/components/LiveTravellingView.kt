package cs10.apps.travels.tracer.pages.live.components

import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import cs10.apps.common.android.Compass
import cs10.apps.rater.HappyRater
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.databinding.ContentLiveTravellingBinding
import cs10.apps.travels.tracer.model.NextZone
import cs10.apps.travels.tracer.model.Parada
import cs10.apps.travels.tracer.model.joins.ColoredTravel
import cs10.apps.travels.tracer.pages.live.adapter.StagesAdapter
import cs10.apps.travels.tracer.pages.live.model.EstimationData
import cs10.apps.travels.tracer.pages.live.model.Stage
import cs10.apps.travels.tracer.pages.live.model.StagedTravel
import cs10.apps.travels.tracer.pages.live.model.SwitcherText
import cs10.apps.travels.tracer.utils.ColorUtils
import cs10.apps.travels.tracer.utils.Utils
import kotlin.math.abs
import kotlin.math.roundToInt

class LiveTravellingView(private val binding: ContentLiveTravellingBinding) :
    LiveSubView(binding.root) {

    private val basicSwitcher: BasicSwitcher = BasicSwitcher(binding.topBannerSwitcher)
    private val zoneSwitcher: BasicSwitcher = BasicSwitcher(binding.zoneSwitcher, false)
    private val stagesAdapter = StagesAdapter()

    fun setTravel(t: ColoredTravel) {
        binding.linearProgressContent.adapter = stagesAdapter
        binding.linearProgressContent.layoutManager = LinearLayoutManager(getContext())
        binding.lineTitle.text = t.lineInformation
        binding.buttonDrawing.setImageDrawable(Utils.getTypeDrawable(t.tipo, getContext()))
        binding.topCardView.setCardBackgroundColor(
            t.color ?: ContextCompat.getColor(
                getContext(),
                ColorUtils.colorFor(t.linea, t.tipo, t.nombrePdaInicio)
            )
        )
    }

    override fun hide() {
        super.hide()
        basicSwitcher.stop()
    }

    override fun show() {
        super.show()
        basicSwitcher.start()
    }

    fun onResume() {
        if (isVisible()) return
        basicSwitcher.start()
    }

    fun onStop() {
        if (isVisible()) return
        basicSwitcher.clear()
        zoneSwitcher.purge()
    }

    fun reset() {
        binding.finishBtn.isVisible = false
        binding.nearMeTitle.text = null
        zoneSwitcher.clear()
        basicSwitcher.clear()
        binding.trafficBanner.isVisible = false
        binding.rateText.text = "--"
        binding.progressCardText.text = "--"
    }

    fun onUpdateLocation() {
        if (basicSwitcher.getCurrentIteration() > 1) return
        binding.updatingView.root.isVisible = true

        Handler(Looper.getMainLooper()).postDelayed({
            binding.updatingView.root.isVisible = false
        }, 3000)
    }

    fun onGetHistorical(data: EstimationData?) {
        if (data == null || data.totalMinutes == 0) {
            binding.trafficSub.text = null
            return
        }

        binding.trafficSub.text =
            String.format("El viaje normal dura %d minutos", data.totalMinutes)

        basicSwitcher.replaceContent(
            SwitcherText(
                "speed",
                String.format("Histórico: %.1f km/h", data.speed)
            )
        )
    }

    fun onUpdateProgress(prog: Double?, avgD: EstimationData?, currentTime: Double?) {
        if (prog == null) return

        val norm = prog.times(100).roundToInt()
        binding.progressCardText.text = String.format("%d%%", norm)
        binding.trafficBanner.isVisible = false

        if (avgD == null || currentTime == null) return
        val estimation = avgD.totalMinutes * prog
        val error = (currentTime - estimation).roundToInt()
        val absError = abs(error)

        if (absError > 3) {
            binding.trafficBanner.isVisible = true

            if (error > 0) {
                binding.trafficTitle.text = String.format("Tráfico: %d minutos", absError)
                binding.trafficCard.setCardBackgroundColor(
                    ContextCompat.getColor(
                        getContext(),
                        R.color.bus_414
                    )
                )
                binding.trafficSub.setTextColor(
                    ContextCompat.getColor(
                        getContext(),
                        android.R.color.holo_red_light
                    )
                )
            } else {
                binding.trafficTitle.text = String.format("Ventaja: %d minutos", absError)
                binding.trafficCard.setCardBackgroundColor(
                    ContextCompat.getColor(
                        getContext(),
                        R.color.bus_500
                    )
                )
                binding.trafficSub.setTextColor(
                    ContextCompat.getColor(
                        getContext(),
                        android.R.color.holo_green_light
                    )
                )
            }
        }
    }

    fun onUpdateDistanceToDestination(distance: Double?, stagedTravel: StagedTravel?) {
        basicSwitcher.replaceContent(
            SwitcherText(
                "endDist",
                String.format("Destino a %.1f km", distance ?: 0.0)
            )
        )

        stagedTravel?.getLinearSpeed()?.let {
            basicSwitcher.replaceContent(
                SwitcherText(
                    "linearSpeed",
                    String.format("Velocidad: %.1f km/h", it)
                )
            )
        }
    }

    fun onUpdateCountdown(secondsLeft: Int) {
        val minutes = secondsLeft / 60

        if (minutes < 10) {
            val secondsMod = secondsLeft % 60

            binding.finishBtn.isVisible = true
            binding.nearMeTitle.text =
                String.format("Llegando en %d' %d\"", minutes, secondsMod)
        } else {
            binding.finishBtn.isVisible = false
            binding.nearMeTitle.text = String.format("Llegando en %d min.", minutes)
        }
    }

    fun onUpdateAngle(angle: Double) {
        binding.compass.rotation = 45f - angle.toFloat()
        binding.compass.isVisible = true

        if (Compass.isForward(angle)) {
            binding.compassText.text = getContext().getString(R.string.continue_straight)
        } else if (angle > 0 && angle < 90) {
            val angleToRight = 90f - angle.toFloat()
            binding.compassText.text = String.format("Girar a la derecha %.0f°", angleToRight)
        } else if (angle > 90 && angle < 180) {
            val angleToLeft = angle.toFloat() - 90f
            binding.compassText.text = String.format("Girar a la izquierda %.0f°", angleToLeft)
        }
    }

    fun onUpdateNextZones(list: MutableList<NextZone>?, stopHere: Parada?) {
        zoneSwitcher.clear()
        if (list.isNullOrEmpty()) zoneSwitcher.purge()

        stopHere?.let { p ->
            zoneSwitcher.replaceContent(SwitcherText("now", "Ahora: ${p.nombre}"), 0)
            list?.removeFirstOrNull()
        }

        list?.firstOrNull()?.let { nz ->
            zoneSwitcher.addContent("En ${nz.minutesLeft}' por ${nz.zone.name}")
        }

        zoneSwitcher.start()
    }

    fun onUpdateRate(rate: Double?) {
        if (rate == null) binding.rateText.text = "--"
        else binding.rateText.text = String.format("%.2f", rate)

        // emoji icon
        val face = HappyRater.getDrawableByRating(rate?.toInt() ?: 3)
        binding.trafficIv.setImageDrawable(
            ContextCompat.getDrawable(
                getContext(),
                face
            )
        )
    }

    fun onUpdateStages(list: List<Stage>) {
        stagesAdapter.data.clear()
        stagesAdapter.data.addAll(list)
        stagesAdapter.notifyDataSetChanged()
    }

    fun getShareBtn() = binding.shareBtn
    fun getEditBtn() = binding.editBtn
    fun getDoneBtn() = binding.finishBtn
}