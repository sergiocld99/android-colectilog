package cs10.apps.travels.tracer.pages.home.viewmodel

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.pages.stops.db.ParadasDao
import cs10.apps.travels.tracer.model.Parada
import cs10.apps.travels.tracer.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeVM(application: Application) : AndroidViewModel(application) {
    private var paradasDao: ParadasDao

    init {
        val database = MiDB.getInstance(getApplication<Application>().applicationContext)
        paradasDao = database.paradasDao()
    }

    val favoriteStops = MutableLiveData<List<Parada>>()
    val maxDistance = MutableLiveData<Double>()

    fun getStop(pos: Int) : Parada {
        if (favoriteStops.value == null) return Parada()
        return favoriteStops.value!![pos]
    }

    fun buildFavList(location: Location) {
        val calendar = Calendar.getInstance()
        val currentWeekDay = calendar.get(Calendar.DAY_OF_WEEK)
        val currentYear = calendar.get(Calendar.YEAR)
        val startMonth = calendar.get(Calendar.MONTH) - 4

        viewModelScope.launch(Dispatchers.IO) {
            var favs = paradasDao.getRecentFavouriteStops(currentWeekDay, currentYear, startMonth)
            if (favs.isEmpty()) favs = paradasDao.generalFavouriteStops

            Utils.orderByProximity(favs, location.latitude, location.longitude)
            favoriteStops.postValue(favs)
        }
    }
}