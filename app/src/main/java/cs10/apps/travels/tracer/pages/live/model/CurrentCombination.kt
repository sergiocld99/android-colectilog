package cs10.apps.travels.tracer.pages.live.model

import cs10.apps.travels.tracer.model.Viaje
import cs10.apps.travels.tracer.model.joins.ColoredTravel

class CurrentCombination(
    val firstTravel: ColoredTravel,
    val referenceCombination: Viaje,
    val expectedFirstTravelFinishTime: Int) {

    var actualSecondTravel: Viaje? = null
}