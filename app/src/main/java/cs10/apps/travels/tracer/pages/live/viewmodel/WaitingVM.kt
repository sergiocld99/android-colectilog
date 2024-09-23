package cs10.apps.travels.tracer.pages.live.viewmodel

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.model.Parada
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WaitingVM(application: Application) : AndroidViewModel(application) {
    private val db: MiDB = MiDB.getInstance(getApplication<Application>().applicationContext)
    private val nearMargin = 1.0 / 1000

    // live models
    val stopHere = MutableLiveData<Parada?>()

    fun reset(){
        stopHere.postValue(null)
    }

    fun updateLocation(location: Location?){
        if (location == null) {
            stopHere.postValue(null)
            return
        }

        viewModelScope.launch(Dispatchers.IO){
            val x0 = location.latitude - nearMargin
            val x1 = location.latitude + nearMargin
            val y0 = location.longitude - nearMargin
            val y1 = location.longitude + nearMargin

            stopHere.postValue(db.paradasDao().findStopIn(x0, x1, y0, y1))
        }


    }
}