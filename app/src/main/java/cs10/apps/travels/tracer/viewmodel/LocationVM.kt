package cs10.apps.travels.tracer.viewmodel

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cs10.apps.common.android.TimedLocation

class LocationVM : ViewModel() {

    private val locationLiveData = MutableLiveData<TimedLocation>()

    fun updateCurrentLocation(location: Location){
        locationLiveData.postValue(TimedLocation.createCurrentObject(location))
    }

    fun getLiveData() : MutableLiveData<TimedLocation> = locationLiveData
}