package cs10.apps.travels.tracer.pages.registry.creator.model

import cs10.apps.travels.tracer.model.Viaje

data class LikelyTravel(val viaje: Viaje, val startIndex: Int, val endIndex: Int)