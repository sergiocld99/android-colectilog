package cs10.apps.travels.tracer.pages.live.model

import cs10.apps.common.android.Localizable
import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.model.Viaje
import cs10.apps.travels.tracer.pages.live.utils.MediumStopsManager
import cs10.apps.travels.tracer.utils.Utils
import kotlin.math.roundToInt

class StagedTravel(val stages: List<Stage>) {
    val totalKmDist = (0.0).plus(stages.sumOf { it.kmDistance })
    val start = stages[0].start
    val end = stages.last().end

    // how much represents this stage of total
    val relativeStageProg = stages.map { 100.0 * it.kmDistance / totalKmDist }

    fun calculateCurrentStage(currentPos: Localizable) : Stage {
        val currentProg = currentProgress(currentPos)
        var found: Stage? = null

        for (s in stages){
            if (found != null) s.progress = 0
            else {
                val d1 = s.end.coordsDistanceTo(start)
                val d2 = s.end.coordsDistanceTo(end)
                val endProg = 100 * d1 / (d1+d2)

                if (endProg < currentProg) s.progress = 100
                else {
                    s.updateProgressFor(currentPos)
                    found = s
                }
            }
        }

        found?.let { return it }
        return stages.last()
    }

    fun getCurrentStage(): Stage? {
        return stages.find { it.progress in 10..90 }
    }

    fun currentProgress(currentPos: Localizable) : Double {
        val startDist = currentPos.coordsDistanceTo(start)
        val endDist = currentPos.coordsDistanceTo(end)
        return 100 * startDist / (startDist + endDist)
    }

    /**
     * Ejecutar una vez calculado el stage actual
     */
    fun currentKmDistanceToFinish(currentPos: Localizable) : Double {
        return stages.sumOf {
            when {
                it.isFinished() -> 0.0
                it.isStarted() -> currentPos.kmDistanceTo(it.end)
                else -> it.kmDistance
            }
        }
    }

    fun getLeftKmDistance(): Double {
        return stages.sumOf {
            when {
                it.isFinished() -> 0.0
                it.isStarted() -> it.getLeftDistance() ?: 0.0
                else -> it.kmDistance
            }
        }
    }

    fun updateStagesETA(minutesLeftToEach: List<Double>) {
        val currentTs = Utils.getCurrentTs()

        stages.forEachIndexed { index, stage ->
            stage.endTime = (currentTs +  minutesLeftToEach[index].roundToInt()) % 1440
        }
    }

    /**
     * @return travel progress between 0 and 100 at the end of each stage
     */
    fun globalProgressAtStagesEnd() : List<Double> {
        val results = mutableListOf<Double>()
        var sum = 0.0

        for (prog in relativeStageProg){
            sum += prog
            results.add(sum)
        }

        return results
    }

    fun calculateCloserPoint(currentPos: Localizable) : Localizable {
        var minDist = currentPos.coordsDistanceTo(start)
        var minPoint = start

        for (s in stages){
            val dist = currentPos.coordsDistanceTo(s.end)
            if (dist < minDist) {
                minDist = dist
                minPoint = s.end
            }
        }

        return minPoint
    }

    fun getLinearSpeed(): Double? {
        if (stages.isNotEmpty()){
            val endTs = stages.last().endTime
            val startTs = stages.first().startTime
            if (startTs != null && endTs != null) {
                var totalMinutes = endTs - startTs
                if (totalMinutes < 0) totalMinutes += 24 * 60

                val h = totalMinutes / 60.0
                return totalKmDist / h
            }
        }

        return null
    }

    companion object {

        suspend fun from(t: Viaje, db: MiDB) : StagedTravel {
            val mediumStopsManager = MediumStopsManager(t)
            mediumStopsManager.buildStops(db)

            val st: StagedTravel = if (mediumStopsManager.stops.size == 2){
                // classic mode
                from(t.nombrePdaInicio, t.nombrePdaFin, db)
            } else {
                // find locations
                val locations = mutableListOf<Localizable>()

                for (stopName in mediumStopsManager.stops){
                    db.safeStopsDao().getStopByName(stopName)?.let { locations.add(it) }
                }

                // build stages
                withStops(locations.toTypedArray())
            }

            // set global start time
            st.stages[0].startTime = t.startHour * 60 + t.startMinute
            return st
        }

        fun from(startName: String, endName: String, db: MiDB) : StagedTravel {
            val start = db.paradasDao().getByName(startName)
            val end = db.paradasDao().getByName(endName)

            if (start.nombre.contains("Adrogué") && end.nombre.contains("Claypole")){
                db.safeStopsDao().getStopByName("Estación Burzaco")?.let {
                    return withStops(arrayOf(start, it, end))
                }

                return defaultSt(start, end)
            }

            /*
            if (start.nombre == "Cruce Varela" && end.nombre == "Av. 1 y 48") {
                val alpargatas = db.safeStopsDao().getStopByName("Alpargatas")
                val pzaItalia = db.safeStopsDao().getStopByName("Plaza Italia")
                val terminal = db.safeStopsDao().getStopByName("Terminal La Plata")
                if (alpargatas == null || pzaItalia == null || terminal == null) return defaultSt(start, end)
                return withStops(arrayOf(start, alpargatas, pzaItalia, terminal, end))
            }

            if (start.nombre == "Estación La Plata" && end.nombre == "Estación Varela"){
                val bera = db.safeStopsDao().getStopByName("Estación Berazategui")
                bera?.let { return withStops(arrayOf(start, it, end)) }
                return defaultSt(start, end)
            }

            if (start.nombre == "Dorrego" && end.nombre == "Colegio de Adrogué"){
                val temperley = db.safeStopsDao().getStopByName("Cruce Varela")
                temperley?.let { return withStops(arrayOf(start, it, end)) }
                return defaultSt(start, end)
            }*/

            return defaultSt(start, end)
        }

        fun withStops(pdas: Array<Localizable>) : StagedTravel {
            if (pdas.size < 2) throw Exception("Origin and destination undefined")
            val stages = mutableListOf<Stage>()
            val n = pdas.size

            for (i in 1 until n) stages.add(Stage(pdas[i-1], pdas[i]))
            return StagedTravel(stages)
        }

        private fun defaultSt(start: Localizable, end: Localizable) : StagedTravel {
            return StagedTravel(listOf(Stage(start, end)))
        }
    }
}