package cs10.apps.travels.tracer.modules.path

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import cs10.apps.travels.tracer.db.SafeStopsDao
import cs10.apps.travels.tracer.enums.TransportType
import cs10.apps.travels.tracer.modules.live.entity.MediumStop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PathsVM(application: Application) : AndroidViewModel(application) {
    val mediumStops = MutableLiveData(arrayListOf<MediumStop>())

    fun searchAllMediumStops(dao: SafeStopsDao) {
        viewModelScope.launch(Dispatchers.IO) {
            val type = TransportType.BUS.ordinal
            val queryResults = dao.getPathGroups(type)

            queryResults.forEach {
                Log.i("ColectiLog", it.toString())
            }
        }
    }

}