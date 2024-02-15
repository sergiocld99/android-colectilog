package cs10.apps.travels.tracer.modules.stops.viewmodel

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import cs10.apps.travels.tracer.Utils
import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.model.Parada
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StopsVM(application: Application) : AndroidViewModel(application) {
    private val db: MiDB = MiDB.getInstance(getApplication<Application>().applicationContext)
    private val data = MutableLiveData(mutableListOf<Parada>())

    fun findAll(location: Location){
        viewModelScope.launch(Dispatchers.IO) {
            val stops  = db.paradasDao().all
            Utils.orderByProximity(stops, location.latitude, location.longitude)

            // find zones
            stops.forEach {
                it.zone = db.zonesDao().findFirstZoneIn(it.latitud, it.longitud)
            }

            data.postValue(stops)
        }
    }

    fun updateDistances(location: Location){
        viewModelScope.launch(Dispatchers.IO) {
            data.value?.let { stops ->
                val auxiliarCopy = mutableListOf<Parada>()
                auxiliarCopy.addAll(stops)
                auxiliarCopy.forEach { it.updateDistance(location) }
                data.postValue(auxiliarCopy)
            }
        }
    }

    fun getLiveData() = data
}