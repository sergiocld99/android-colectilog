package cs10.apps.travels.tracer.ui.live

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.model.Viaje
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LiveVM : ViewModel() {

    val travel = MutableLiveData<Viaje>()

    fun findLastTravel(db: MiDB){
        viewModelScope.launch(Dispatchers.IO){
            val t = db.viajesDao().lastTravel
            if (t.tipo == 0) travel.postValue(t)
        }
    }
}