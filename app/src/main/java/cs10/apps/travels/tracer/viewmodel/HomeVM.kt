package cs10.apps.travels.tracer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.domain.GetMaxTravelDistanceUseCase
import cs10.apps.travels.tracer.model.Parada

class HomeVM(application: Application) : AndroidViewModel(application) {
    private var getMaxTravelDistanceUseCase: GetMaxTravelDistanceUseCase

    init {
        val database = MiDB.getInstance(getApplication<Application>().applicationContext)
        getMaxTravelDistanceUseCase = GetMaxTravelDistanceUseCase(database.viajesDao())
    }

    val maxDistance = MutableLiveData<Double>()
    val favoriteStops = MutableLiveData<List<Parada>>()

    fun getStop(pos: Int) : Parada {
        if (favoriteStops.value == null) return Parada()
        return favoriteStops.value!![pos]
    }

    fun updateMaxDistance() {
        getMaxTravelDistanceUseCase()?.let { maxDistance.postValue(it) }
    }
}