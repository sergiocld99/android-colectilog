package cs10.apps.travels.tracer.viewmodel.stats

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.enums.TransportType
import cs10.apps.travels.tracer.model.joins.PriceSum
import cs10.apps.travels.tracer.viewmodel.RootVM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.max

class StatsVM(application: Application) : AndroidViewModel(application) {
    private val database: MiDB
    private val prefs : SharedPreferences

    init {
        val context = getApplication<Application>().applicationContext
        database = MiDB.getInstance(context)
        prefs = context.getSharedPreferences("balance", Context.MODE_PRIVATE)
    }

    val balance = MutableLiveData<Double>()
    val busStat = MutableLiveData<Stat>()
    val trainStat = MutableLiveData<Stat>()
    val coffeeStat = MutableLiveData<Stat>()

    val bus1Stat = MutableLiveData<LineStat>()
    val bus2Stat = MutableLiveData<LineStat>()
    val bus3Stat = MutableLiveData<LineStat>()

    fun fillData(rootVM: RootVM){
        val calendar = Calendar.getInstance()
        val month = calendar[Calendar.MONTH] + 1
        val year = calendar[Calendar.YEAR]

        viewModelScope.launch(Dispatchers.IO) {
            val coffee = async { database.coffeeDao().getTotalSpent(month, year) ?: 0.0 }
            val buses = async { database.travelsDao().getTotalSpentInMonthInType(month, year, TransportType.BUS.ordinal) ?: 0.0 }
            val trains = async { database.travelsDao().getTotalSpentInMonthInType(month, year, TransportType.TRAIN.ordinal) ?: 0.0 }
            val sumQuery = async {database.travelsDao().getMostSpentBusLineInMonth(month, year)}

            val genStats = async {
                val (c,b,t) = awaitAll(coffee, buses, trains)
                val total = max(c+b+t, 1.0)
                setTypeStats(b,t,c,total)
            }

            val busesStats = async {
                val b = buses.await()
                val sums = sumQuery.await()

                if (sums.isNotEmpty()) setBus1Stat(sums[0], b)
                if (sums.size > 1) setBus2Stat(sums[1], b)
                if (sums.size > 2) setBus3Stat(sums[2], b)
            }

            val balanceStat = async {
                val travelId = prefs.getLong("travelId", 0)
                val coffeeId = prefs.getLong("coffeeId", 0)
                val chargeId = prefs.getLong("chargeId", 0)
                val savedBalance = prefs.getFloat("balance", 0f)

                val sinceBuses = database.travelsDao().getTotalSpentInTypeSince(travelId, TransportType.BUS.ordinal) ?: 0.0
                val sinceTrains = database.travelsDao().getTotalSpentInTypeSince(travelId, TransportType.TRAIN.ordinal) ?: 0.0
                val sinceCoffee = database.coffeeDao().getSpentSince(coffeeId) ?: 0.0
                val charges = database.recargaDao().getTotalChargedSince(chargeId) ?: 0.0

                val money = savedBalance - sinceBuses - sinceTrains - sinceCoffee + charges
                setBalance(money)
            }

            delay(300)
            awaitAll(genStats, busesStats, balanceStat)
            rootVM.disableLoading()
        }
    }

    private fun setBalance(value: Double){
        balance.postValue(value)
    }

    private fun setBus1Stat(priceSum: PriceSum, total: Double){
        bus1Stat.postValue(LineStat(priceSum.linea, priceSum.color, priceSum.suma, total))
    }

    private fun setBus2Stat(priceSum: PriceSum, total: Double){
        bus2Stat.postValue(LineStat(priceSum.linea, priceSum.color, priceSum.suma, total))
    }

    private fun setBus3Stat(priceSum: PriceSum, total: Double){
        bus3Stat.postValue(LineStat(priceSum.linea, priceSum.color, priceSum.suma, total))
    }

    private fun setTypeStats(buses: Double, trains: Double, coffee: Double, total: Double) {
        busStat.postValue(Stat(buses, total))
        trainStat.postValue(Stat(trains, total))
        coffeeStat.postValue(Stat(coffee, total))
    }
}