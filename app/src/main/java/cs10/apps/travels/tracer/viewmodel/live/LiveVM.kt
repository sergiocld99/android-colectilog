package cs10.apps.travels.tracer.viewmodel.live

import android.app.Application
import android.content.Context
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import cs10.apps.common.android.Clock
import cs10.apps.common.android.NumberUtils
import cs10.apps.common.android.TimedLocation
import cs10.apps.rater.HappyRater
import cs10.apps.travels.tracer.Utils
import cs10.apps.travels.tracer.data.generator.Station
import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.domain.GetCurrentTravelUseCase
import cs10.apps.travels.tracer.model.Parada
import cs10.apps.travels.tracer.model.Viaje
import cs10.apps.travels.tracer.model.Zone
import cs10.apps.travels.tracer.model.joins.ColoredTravel
import cs10.apps.travels.tracer.model.roca.RamalSchedule
import cs10.apps.travels.tracer.modules.ZoneData
import cs10.apps.travels.tracer.viewmodel.LocationVM
import cs10.apps.travels.tracer.viewmodel.estimation.EstimationData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.util.*
import kotlin.math.pow
import kotlin.math.roundToInt

class LiveVM(application: Application) : AndroidViewModel(application) {

    // Debug constants
    private val areaSize = 0

    // general live data
    val travel = MutableLiveData<ColoredTravel?>()
    val toggle = MutableLiveData(true)
    val nextTravel = MutableLiveData<Viaje?>()
    val nearArrivals = MutableLiveData<MutableList<RamalSchedule>>()
    val customZone = MutableLiveData<Zone?>()

    // distances in metres
    val startDistance = MutableLiveData<Double?>()
    val endDistance = MutableLiveData<Double?>()

    // time in minutes
    val minutesFromStart = MutableLiveData<Double?>()
    val minutesToEnd = MutableLiveData<Int?>()
    val averageDuration = MutableLiveData<EstimationData?>()
    val minDuration = MutableLiveData<Int?>()

    // speed in km/h
    val speed = MutableLiveData<Double?>()

    // progress (between 0 and 1)
    val progress = MutableLiveData<Double?>()

    // expected rating
    val rate = MutableLiveData<Double?>()

    // timer 1: update minutes from start every 30 seconds
    private var minuteClock: Clock? = null

    // timer 2: toggle a boolean every 5 seconds (ramal - line)
    private var toggleClock: Clock? = null

    // DECEMBER 2022 - MVVM Architecture
    private val database: MiDB = MiDB.getInstance(getApplication<Application>().applicationContext)
    private var getCurrentTravelUseCase: GetCurrentTravelUseCase = GetCurrentTravelUseCase(database)


    fun findLastTravel(locationVM: LocationVM, newTravelRunnable: Runnable) {
        viewModelScope.launch(Dispatchers.IO) {
            val t: ColoredTravel? = getCurrentTravelUseCase()

            // Aceptamos buses y trenes
            if (t == null) resetAllButTravel()
            else {
                // general estimation for buses or trains
                calculateAvgDuration(t)
                calculateBestRate(t)

                travel.postValue(t)
                delay(500)

                // start clocks
                minuteClock?.apply { stop() }
                toggleClock?.apply { stop() }

                // start minutes ago clock
                minuteClock = Clock({
                    travel.value?.let { t ->
                        val startTs = t.startHour * 60 + t.startMinute
                        val currentTs = Utils.getRealCurrentTs()
                        minutesFromStart.postValue(currentTs - startTs)
                    }
                }, 30000)

                // start toggle clock
                toggleClock = Clock({
                    toggle.value?.let { v -> toggle.postValue(!v) }
                }, 7000)

                minuteClock?.apply { start() }
                toggleClock?.apply { start() }

                delay(500)

                // force get location
                locationVM.getLiveData().value?.let {
                    if (TimedLocation.isStillValid(it)) recalculateDistances(
                        it.location,
                        newTravelRunnable
                    )
                }
            }
        }
    }

    // realizado solo al encontrar viaje actual
    private fun calculateAvgDuration(t: Viaje) {
        // get distance of current travel
        val currentTravelDistance = database.viajesDao().getTravelDistanceFromId(t.id)

        // primero buscar para el mismo origen, destino y ramal
        var expectedDuration =
            if (t.ramal == null) database.viajesDao().getAverageTravelDuration(t.nombrePdaInicio, t.nombrePdaFin)
            else database.viajesDao().getAverageTravelDurationWithRamal(t.nombrePdaInicio, t.nombrePdaFin, t.ramal)

        if (expectedDuration > 0) {
            val speed = (currentTravelDistance.distance / NumberUtils.minutesToHours(expectedDuration))
            val estimationData = EstimationData(expectedDuration, speed.roundToInt(), true)
            averageDuration.postValue(estimationData)
        } else {
            // en trenes: segundo buscar cualquier viaje realizado antes (todos son del Roca)
            // en colectivos: segundo buscar para cualquier viaje de la misma linea
            val pastStats = if (t.linea == null) database.viajesDao().lastFinishedTrainTravel
            else database.viajesDao().getLastFinishedTravelFromLine(t.linea!!)

            val speed = pastStats?.calculateSpeedInKmH()

            if (speed != null) {
                expectedDuration = (currentTravelDistance.distance / speed).times(60).roundToInt()

                if (expectedDuration > 0) averageDuration.postValue(
                    EstimationData(expectedDuration, speed.roundToInt(), false)
                )
            }

            if (expectedDuration <= 0) averageDuration.postValue(null)
        }
    }

    private fun calculateBestRate(travel: Viaje){

        if (travel.linea == null){
            minDuration.postValue(database.viajesDao().getTrainMinTravelDuration(
                travel.nombrePdaInicio, travel.nombrePdaFin
            ))
        } else {
            minDuration.postValue(database.viajesDao().getMinTravelDuration(
                travel.linea!!, travel.nombrePdaInicio, travel.nombrePdaFin
            ))
        }
    }

    // CALCULA EL PORCENTAJE A PARTIR DE LA DISTANCIA AL INICIO y AL FIN
    private fun calculateProgress(startDist: Double, endDist: Double): Double {
        val total = startDist + endDist
        return startDist / total
    }

    private fun calculateMinutesLeft(speed: Double, prog: Double, endDist: Double): Double {
        return if (averageDuration.value == null) (endDist / speed) * 60
        else (1 - prog) * averageDuration.value!!.totalMinutes
    }

    fun recalculateDistances(location: Location, newTravelRunnable: Runnable) {
        travel.value?.let { t ->
            // do not continue if travel is already finished
            if (t.endHour != null) return

            viewModelScope.launch(Dispatchers.IO) {
                val startStop = database.paradasDao().getByName(t.nombrePdaInicio)
                val endStop = database.paradasDao().getByName(t.nombrePdaFin)

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

                        // calc direction and apply correction to progress
                        val correctedProg = when (Utils.getDirection(startStop, endStop)) {
                            Utils.Direction.SOUTH_EAST -> 2 * prog.pow(3) - 2.76 * prog.pow(2) + 1.76 * prog
                            Utils.Direction.NORTH_WEST -> 0.25 * prog.pow(3) - 1.05 * prog.pow(2) + 1.8 * prog
                            else -> prog
                        }

                        val minutesLeft = calculateMinutesLeft(speed, correctedProg, endStop.distance)

                        // postear para ui
                        minutesToEnd.postValue(minutesLeft.roundToInt())
                        endDistance.postValue(endStop.distance)
                        progress.postValue(correctedProg)
                        this@LiveVM.speed.postValue(speed)

                        // calculate expected rating
                        minDuration.value?.let { bestDuration ->
                            if (bestDuration > 0){
                                val currentDuration = it + minutesLeft

                                if (currentDuration <= bestDuration) rate.postValue(5.0)
                                else rate.postValue(5.0 * bestDuration / currentDuration)
                            } else rate.postValue(null)
                        }

                        // guardar para analisis posterior
                        saveDebugData(t, it, prog, location, startStop, endStop)
                    } else {
                        // should create a new travel
                        this.launch(Dispatchers.Main) { newTravelRunnable.run() }
                    }
                }

                // secondary action: search travel from next destination
                if (nextTravel.value == null) database.viajesDao().getCompletedTravelFrom(
                    endStop.nombre, startStop.nombre, t.linea
                )?.let { nextT ->
                    nextTravel.postValue(nextT)
                }
            }
        }

        // third action: search near arrivals
        viewModelScope.launch(Dispatchers.IO) {
            val zone = ZoneData.getCodes(location)
            findNearArrivals(zone.first, zone.second)

            // new database
            val zonesFromDB = database.zonesDao().findZonesIn(location.latitude, location.longitude)
            if (zonesFromDB.isNotEmpty()) customZone.postValue(zonesFromDB[0])
            else customZone.postValue(null)
        }
    }

    // guarda datos del live actual en un .log
    private fun saveDebugData(
        t: Viaje, minutesFromStart: Double, prog: Double,
        location: Location, startStop: Parada, endStop: Parada
    ) {

        val context = getApplication<Application>().applicationContext
        val file0 = File(context.filesDir, "${t.id}.log")
        if (!file0.exists()) file0.createNewFile()

        try {
            val file = FileOutputStream(file0, true)
            val os = OutputStreamWriter(file)
            os.write("$minutesFromStart; $prog; ${location.latitude}; ${location.longitude}; ${startStop.distance}; ${endStop.distance}")
            os.write("\n")
            os.close()
            file.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // busca trenes llegando en la zona
    private fun findNearArrivals(xCode: Int, yCode: Int) {
        val nearStations = Station.findStationsAtZone(xCode, yCode, 2, areaSize)
        val currentTime = Utils.getCurrentTs()
        val arrivals = mutableListOf<RamalSchedule>()

        nearStations.forEach { s ->
            // search 2 if there is only one station, search 1 if there's two stations
            val queryResult =
                database.servicioDao().getNextArrivals(s.nombre, currentTime, 2 / nearStations.size)
            queryResult.forEach { arrivals.add(it) }
        }

        arrivals.sort()
        nearArrivals.postValue(arrivals)
    }

    fun finishTravel(cal: Calendar, layoutInflater: LayoutInflater, context: Context) {
        travel.value?.let {
            // Sumar tiempo que faltaba para terminar 
            minutesToEnd.value?.let { minutes -> cal.add(Calendar.MINUTE, minutes) }

            it.endHour = cal.get(Calendar.HOUR_OF_DAY)
            it.endMinute = cal.get(Calendar.MINUTE)
            viewModelScope.launch(Dispatchers.IO) { database.viajesDao().update(it) }

            // load rater before reset variables
            viewModelScope.launch(Dispatchers.Main) { showRaterDelayed(layoutInflater, context) }

            resetAllButTravel()
        }
    }

    private fun showRaterDelayed(layoutInflater: LayoutInflater, context: Context){
        val rater = HappyRater()
        rater.doneCallback = { rate -> saveRating(rate) }
        rater.cancelCallback = { eraseAll() }
        rater.create(context, layoutInflater, rate.value?.toFloat() ?: 3f)

        Handler(Looper.getMainLooper()).postDelayed({rater.show()}, 2500)
    }

    fun resetAllButTravel() {
        startDistance.postValue(null)
        endDistance.postValue(null)
        minutesToEnd.postValue(null)
        averageDuration.postValue(null)
        progress.postValue(null)
        speed.postValue(null)
        nextTravel.postValue(null)
        rate.postValue(null)
    }

    fun eraseAll() {
        travel.postValue(null)
        minutesFromStart.postValue(null)
        resetAllButTravel()
    }

    fun saveRating(rate: Int) {
        travel.value?.let { t ->
            t.rate = rate
            viewModelScope.launch(Dispatchers.IO) { database.viajesDao().update(t) }
            showToastInMainThread("Viaje calificado con éxito")
            eraseAll()
        }
    }

    private fun showToastInMainThread(message: String) {
        viewModelScope.launch(Dispatchers.Main) {
            Toast.makeText(getApplication(), message, Toast.LENGTH_LONG).show()
        }
    }

}
