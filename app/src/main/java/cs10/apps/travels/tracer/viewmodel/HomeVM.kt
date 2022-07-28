package cs10.apps.travels.tracer.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.model.Parada

class HomeVM : ViewModel() {

    val maxDistance = MutableLiveData<Double>()
    val favoriteStops = MutableLiveData<List<Parada>>()
    val isLoading = MutableLiveData(false)

    fun getStop(pos: Int) : Parada {
        if (favoriteStops.value == null) return Parada()
        return favoriteStops.value!![pos]
    }

    fun updateMaxDistance(db : MiDB) {
        val list = db.viajesDao().travelDistances
        if (!list.isNullOrEmpty()) maxDistance.postValue(list.last().distance)
    }
}