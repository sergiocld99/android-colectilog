package cs10.apps.travels.tracer.pages.manage_lines.utils

import cs10.apps.travels.tracer.model.joins.TravelStats
import cs10.apps.travels.tracer.pages.manage_lines.model.CommonLineInfo

object SpeedCalculator {

    fun calculate(item: CommonLineInfo, stats: List<TravelStats>) {
        if (stats.isEmpty()) item.speed = null
        else {
            var sum = 0.0
            stats.forEach { stat -> sum += stat.calculateSpeedInKmH() }
            item.speed = sum / stats.size
        }
    }
}