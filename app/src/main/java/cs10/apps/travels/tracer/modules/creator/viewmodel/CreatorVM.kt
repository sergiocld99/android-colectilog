package cs10.apps.travels.tracer.modules.creator.viewmodel

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import cs10.apps.travels.tracer.Utils
import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.model.Parada
import cs10.apps.travels.tracer.model.Viaje
import cs10.apps.travels.tracer.modules.creator.model.LikelyTravel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class CreatorVM(application: Application) : AndroidViewModel(application) {
    private val db: MiDB = MiDB.getInstance(getApplication<Application>().applicationContext)

    val startParadas = MutableLiveData(listOf<Parada>())
    val endParadas = MutableLiveData(listOf<Parada>())
    val likelyTravel = MutableLiveData<LikelyTravel?>(null)

    fun loadInBackground(location: Location?){
        viewModelScope.launch(Dispatchers.IO){
            loadStops(location)
        }
    }

    private fun loadStops(location: Location?){
        val startOnes = db.paradasDao().all
        val endOnes = db.paradasDao().allOrderedByMostVisited

        // order start ones by proximity
        location?.let { Utils.orderByProximity(startOnes, it.latitude, it.longitude) }

        // notify activity
        startParadas.postValue(startOnes)
        endParadas.postValue(endOnes)

        // prediction
        if (startOnes.isEmpty()) {
            likelyTravel.postValue(null)
        } else {
            val v = predict(startOnes, endOnes)
            likelyTravel.postValue(v)
        }
    }

    private fun predict(startOnes: Iterable<Parada>, endOnes: Iterable<Parada>) : LikelyTravel? {
        val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        var startIndex = 0
        var v: Viaje? = null

        for (p in startOnes){
            v = db.viajesDao().getLikelyTravelFrom(p.nombre, h)
            if (v != null || startIndex == 4) break else startIndex++
        }

        return if (v == null) null
        else {
            var endIndex = 0

            for (p in endOnes) {
                if (p.nombre == v.nombrePdaFin) break else endIndex++
            }

            LikelyTravel(v, startIndex, endIndex)
        }
    }
}