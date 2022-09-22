package cs10.apps.travels.tracer.viewmodel

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import cs10.apps.common.android.Calendar2
import cs10.apps.common.android.Clock
import cs10.apps.travels.tracer.Utils
import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.model.Parada
import cs10.apps.travels.tracer.model.Viaje
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.util.*
import kotlin.math.max
import kotlin.math.pow

class LiveVM(application: Application) : AndroidViewModel(application) {

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
    private var minuteClock : Clock? = null

    // timer 2: toggle a boolean every 5 seconds (ramal - line)
    private var toggleClock : Clock? = null

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
                minuteClock?.apply { stop() }
                toggleClock?.apply { stop() }

                // start minutes ago clock
                minuteClock = Clock({
                    travel.value?.let { t ->
                        val startTs = t.startHour * 60 + t.startMinute
                        minutesFromStart.postValue(Utils.getCurrentTs() - startTs)
                    }
                }, 30000)

                // start toggle clock
                toggleClock = Clock({
                    toggle.value?.let { v -> toggle.postValue(!v) }
                }, 5000)

                minuteClock?.apply { start() }
                toggleClock?.apply { start() }

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
            averageDiff = max(aux, 0)
        }

        val averageWeight = 1 - prog
        val correctedDiff = (currentDiff * prog + averageDiff * averageWeight)

        minutesToEnd.postValue(correctedDiff.toInt())
        endDistance.postValue(endDist)
        progress.postValue(prog)
        this.speed.postValue(speed)
    }

    fun recalculateDistances(db: MiDB, location: Location, newTravelRunnable: Runnable) {
        travel.value?.let { t ->
            viewModelScope.launch(Dispatchers.IO) {
                val startStop = db.paradasDao().getByName(t.nombrePdaInicio)
                val endStop = db.paradasDao().getByName(t.nombrePdaFin)

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
                        //val correctedProg = 4 * prog.pow(3) - 6 * prog.pow(2) + 3 * prog
                        val correctedProg = 2 * prog.pow(3) - 2.76 * prog.pow(2) + 1.76 * prog
                        calculateETA(speed, correctedProg, endStop.distance)

                        // guardar para analisis posterior
                        saveDebugData(t, it, prog, location, startStop, endStop)
                    } else {
                        // should create a new travel
                        this.launch(Dispatchers.Main) { newTravelRunnable.run() }
                    }
                }

                // secondary action: search travel from next destination
                if (nextTravel.value == null) db.viajesDao().getCompletedTravelFrom(
                        endStop.nombre, startStop.nombre, t.linea)?.let { nextT ->
                    nextTravel.postValue(nextT)
                }

            }
        }
    }

    // guarda datos del live actual en un .log
    private fun saveDebugData(t: Viaje, minutesFromStart: Int, prog: Double,
                              location: Location, startStop: Parada, endStop: Parada) {

        val context = getApplication<Application>().applicationContext
        val file0 = File(context.filesDir, "${t.id}.log")
        if (!file0.exists()) file0.createNewFile()

        try {
            val file = FileOutputStream(file0, true)
            val os = OutputStreamWriter(file)
            os.write("$minutesFromStart, $prog, ${location.latitude}, ${location.longitude}, ${startStop.distance}, ${endStop.distance}")
            os.write("\n")
            os.close()
            file.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun calculateDirectionToDestination(){
        
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

}
