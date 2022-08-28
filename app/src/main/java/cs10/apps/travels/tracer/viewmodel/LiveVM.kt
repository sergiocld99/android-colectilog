package cs10.apps.travels.tracer.viewmodel

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val startDistance = MutableLiveData<Double>()
    private val endDistance = MutableLiveData<Double>()

    // time in minutes
    val minutesFromStart = MutableLiveData<Int>()
    val minutesToEnd = MutableLiveData<Int>()

    // speed in km/h
    val speed = MutableLiveData<Double>()

    // timer
    private val clock = Clock({
        travel.value?.let { t ->
            val startTs = t.startHour * 60 + t.startMinute
            minutesFromStart.postValue(Utils.getCurrentTs() - startTs)
        }
    }, 30000)

    fun findLastTravel(db: MiDB, locationVM: LocationVM, cancelRunnable: Runnable) {
        val calendar = Calendar.getInstance()

        viewModelScope.launch(Dispatchers.IO) {
            val t = db.viajesDao().getCurrentTravel(
                calendar[Calendar.YEAR],
                calendar[Calendar.MONTH],
                calendar[Calendar.DAY_OF_MONTH],
                Utils.getCurrentTs()
            )

            // Aceptamos buses y trenes
            if (t == null) travel.postValue(null)
            else {
                travel.postValue(t)
                delay(500)
                clock.start()

                delay(500)

                // force get location
                locationVM.location.value?.let { recalculateDistances(db, it, cancelRunnable) }
            }
        }
    }

    private fun calculateETA(speed: Double) {
        endDistance.value?.let {
            // distance is already in km
            val timeHours = it / speed
            minutesToEnd.postValue((timeHours * 60).toInt())
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

                // calculate speed
                minutesFromStart.value?.let {
                    if (it > 0) {
                        val hours = it / 60.0
                        val speed = startStop.distance / hours
                        val correctedSpeed = (speed + 25) / 2
                        this@LiveVM.speed.postValue(correctedSpeed)
                        calculateETA(correctedSpeed)
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
            travel.postValue(null)
        }
    }
}