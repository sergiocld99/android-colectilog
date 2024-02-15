package cs10.apps.travels.tracer.modules.lines.model

import cs10.apps.travels.tracer.model.lines.HourBusStat
import cs10.apps.travels.tracer.modules.lines.entity.FrequentTravel

data class HourChartData(val statList: List<HourBusStat>, val frequentTravel: FrequentTravel)