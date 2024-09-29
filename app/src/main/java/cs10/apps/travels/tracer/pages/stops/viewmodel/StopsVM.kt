package cs10.apps.travels.tracer.pages.stops.viewmodel

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import cs10.apps.common.android.Compass
import cs10.apps.travels.tracer.common.enums.TransportType
import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.model.Parada
import cs10.apps.travels.tracer.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class StopsVM(application: Application) : AndroidViewModel(application) {
    private val db: MiDB = MiDB.getInstance(getApplication<Application>().applicationContext)
    private var data = mutableListOf<Parada>()
    private val filteredData = MutableLiveData(mutableListOf<Parada>())

    fun findAll(location: Location){
        viewModelScope.launch(Dispatchers.IO) {
            val stops = db.paradasDao().all
            Utils.orderByProximity(stops, location.latitude, location.longitude)

            // find zones
            stops.forEach {
                it.zone = db.zonesDao().findFirstZoneIn(it.latitud, it.longitud)
                it.angle = Compass.buildWithoutStart(location, it).getAngleToDestination().toFloat()
            }

            delay(400)
            data = stops
            filteredData.postValue(stops)
        }
    }

    fun filter(type: TransportType?){
        viewModelScope.launch(Dispatchers.IO) {
            data.let { list ->
                if (type == null) filteredData.postValue(list)
                else {
                    val aux = mutableListOf<Parada>()
                    val filtered = list.filter { it.tipo == type.ordinal }
                    aux.addAll(filtered)
                    delay(400)
                    filteredData.postValue(aux)
                }
            }
        }
    }

    fun updateDistances(location: Location){
        viewModelScope.launch(Dispatchers.IO) {
            data.forEach {
                it.updateDistance(location)
            }
        }
    }

    fun getLiveData() = filteredData
}