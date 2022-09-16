package cs10.apps.travels.tracer.viewmodel

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cs10.apps.common.android.Calendar2
import cs10.apps.common.android.Clock
import cs10.apps.travels.tracer.Utils
import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.model.Viaje
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.pow

class LiveVM : ViewModel() {

    val travel = MutableLiveData<Viaje?>()
    val toggle = MutableLiveData(false)
    val nextTravel = MutableLiveData<Viaje?>()

    // distances in metres
    private val startDistance = MutableLiveData<Double?>()
    private val endDistance = MutableLiveData<Double?>()

    // time in minutes
    val minutesFromStart = MutableLiveData<Int?>()
    val minutesToEnd = MutableLiveData<Int?>()
    val averageDuration = MutableLiveData<Int?>()

    // speed in km/h
    val speed = MutableLiveData<Double?>()

    // progress (between 0 and 1)
    val progress = MutableLiveData<Double?>()

    // timer 1: update minutes from start every 30 seconds
    private val minuteClock = Clock({
        travel.value?.let { t ->
            val startTs = t.startHour * 60 + t.startMinute
            minutesFromStart.postValue(Utils.getCurrentTs() - startTs)
        }
    }, 30000)

    // timer 2: toggle a boolean every 5 seconds (ramal - line)
    private val toggleClock = Clock({
        toggle.value?.let { v -> toggle.postValue(!v) }
    }, 5000)

    fun findLastTravel(db: MiDB) {
        val (y,m,d) = Calendar2.getDate()

        viewModelScope.launch(Dispatchers.IO) {
            val t = db.viajesDao().getCurrentTravel(y, m, d, Utils.getCurrentTs())

            // Aceptamos buses y trenes
            if (t == null) resetEverything()
            else {
                t.linea?.let {
                    val avgDuration = db.viajesDao().getAverageTravelDuration(it, t.nombrePdaInicio, t.nombrePdaFin)
                    if (avgDuration > 0) averageDuration.postValue(avgDuration)
                    else averageDuration.postValue(null)
                }

                travel.postValue(t)
                delay(500)

                // start clocks
                minuteClock.start()
                toggleClock.start()

                // delay(500)

                // force get location
                // locationVM.location.value?.let { recalculateDistances(db, it, cancelRunnable) }
            }
        }
    }

    // CALCULA EL PORCENTAJE A PARTIR DE LA DISTANCIA AL INICIO y AL FIN
    private fun calculateProgress(startDist: Double, endDist: Double) : Double {
        val total = startDist + endDist
        return startDist / total
    }

    private fun calculateETA(speed: Double, prog: Double, endDist: Double) {
        // distance is already in km
        val currentDiff = (endDist / speed) * 60
        var averageDiff = currentDiff.toInt()

        if (averageDuration.value != null && minutesFromStart.value != null) {
            val aux = averageDuration.value!! - minutesFromStart.value!!
            if (aux > 0) averageDiff = aux
        }

        val averageWeight = 1 - prog
        val correctedDiff = (currentDiff * prog + averageDiff * averageWeight)

        minutesToEnd.postValue(correctedDiff.toInt())
        endDistance.postValue(endDist)
        progress.postValue(prog)
        this.speed.postValue(speed)

    }

    fun recalculateDistances(db: MiDB, location: Location, newTravelRunnable: Runnable) {
        travel.value?.let {
            viewModelScope.launch(Dispatchers.IO) {
                val startStop = db.paradasDao().getByName(it.nombrePdaInicio)
                val endStop = db.paradasDao().getByName(it.nombrePdaFin)

                // calc distances internally
                startStop.updateDistance(location)
                endStop.updateDistance(location)

                // update values for UI
                startDistance.postValue(startStop.distance)
                val prog = calculateProgress(startStop.distance, endStop.distance)

                // calculate speed
                minutesFromStart.value?.let {
                    if (it > 0) {
                        val hours = it / 60.0
                        val speed = 0.5 * (startStop.distance / hours) + 12.5
                        calculateETA(speed, prog.pow(2), endStop.distance)
                    } else {
                        // should create a new travel
                        this.launch(Dispatchers.Main) { newTravelRunnable.run() }
                    }
                }

                // secondary action: search travel from next destination
                if (nextTravel.value == null) db.viajesDao().getCompletedTravelFrom(
                        endStop.nombre, startStop.nombre, it.linea)?.let { nextT ->
                    nextTravel.postValue(nextT)
                }
            }
        }
    }

    fun finishTravel(cal: Calendar, db: MiDB) {
        travel.value?.let {
            // Sumar tiempo que faltaba para terminar 
            minutesToEnd.value?.let { minutes ->
              cal.add(Calendar.MINUTE, minutes)
            }

            it.endHour = cal.get(Calendar.HOUR_OF_DAY)
            it.endMinute = cal.get(Calendar.MINUTE)
            viewModelScope.launch(Dispatchers.IO) { db.viajesDao().update(it) }
            resetEverything()
        }
    }

    fun resetEverything(){
        travel.postValue(null)
        startDistance.postValue(null)
        endDistance.postValue(null)
        minutesFromStart.postValue(null)
        minutesToEnd.postValue(null)
        averageDuration.postValue(null)
        progress.postValue(null)
        speed.postValue(null)
        nextTravel.postValue(null)
    }

    fun getCurrentETA(): Calendar? {
        minutesToEnd.value?.let {
            return Calendar2.getETA(it)
        }

        return null
    }
}
