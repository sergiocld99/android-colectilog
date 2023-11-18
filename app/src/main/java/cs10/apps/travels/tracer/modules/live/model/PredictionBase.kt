package cs10.apps.travels.tracer.modules.live.model

import kotlin.math.roundToInt

class PredictionBase(private val estData: EstimationData?, private val st: StagedTravel) {

    /**
     * @return speed in km/h
     */
    fun getEstimatedSpeed() : Double {
        estData?.totalMinutes?.let {
            val km = st.totalKmDist
            val h = it / 60.0
            return km/h
        }

        return 10.0
    }

    /**
     * @return normal duration in minutes
     */
    fun getAverageDuration(): Int {
        estData?.let {
            return it.totalMinutes
        }

        val km = st.totalKmDist
        return (km / getEstimatedSpeed()).times(60).roundToInt()
    }
}