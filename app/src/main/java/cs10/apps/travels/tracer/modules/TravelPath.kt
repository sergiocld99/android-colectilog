package cs10.apps.travels.tracer.modules

import cs10.apps.travels.tracer.model.Point
import cs10.apps.travels.tracer.model.Zone
import cs10.apps.travels.tracer.model.joins.TravelStats

class TravelPath {

    companion object {

        /**
         * @param travel the stats that contains start and end coordinates
         * @param howMany positive quantity of points to get
         * @return a list of howMany coordinates between start and end of travel (including them)
         */
        fun getPointsForTravel(travel: TravelStats, howMany: Int) : List<Point> {
            val xDiff = travel.end_x - travel.start_x
            val yDiff = travel.end_y - travel.start_y
            val delta = 1.0 / howMany

            // result buffer
            val points = mutableListOf<Point>()

            // calculate points between start and end
            for (i in 1..howMany){
                val percentage = i * delta
                val newX = travel.start_x + percentage * xDiff
                val newY = travel.start_y + percentage * yDiff
                points.add(Point(newX, newY))
            }

            return points
        }

    }
}

data class PathResult(
    val zonesMatched: MutableList<Zone> = mutableListOf(),
    val unknownPoints: MutableList<Point> = mutableListOf()
)