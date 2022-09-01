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

class LiveVM : ViewModel() {

    val travel = MutableLiveData<Viaje?>()

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

    // timer
    private val clock = Clock({
        travel.value?.let { t ->
            val startTs = t.startHour * 60 + t.startMinute
            minutesFromStart.postValue(Utils.getCurrentTs() - startTs)
        }
    }, 30000)

    fun findLastTravel(db: MiDB, locationVM: LocationVM, cancelRunnable: Runnable) {
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
                clock.start()

                delay(500)

                // force get location
                locationVM.location.value?.let { recalculateDistances(db, it, cancelRunnable) }
            }
        }
    }

    // CALCULA EL PORCENTAJE A PARTIR DE LA DISTANCIA AL INICIO y AL FIN
    private fun calculateProgress(){
        if (startDistance.value != null && endDistance.value != null){
            val total = startDistance.value!! + endDistance.value!!
            progress.postValue(startDistance.value!! / total)
        } else progress.postValue(null)
    }

    private fun calculateETA(speed: Double) {
        endDistance.value?.let { dist ->
            // distance is already in km
            val currentDiff = (dist / speed) * 60
            var averageDiff = currentDiff.toInt()
            var currentWeight = 1.0

            progress.value?.let { currentWeight = 1 - it }

            if (averageDuration.value != null && minutesFromStart.value != null){
                val aux = averageDuration.value!! - minutesFromStart.value!!
                if (aux > 0) averageDiff = aux
            }

            val averageWeight = 1 - currentWeight
            val correctedDiff = (currentDiff * currentWeight + averageDiff * averageWeight)

            minutesToEnd.postValue(correctedDiff.toInt())
        }
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
                endDistance.postValue(endStop.distance)
                calculateProgress()

                // calculate speed
                minutesFromStart.value?.let {
                    if (it > 0) {
                        val hours = it / 60.0
                        val speed = startStop.distance / hours
                        this@LiveVM.speed.postValue(speed)
                        calculateETA(speed)
                    } else {
                        // should create a new travel
                        this.launch(Dispatchers.Main) { newTravelRunnable.run() }
                    }
                }
            }
        }
    }

    fun finishTravel(cal: Calendar, db: MiDB) {
        travel.value?.let {
            it.endHour = cal.get(Calendar.HOUR_OF_DAY)
            it.endMinute = cal.get(Calendar.MINUTE)
            viewModelScope.launch(Dispatchers.IO) { db.viajesDao().update(it) }
            resetEverything()
        }
    }

    private fun resetEverything(){
        travel.postValue(null)
        startDistance.postValue(null)
        endDistance.postValue(null)
        minutesFromStart.postValue(null)
        minutesToEnd.postValue(null)
        averageDuration.postValue(null)
        progress.postValue(null)
        speed.postValue(null)
    }
}