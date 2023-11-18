package cs10.apps.travels.tracer.modules.live.utils

import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.enums.TransportType
import cs10.apps.travels.tracer.model.Viaje
import cs10.apps.travels.tracer.modules.live.entity.MediumStop
import cs10.apps.travels.tracer.modules.live.model.Stage

class MediumStopsManager(val travel: Viaje) {
    val start = travel.nombrePdaInicio
    val end = travel.nombrePdaFin
    var stops = mutableListOf(start, end)
    private val candidatesAsked = mutableListOf<String>()

    suspend fun buildStops(db: MiDB) : MediumStopsManager {
        if (stops.size == 2){
            if (travel.tipo == TransportType.BUS.ordinal){
                travel.linea?.let {
                    // buildStopsForBusRamal(it, travel.ramal, travel.nombrePdaFin, db)
                    val all = db.safeStopsDao().getMediumStopsCreatedForBusTo(it, travel.ramal, travel.nombrePdaFin)
                    buildStopsFromData(all, db)
                }
            } else {
                // buildStopsForType(travel.tipo, travel.nombrePdaFin, db)
                val allMS = db.safeStopsDao().getMediumStopsCreatedForTypeTo(travel.tipo, travel.nombrePdaFin)
                buildStopsFromData(allMS, db)
            }
        }

        return this
    }

    suspend fun rebuildStops(db: MiDB) {
        stops = mutableListOf(start, end)
        buildStops(db)
    }

    private suspend fun buildStopsFromData(allMS: List<MediumStop>, db: MiDB){
        var lastMS: MediumStop? = null
        var finish = false
        var i = 0

        while (!finish){
            val target = allMS.filter { it.prev == stops[i] }
            if (target.size > 1) {
                for(j in 1 until target.size) db.safeStopsDao().deleteMediumStop(target[j])
                throw Exception("Multiple target for ${stops[i]}, deleted all but ${target.first()}")
            }

            if (target.isEmpty()) finish = true
            else {
                i++
                stops.add(i, target.first().name)       // en la posición correcta
                lastMS = target.first()
            }
        }

        lastMS?.let {
            if (it.next != end) throw Exception("Incomplete path: last 2 stops unmatched - $allMS")
        }
    }

    fun checkIfCanAdd(candidate: String): Boolean {
        if (countStops() >= 4) return false
        if (candidate == start || candidate == end) return false
        if (stops.contains(candidate)) return false
        return !candidatesAsked.contains(candidate)
    }

    suspend fun getAddQuestion(candidate: String, currentStage: Stage, db: MiDB): String {
        val prevName = db.safeStopsDao().getNameByCoords(currentStage.start.getX(), currentStage.start.getY())
        val nextName = db.safeStopsDao().getNameByCoords(currentStage.end.getX(), currentStage.end.getY())
        val msg = "¿Quieres añadir $candidate como una parada intermedia entre $prevName y $nextName?"

        // avoid to ask again...
        candidatesAsked.add(candidate)
        return msg
    }

    suspend fun add(candidate: String, currentStageStart: String, currentStageEnd: String, db: MiDB): Boolean {
        val ms = MediumStop(0, type = travel.tipo, line = travel.linea, ramal = travel.ramal,
            prev = currentStageStart, name = candidate,
            next = currentStageEnd, destination = travel.nombrePdaFin)

        val place = stops.indexOf(currentStageEnd)
        if (place < 0) return false

        db.safeStopsDao().insertMediumStop(ms)
        stops.add(place, candidate)

        // FIX: ALSO MODIFY NEXT MEDIUM STOP
        if (travel.tipo == TransportType.BUS.ordinal) travel.linea?.let {
            db.safeStopsDao().updateNextBusMediumStop(it, travel.ramal, travel.nombrePdaFin, ms.next, ms.name)
        } else {
            db.safeStopsDao().updateNextMediumStopForType(travel.tipo, travel.nombrePdaFin, ms.next, ms.name)
        }

        return true
    }

    suspend fun add(candidate: String, currentStage: Stage, db: MiDB): Boolean {
        val prevName = db.safeStopsDao().getNameByCoords(currentStage.start.getX(), currentStage.start.getY())
        val nextName = db.safeStopsDao().getNameByCoords(currentStage.end.getX(), currentStage.end.getY())
        if (prevName == null || nextName == null) return false

        return add(candidate, prevName, nextName, db)
    }

    fun countStops() : Int = stops.size
}