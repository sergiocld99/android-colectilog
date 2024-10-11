package cs10.apps.travels.tracer.pages.live.utils

import cs10.apps.common.android.Compass
import cs10.apps.travels.tracer.common.enums.TransportType
import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.model.Viaje
import cs10.apps.travels.tracer.pages.live.entity.MediumStop
import cs10.apps.travels.tracer.pages.live.model.Stage

class MediumStopsManager(val travel: Viaje, private val allowAutoDeletion: Boolean = false) {
    val start = travel.nombrePdaInicio
    val end = travel.nombrePdaFin
    var stops = mutableListOf(start, end)
    private val candidatesAsked = mutableListOf<String>()

    suspend fun getMediumStopsFromDb(db: MiDB) : List<MediumStop> {
        return if (travel.tipo == TransportType.BUS.ordinal){
            if (travel.linea == null) throw Exception("Line should be defined for bus travels")
            db.safeStopsDao().getMediumStopsCreatedForBusTo(travel.linea!!, travel.ramal, travel.nombrePdaFin)
        } else {
            db.safeStopsDao().getMediumStopsCreatedForTypeTo(travel.tipo, travel.nombrePdaFin)
        }
    }

    suspend fun buildStops(db: MiDB) : MediumStopsManager {
        if (stops.size == 2){
            val allMS = getMediumStopsFromDb(db)
            buildStopsFromData(allMS, db)
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

        // maximum size is 4 stops
        if (allowAutoDeletion) while (countStops() > 4) {
            val targetName = getRandomMediumStopName()
            val targetMS = allMS.first { it.name == targetName }

            // update previous to target (if null, prev is start)
            val prevMS = allMS.firstOrNull { it.next == targetName }
            if (prevMS != null){
                prevMS.next = targetMS.next
                db.safeStopsDao().updateMediumStop(prevMS)
            }

            // update next to target (if null, next is end)
            val nextMS = allMS.firstOrNull { it.prev == targetName }
            if (nextMS != null){
                nextMS.prev = targetMS.prev
                db.safeStopsDao().updateMediumStop(nextMS)
            }

            // delete target
            db.safeStopsDao().deleteMediumStop(targetMS)
            stops.remove(targetName)
        }
    }

    fun checkIfCanAdd(candidate: String, currentAngle: Double?): Boolean {
        if (currentAngle != null && Compass.isForward(currentAngle)) return false
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
        updateNextMediumStop(ms, db)

        return true
    }

    private suspend fun updateNextMediumStop(ms: MediumStop, db: MiDB) {
        if (travel.tipo == TransportType.BUS.ordinal) travel.linea?.let {
            db.safeStopsDao().updateNextBusMediumStop(it, travel.ramal, travel.nombrePdaFin, ms.next, ms.name)
        } else {
            db.safeStopsDao().updateNextMediumStopForType(travel.tipo, travel.nombrePdaFin, ms.next, ms.name)
        }
    }

    suspend fun add(candidate: String, currentStage: Stage, db: MiDB): Boolean {
        val prevName = db.safeStopsDao().getNameByCoords(currentStage.start.getX(), currentStage.start.getY())
        val nextName = db.safeStopsDao().getNameByCoords(currentStage.end.getX(), currentStage.end.getY())
        if (prevName == null || nextName == null) return false

        return add(candidate, prevName, nextName, db)
    }

    private fun getRandomMediumStopName() : String {
        var chosen = stops.random()
        while (chosen == start || chosen == end) chosen = stops.random()
        return chosen
    }

    fun countStops() : Int = stops.size

    fun checkIfShouldDelete(candidate: String, currentAngle: Double?) : Boolean {
        if (currentAngle == null) return false
        if (!Compass.isForward(currentAngle)) return false
        if (!stops.contains(candidate)) return false
        return true
    }

    suspend fun deleteIfShould(candidate: String, db: MiDB): Boolean {
        val mediumStops = getMediumStopsFromDb(db)
        val deletionTarget = mediumStops.first { it.name == candidate }
        db.safeStopsDao().deleteMediumStop(deletionTarget)

        // Fix former stop
        mediumStops.find { it.next == candidate }?.let {
            db.safeStopsDao().updateMediumStopNextField(it.id, deletionTarget.next)
        }

        // Fix following stop
        mediumStops.find { it.prev == candidate }?.let {
            db.safeStopsDao().updateMediumStopPrevField(it.id, deletionTarget.prev)
        }

        stops.remove(candidate)
        return true
    }
}