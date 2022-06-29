package cs10.apps.travels.tracer.viewmodel

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LocationVM : ViewModel() {

    val location = MutableLiveData<Location>()
}