package cs10.apps.travels.tracer.ui.live

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cs10.apps.common.android.Clock
import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.model.Viaje
import cs10.apps.travels.tracer.viewmodel.LocationVM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.roundToInt

class LiveVM : ViewModel() {

    val travel = MutableLiveData<Viaje>()

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
        val calendar = Calendar.getInstance()

        travel.value?.let { t ->
            val startTs = t.startHour * 60 + t.startMinute
            val currentTs = calendar[Calendar.HOUR_OF_DAY] * 60 + calendar[Calendar.MINUTE]
            minutesFromStart.postValue(currentTs - startTs)
        }
    }, 30000)

    fun findLastTravel(db: MiDB, locationVM: LocationVM){
        viewModelScope.launch(Dispatchers.IO){
            val t = db.viajesDao().lastTravel

            if (t.tipo == 0){
                travel.postValue(t)
                delay(500)
                clock.start()

                delay(500)

                // force get location
                locationVM.location.value?.let { recalculateDistances(db, it) }
            }
        }
    }

    fun calculateETA(speed: Double){
        endDistance.value?.let {
            // distance is already in km
            val timeHours = it / speed
            minutesToEnd.postValue((timeHours * 60).toInt())
        }
    }

    fun recalculateDistances(db:MiDB, location: Location){
        travel.value?.let {
            viewModelScope.launch(Dispatchers.IO){
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
                    if (it > 0){
                        val hours = it / 60.0
                        val speed = (startStop.distance * 10 / hours).roundToInt() / 10.0

                        this@LiveVM.speed.postValue(speed)
                    }
                }
            }
        }
    }
}