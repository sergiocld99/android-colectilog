package cs10.apps.travels.tracer.pages.fab.db

import cs10.apps.travels.tracer.common.constants.Quantities
import cs10.apps.travels.tracer.common.enums.TransportType
import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.model.Parada
import cs10.apps.travels.tracer.model.Point
import cs10.apps.travels.tracer.model.lines.CustomBusLine
import cs10.apps.travels.tracer.pages.fab.tools.LimitedSet
import cs10.apps.travels.tracer.utils.Utils

class LikelyBusFinder(private val currentLocation: Point, private val db: MiDB) {

    fun predict(): List<CustomBusLine> {
        val allStops = db.paradasDao().all
        Utils.orderByProximity(allStops, currentLocation.getX(), currentLocation.getY())

        return predict(allStops).mapNotNull { db.linesDao().getByNumber(it) }
    }

    private fun predict(sortedStopsByProximity: List<Parada>, maxSize: Int = 3): List<Int> {
        val lineCandidates = LimitedSet<Int>(maxSize)
        val stopNames = sortedStopsByProximity.subList(0, Quantities.NEAREST_STOPS_SEARCH_LIMIT).map { it.nombre }

        for (p in stopNames) {
            val viajes = db.viajesDao().getLikelyTravelsFromUsingType(p, TransportType.BUS.ordinal)
            val linesFromIt = viajes.mapNotNull { it.linea }
            lineCandidates.addAll(linesFromIt)

            if (lineCandidates.isFull()) break
        }

        return lineCandidates.sorted()
    }
}