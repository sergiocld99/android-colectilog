package cs10.apps.travels.tracer.modules.live.viewmodel

import android.app.Application
import android.content.Context
import android.location.Location
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.LayoutInflater
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.patrykandpatrick.vico.core.entry.FloatEntry
import cs10.apps.common.android.Clock
import cs10.apps.common.android.Localizable
import cs10.apps.common.android.TimedLocation
import cs10.apps.rater.HappyRater
import cs10.apps.travels.tracer.Utils
import cs10.apps.travels.tracer.data.generator.Station
import cs10.apps.travels.tracer.db.DatabaseFinder
import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.domain.GetCurrentTravelUseCase
import cs10.apps.travels.tracer.model.NextZone
import cs10.apps.travels.tracer.model.Parada
import cs10.apps.travels.tracer.model.Point
import cs10.apps.travels.tracer.model.Viaje
import cs10.apps.travels.tracer.model.Zone
import cs10.apps.travels.tracer.model.joins.ColoredTravel
import cs10.apps.travels.tracer.model.roca.RamalSchedule
import cs10.apps.travels.tracer.modules.ZoneData
import cs10.apps.travels.tracer.modules.live.model.Countdown
import cs10.apps.travels.tracer.modules.live.model.EstimationData
import cs10.apps.travels.tracer.modules.live.model.PredictionBase
import cs10.apps.travels.tracer.modules.live.model.Stage
import cs10.apps.travels.tracer.modules.live.model.StagedTravel
import cs10.apps.travels.tracer.modules.live.utils.AutoTravelFinisher
import cs10.apps.travels.tracer.modules.live.utils.MediumStopsManager
import cs10.apps.travels.tracer.modules.live.utils.ProgressCorrector
import cs10.apps.travels.tracer.viewmodel.LocationVM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.roundToInt

class LiveVM(application: Application) : AndroidViewModel(application) {

    // services
    private val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vm = application.applicationContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vm.defaultVibrator
    } else {
        application.applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    // Debug constants
    private val areaSize = 0

    // general live data
    val travel = MutableLiveData<ColoredTravel?>()
    private val toggle = MutableLiveData(true)
    private val nextTravel = MutableLiveData<Viaje?>()
    private val nearArrivals = MutableLiveData<MutableList<RamalSchedule>>()
    val customZone = MutableLiveData<Zone?>()

    // distances in metres
    val endDistance = MutableLiveData<Double?>()

    // time in minutes
    val countdown = Countdown()
    val minutesFromStart = MutableLiveData<Double?>()
    val minutesToEnd = MutableLiveData<Double?>()
    val estData = MutableLiveData<EstimationData?>()
    private val minDuration = MutableLiveData<Int?>()

    // aditional info
    val progress = MutableLiveData<Double?>()
    val rate = MutableLiveData<Double?>()
    val nextZones = MutableLiveData<MutableList<NextZone>>()
    private val deviation = MutableLiveData(0.0)
    val progressEntries = MutableLiveData<MutableList<FloatEntry>>()
    val finishData = MutableLiveData(false)

    // timer 1: update minutes from start every 30 seconds
    private var minuteClock: Clock? = null

    // DECEMBER 2022 - MVVM Architecture
    private val database: MiDB = MiDB.getInstance(getApplication<Application>().applicationContext)
    private var getCurrentTravelUseCase: GetCurrentTravelUseCase = GetCurrentTravelUseCase(database)

    // utils
    private val corrector = ProgressCorrector()
    private val finisher = AutoTravelFinisher()

    // staged travel
    var stagedTravel: StagedTravel? = null
    val stages = MutableLiveData(listOf<Stage>())

    // medium stops
    var mediumStopsManager: MediumStopsManager? = null

    // --------------------------- FUNCTIONS ---------------------------------

    fun findLastTravel(locationVM: LocationVM, newTravelRunnable: Runnable) {
        viewModelScope.launch(Dispatchers.IO) {
            val t: ColoredTravel? = getCurrentTravelUseCase()

            if (t == null) resetAllButTravel()
            else {
                // general estimation for buses or trains
                val estimation = DatabaseFinder(database).findAverageDuration(t)
                val st = StagedTravel.from(t, database)
                val pred = PredictionBase(estimation, st)

                estData.postValue(EstimationData(pred.getAverageDuration(), pred.getEstimatedSpeed(), true))
                minDuration.postValue(DatabaseFinder(database).findMinimalDuration(t))
                travel.postValue(t)

                stagedTravel = st
                mediumStopsManager = MediumStopsManager(t)
                mediumStopsManager?.buildStops(db = database)

                delay(500)

                // stop clock
                minuteClock?.apply { stop() }

                // start minutes ago clock
                minuteClock = Clock({
                    travel.value?.let { t ->
                        val startTs = t.startHour * 60 + t.startMinute
                        val currentTs = Utils.getRealCurrentTs()
                        minutesFromStart.postValue(currentTs - startTs)
                    }
                }, 30000)

                // start clock
                minuteClock?.apply { start() }

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

    // CALCULA EL PORCENTAJE A PARTIR DE LA DISTANCIA AL INICIO y AL FIN
    /**
     * @param startDist distance from start measured in a custom unit
     * @param endDist distance to end measured in same unit than startDist
     * @return progress between 0 and 1
     */
    private fun calculateProgress(startDist: Double, endDist: Double): Double {
        return startDist / (startDist + endDist)
    }

    /**
     * @param endDist in kilometers
     * @param speed in km/h
     * @param prog percentage between 0 and 1
     * @return estimated time in minutes (double) to arrive from now
     */
    private fun calculateMinutesLeft(speed: Double, prog: Double, endDist: Double): Double {
        return if (estData.value == null) (endDist / speed) * 60
        else (1 - prog) * estData.value!!.totalMinutes
    }

    fun recalculateDistances(location: Location, newTravelRunnable: Runnable) {
        travel.value?.let { t ->
            // do not continue if travel is already finished
            if (t.endHour != null) return

            viewModelScope.launch(Dispatchers.IO) {
                val currentPoint = Point(location.latitude, location.longitude)

                /*
                val startStop = database.paradasDao().getByName(t.nombrePdaInicio)
                val endStop = database.paradasDao().getByName(t.nombrePdaFin)

                // calc distances internally (in km)
                startStop.updateDistance(location)
                endStop.updateDistance(location)
                calculateDeviation(startStop, endStop)

                // update values for UI
                val prog = calculateProgress(startStop.distance, endStop.distance) */
                val st = stagedTravel ?: return@launch
                st.calculateCurrentStage(currentPoint)
                //stages.postValue(st.stages)

                val startStop = st.start
                val endStop = st.end
                val prog = st.currentProgress(currentPoint) / 100
                val endKmDistance = st.currentKmDistanceToFinish(currentPoint)

                // estimate time to arrive
                minutesFromStart.value?.let {
                    if (it > 0) {
                        val hours = it / 60.0
                        val speed = 0.5 * (startStop.kmDistanceTo(currentPoint) / hours) + 12.5

                        // calc direction and apply correction to progress
                        val correctedProg = corrector.correct(startStop, endStop, prog, t.ramal)
                        val minutesLeft = calculateMinutesLeft(speed, correctedProg, endKmDistance)

                        // evaluate if travel should be finished
                        val shouldFinish = finisher.evaluate(startStop, endStop, endKmDistance)

                        // postear para ui
                        if (countdown.start((minutesLeft * 60).roundToInt())) shortVibration()

                        minutesToEnd.postValue(minutesLeft)
                        endDistance.postValue(endKmDistance)
                        progress.postValue(correctedProg)
                        finishData.postValue(shouldFinish)

                        // update ETA stages
                        val progsToCalculate = st.globalProgressAtStagesEnd()
                        val etas = mutableListOf<Double>()

                        for (p in progsToCalculate) {
                            val cp = corrector.correct(startStop, endStop, 0.01 * p, t.ramal)
                            val distFromCurrent = (0.01 * p).times(st.totalDist)
                            val eta = minutesLeft - calculateMinutesLeft(speed, cp, distFromCurrent)
                            etas.add(eta)
                        }

                        st.updateStagesETA(etas)
                        stages.postValue(st.stages)

                        // fourth action: find next zones to get in
                        calculateNextPoints(startStop, endStop, location, minutesLeft, speed)

                        // fifth action: calculate expected rating
                        val bestDuration = minDuration.value
                        if (bestDuration == null || bestDuration <= 0) rate.postValue(3.0)
                        else {
                            val currentDuration = it + minutesLeft
                            if (currentDuration <= bestDuration) rate.postValue(5.0)
                            else rate.postValue(5.0 * bestDuration / currentDuration)
                        }

                        // sixth action: build progress chart
                        val entries = mutableListOf<FloatEntry>()

                        for (p in 0..10){
                            val x = p * 0.1
                            if (x + 0.1 < prog) continue

                            val x2 = x + (prog % 0.1) * (1.0 - x)
                            val y = corrector.correct(startStop, endStop, x2, t.ramal)
                            entries.add(FloatEntry(x.toFloat(), y.toFloat()))
                        }

                        progressEntries.postValue(entries)

                        // guardar para analisis posterior
                        // saveDebugData(t, it, prog, location, startStop, endStop)
                    } else {
                        // should create a new travel
                        this.launch(Dispatchers.Main) { newTravelRunnable.run() }
                    }
                }
            }
        }

        // third action: search near arrivals
        viewModelScope.launch(Dispatchers.IO) {
            val zone = ZoneData.getCodes(location)
            findNearArrivals(zone.first, zone.second)

            // new database
            val found = database.zonesDao().findFirstZoneIn(location.latitude, location.longitude)
            customZone.postValue(found)
        }
    }

    private fun shortVibration() {
        vibrate(360)
    }

    fun vibrate(ms: Long) {
        if (vibrator.hasVibrator() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(effect)
        }
    }

    private fun calculateDeviation(startStop: Parada, endStop: Parada) {
        val startPlusEndDistance = startStop.distance + endStop.distance
        val perfectDistance = endStop.kmDistanceTo(startStop)
        val error = abs(startPlusEndDistance - perfectDistance)

        Thread {
            Thread.sleep(6000)
            deviation.postValue(error * 100 / perfectDistance)
        }.start()
    }

    private suspend fun calculateNextPoints(
        startStop: Localizable,
        endStop: Localizable,
        currentLocation: Location,
        minutesCurrentToEnd: Double,
        speed: Double
    ) {
        val start = Point(startStop.getX(), startStop.getY())
        val end = Point(endStop.getX(), endStop.getY())
        val current = Point(currentLocation.latitude, currentLocation.longitude)
        val n = 3 // ((1 - currentProg) * 10).roundToInt()
        val nextPoints = calculateNextPoints(current, end, n)
        // var nextFound = false

        // avoid repeated zones
        val zoneIds = mutableSetOf<Long>()
        val list = mutableListOf<NextZone>()

        for (p in nextPoints) {
            database.zonesDao().findFirstZoneIn(p.getX(), p.getY())?.let { z ->
                if (zoneIds.contains(z.id)) return@let

                // calculate variables from this zone
                val distanceFromStart = z.getCoordsDistanceTo(start)
                val distanceToEnd = z.getCoordsDistanceTo(end)
                val prog = calculateProgress(distanceFromStart, distanceToEnd)
                val correctedProg = corrector.correct(start, end, prog, null)

                //val distance = z.getCoordsDistanceTo(location)
                //val kmDistance = NumberUtils.coordsDistanceToKm(distance)
                val minutesPointToEnd = calculateMinutesLeft(speed, correctedProg, distanceToEnd)
                val minutesCurrentToPoint = (minutesCurrentToEnd - minutesPointToEnd).toInt()

                if (minutesCurrentToPoint > 0){
                    list.add(NextZone(z, minutesCurrentToPoint))
                }

                zoneIds.add(z.id)
            }
        }

        nextZones.postValue(list)
        // if (!nextFound) nextZone.postValue(null)
    }

    private fun calculateNextPoints(start: Point, end: Point, n: Int): MutableList<Point> {
        val result = mutableListOf<Point>()
        val absX = end.getX() - start.getX()
        val absY = end.getY() - start.getY()

        for (i in 0 until n){
            val x = start.getX() + i * absX / n
            val y = start.getY() + i * absY / n
            result.add(Point(x,y))
        }

        return result
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
            minutesToEnd.value?.let { min -> cal.add(Calendar.SECOND, (min * 60).toInt()) }

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

    private fun resetAllButTravel() {
        endDistance.postValue(null)
        minutesToEnd.postValue(null)
        estData.postValue(null)
        progress.postValue(null)
        nextTravel.postValue(null)
        rate.postValue(null)
        deviation.postValue(0.0)
        finishData.postValue(false)

        countdown.stop()
    }

    fun eraseAll() {
        travel.postValue(null)
        minutesFromStart.postValue(null)
        resetAllButTravel()
    }

    private fun saveRating(rate: Int) {
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
