package cs10.apps.travels.tracer.viewmodel

import android.location.Location
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import cs10.apps.common.android.Speedometer
import cs10.apps.common.android.TimedLocation

class LocationVM : ViewModel() {

    // LiveData to observe
    private val locationLiveData = MutableLiveData<TimedLocation>()
    private val currentSpeedInKmH = MutableLiveData<Double>()

    // Utilities
    private val speedometer = Speedometer()


    fun updateCurrentLocation(location: Location){
        val timedLocation = TimedLocation.createCurrentObject(location)
        val speedInKmH = speedometer.update(timedLocation)

        // post values
        locationLiveData.postValue(timedLocation)
        speedInKmH?.let { currentSpeedInKmH.postValue(it) }
    }

    fun getLiveData() : MutableLiveData<TimedLocation> = locationLiveData

    fun setSpeedObserver(lifecycleOwner: LifecycleOwner, actionRunnable: Observer<Double>) {
        currentSpeedInKmH.observe(lifecycleOwner, actionRunnable)
    }
}