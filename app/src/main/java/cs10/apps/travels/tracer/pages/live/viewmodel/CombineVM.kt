package cs10.apps.travels.tracer.pages.live.viewmodel

import android.app.Application
import android.location.Location
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import cs10.apps.travels.tracer.common.enums.TransportType
import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.model.Point
import cs10.apps.travels.tracer.model.Viaje
import cs10.apps.travels.tracer.model.joins.ColoredTravel
import cs10.apps.travels.tracer.pages.live.model.CurrentCombination
import cs10.apps.travels.tracer.pages.live.model.EstimationData
import cs10.apps.travels.tracer.pages.live.model.Stage
import cs10.apps.travels.tracer.pages.live.model.StagedTravel
import kotlin.math.roundToInt

class CombineVM(application: Application) : AndroidViewModel(application) {
    val database: MiDB = MiDB.getInstance(getApplication<Application>().applicationContext)
    var combination: CurrentCombination? = null
    var stagedTravel: StagedTravel? = null

    // read from UI
    val stages = MutableLiveData<List<Stage>>()
    val distanceToCombinationStop = MutableLiveData<Double>()

    suspend fun evaluatePossibleCombination(t: ColoredTravel, estData: EstimationData): CurrentCombination? {
        if (t.tipo != TransportType.BUS.ordinal) return null

        t.linea?.let { line ->
            val eta = t.startTime + estData.totalMinutes
            val expectedEndHour = (eta / 60.0).toInt()
            val historicalNextTravels = database.travelsDao().getBusCombinations(line, t.nombrePdaFin, expectedEndHour)

            // Be careful, the previous result can be empty
            if (historicalNextTravels.isEmpty()) return null

            val bestLine = historicalNextTravels.groupBy { it.linea }.toList().maxBy { it.second.size }.first
            Log.i("FATAL BEST LINE", bestLine.toString())
            val expectedNextTravel = historicalNextTravels.first { it.linea == bestLine }
            val result = CurrentCombination(t, expectedNextTravel, eta)
            this.combination = result
            return result
        }

        return null
    }

    suspend fun buildStagesForCombination(combination: CurrentCombination, stagedTravel: StagedTravel) {
        val finalDestination = database.paradasDao().getByName(combination.referenceCombination.nombrePdaFin)
        val finalStage = Stage(stagedTravel.end, finalDestination)

        val resultStages = mutableListOf<Stage>()
        stagedTravel.stages.forEach { resultStages.add(it) }
        resultStages.add(finalStage)

        resultStages.first().startTime = combination.firstTravel.startTime
        stages.postValue(resultStages)

        this.stagedTravel = StagedTravel(resultStages)
    }

    suspend fun createUpcomingTravel(combination: CurrentCombination) {
        val line1 = combination.firstTravel.linea
        val line2 = combination.referenceCombination.linea
        val stop = combination.referenceCombination.nombrePdaInicio

        if (line1 == null || line2 == null) return

        // check if it's already created
        val exists = combination.actualSecondTravel != null || database.travelsDao().getBusStartedTravels(
            combination.firstTravel.day,
            combination.firstTravel.month,
            combination.firstTravel.year,
            line2
        ).isNotEmpty()

        if (exists) return

        val expectedWaitingInStop = database.travelsDao().getBusCombinationWaiting(line1, line2, stop).roundToInt()
        val expectedNextStartTime = combination.expectedFirstTravelFinishTime + expectedWaitingInStop

        val upcomingTravel = Viaje().also {
            it.tipo = TransportType.BUS.ordinal

            with(combination.firstTravel) {
                it.day = this.day
                it.month = this.month
                it.year = this.year
            }

            with(combination.referenceCombination) {
                it.linea = this.linea
                it.costo = this.costo
                it.nombrePdaInicio = this.nombrePdaInicio
                it.nombrePdaFin = this.nombrePdaFin
                it.ramal = this.ramal
                it.weekDay = this.weekDay
            }

            it.startHour = expectedNextStartTime / 60
            it.startMinute = expectedNextStartTime % 60
        }

        database.viajesDao().insert(upcomingTravel)
        combination.actualSecondTravel = upcomingTravel
    }

    fun updateWithLocation(location: Location) {
        combination?.let {
            val currentPoint = Point(location.latitude, location.longitude)
            val listOfStages = stages.value ?: return

            // 0: stages progress
            stagedTravel?.let { st ->
                st.calculateCurrentStage(currentPoint)
                stages.postValue(st.stages)
            }

            // 1.1: distance to combination
            val combinationStop = listOfStages.last().start
            val distanceToCombinationStop = combinationStop.kmDistanceTo(currentPoint)
            this.distanceToCombinationStop.postValue(distanceToCombinationStop)
        }
    }
}
