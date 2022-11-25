package cs10.apps.travels.tracer.viewmodel

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cs10.apps.travels.tracer.model.Parada
import cs10.apps.travels.tracer.model.joins.ColoredTravel
import kotlin.math.max

class LocatedArrivalVM : ViewModel() {

    val stop: MutableLiveData<Parada> by lazy {
        MutableLiveData<Parada>().also { Parada() }
    }

    val arrivals = MutableLiveData<MutableList<ColoredTravel>>()

    /*
    val arrivals: MutableLiveData<MutableList<ColoredTravel>> by lazy {
        MutableLiveData<MutableList<ColoredTravel>>().also { emptyList<Viaje>() }
    }

     */

    val proximity = MutableLiveData<Double>()
    val goingTo = MutableLiveData(false)
    val summary = MutableLiveData<Pair<Int, Int>>()

    fun recalculate(locationVM: LocationVM, homeVM: HomeVM) {
        locationVM.getLiveData().value?.let { timedLocation ->
            homeVM.maxDistance.value?.let { maxDistance ->
                recalculate(timedLocation.location, maxDistance)
            }
        }
    }

    fun recalculate(location: Location, maxD: Double) {
        if (stop.value == null) return
        val parada = stop.value!!

        parada.deltaX = parada.latitud - location.latitude
        parada.deltaY = parada.longitud - location.longitude

        setProximity(1 - parada.distance / maxD)
    }

    fun setStop(parada: Parada){
        if (stop.value != parada) stop.postValue(parada)
    }

    fun setStop(parada: Parada, forceSet: Boolean){
        if (forceSet) stop.postValue(parada) else setStop(parada)
    }

    private fun setProximity(prox: Double){
        val prev = proximity.value
        proximity.postValue(max(prox, 0.0))

        if (prev != null){
            goingTo.postValue(prox > prev && prox > 0.9)
        }
    }

    fun setSummary(travelCount: Int, rank: Int) {
        summary.postValue(Pair(travelCount, rank))
    }
}