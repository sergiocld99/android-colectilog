package cs10.apps.travels.tracer.pages.live.components

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import cs10.apps.travels.tracer.databinding.ContentLiveCombiningBinding
import cs10.apps.travels.tracer.pages.live.adapter.StagesAdapter
import cs10.apps.travels.tracer.pages.live.model.CurrentCombination
import cs10.apps.travels.tracer.pages.live.model.Stage
import cs10.apps.travels.tracer.pages.live.model.SwitcherText
import cs10.apps.travels.tracer.utils.ColorUtils
import cs10.apps.travels.tracer.utils.Utils
import kotlin.math.roundToInt

class LiveCombiningView(private val binding: ContentLiveCombiningBinding) : LiveSubView(binding.root) {
    private val basicSwitcher: BasicSwitcher = BasicSwitcher(binding.topBannerSwitcher)
    private val zoneSwitcher: BasicSwitcher = BasicSwitcher(binding.zoneSwitcher, false)
    private val stagesAdapter = StagesAdapter()

    fun setCombination(currentCombination: CurrentCombination) {
        binding.linearProgressContent.adapter = stagesAdapter
        binding.linearProgressContent.layoutManager = LinearLayoutManager(getContext())

        // 1.0: first travel info
        val t = currentCombination.firstTravel
        binding.lineTitle.text = t.lineInformation
        binding.buttonDrawing.setImageDrawable(Utils.getTypeDrawable(t.tipo, getContext()))
        binding.topCardView.setCardBackgroundColor(
            t.color ?: ContextCompat.getColor(
                getContext(),
                ColorUtils.colorFor(t.linea, t.tipo, t.nombrePdaInicio)
            )
        )

        // 2: combination info
        binding.nearMeTitle.text = String.format("Combinarás con Línea %d", currentCombination.referenceCombination.linea)
        zoneSwitcher.replaceContent(SwitcherText(
            "ramal", String.format("Ramal %s", currentCombination.referenceCombination.ramal ?: "común")
        ))
    }

    override fun hide() {
        super.hide()
        basicSwitcher.stop()
    }

    override fun show() {
        super.show()
        basicSwitcher.start()
    }

    fun onUpdateStages(list: List<Stage>) {
        stagesAdapter.data.clear()
        stagesAdapter.data.addAll(list)
        stagesAdapter.notifyDataSetChanged()
    }

    fun onUpdateDistanceToCombination(distance: Double) {
        val roundedMeters = distance.times(10).roundToInt().times(100)

        basicSwitcher.replaceContent(SwitcherText(
            "distance", String.format("Bajar en %d metros", roundedMeters)
        ))
    }

    fun reset() {
        binding.nearMeTitle.text = null
        zoneSwitcher.clear()
        basicSwitcher.clear()
        binding.progressCardText.text = "--"
    }
}