package cs10.apps.travels.tracer.pages.month_summary.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import cs10.apps.travels.tracer.db.MiDB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BalanceVM(application: Application) : AndroidViewModel(application) {
    private val database: MiDB
    private val prefs : SharedPreferences

    init {
        val context = getApplication<Application>().applicationContext

        database = MiDB.getInstance(context)
        prefs = context.getSharedPreferences("balance", Context.MODE_PRIVATE)
    }

    val savedBalance = MutableLiveData(prefs.getFloat("balance", 0f))

    fun save(balance: Float, runnable: Runnable) {
        viewModelScope.launch(Dispatchers.IO) {
            val travelId = database.viajesDao().lastTravelId
            val coffeeId = database.coffeeDao().lastId
            val chargeId = database.recargaDao().lastId

            prefs.edit().putFloat("balance", balance).putLong("travelId", travelId)
                .putLong("coffeeId", coffeeId).putLong("chargeId", chargeId).apply()

            launch(Dispatchers.Main) { runnable.run() }
        }
    }
}