package cs10.apps.travels.tracer.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cs10.apps.travels.tracer.model.Viaje

class LocatedArrivalVM : ViewModel() {

    val stopName = MutableLiveData<String>()
    val proximity = MutableLiveData<Double>()

    val arrivals: MutableLiveData<MutableList<Viaje>> by lazy {
        MutableLiveData<MutableList<Viaje>>().also { listOf<Viaje>() }
    }
}