package cs10.apps.travels.tracer.viewmodel.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.model.PriceSum
import cs10.apps.travels.tracer.ui.stats.MonthSummaryFragment
import cs10.apps.travels.tracer.viewmodel.RootVM
import kotlinx.coroutines.*
import java.util.*
import kotlin.math.max

class StatsVM(application: Application) : AndroidViewModel(application) {
    private val database: MiDB = MiDB.getInstance(getApplication<Application>().applicationContext)

    val currency = MutableLiveData<Double>()
    val busStat = MutableLiveData<Stat>()
    val trainStat = MutableLiveData<Stat>()
    val coffeeStat = MutableLiveData<Stat>()

    val bus1Stat = MutableLiveData<LineStat>()
    val bus2Stat = MutableLiveData<LineStat>()
    val bus3Stat = MutableLiveData<LineStat>()

    fun fillData(rootVM: RootVM){
        val calendar = Calendar.getInstance()
        val month = calendar[Calendar.MONTH] + 1

        viewModelScope.launch(Dispatchers.IO) {
            val coffee = async { database.coffeeDao().getTotalSpent(month) }
            val buses = async { database.viajesDao().getTotalSpentInBuses(month) }
            val trains = async { database.viajesDao().getTotalSpentInTrains(month) }

            val genStats = async {
                val (c,b,t) = awaitAll(coffee, buses, trains)
                val total = max(c+b+t, 1.0)
                setTypeStats(b,t,c,total)
            }

            val busesStats = async {
                val sums = database.viajesDao().getMostExpensiveBus(month)
                val b = buses.await()

                if (sums.size > 0) setBus1Stat(sums[0], b)
                if (sums.size > 1) setBus2Stat(sums[1], b)
                if (sums.size > 2) setBus3Stat(sums[2], b)
            }

            val currencyStat = async {
                val sinceBuses = database.viajesDao().getSpentInBusesSince(MonthSummaryFragment.VIAJE_PARA_SALDO.toLong())
                val sinceTrains = database.viajesDao().getSpentInTrainsSince(MonthSummaryFragment.VIAJE_PARA_SALDO.toLong())
                val charges = database.recargaDao().getTotalChargedSince(0)

                val money = MonthSummaryFragment.SALDO_TEST - sinceBuses - sinceTrains - coffee.await() + charges
                setCurrency(money)
            }

            delay(1000)
            awaitAll(genStats, busesStats, currencyStat)
            rootVM.disableLoading()
        }
    }

    private fun setCurrency(value: Double){
        currency.postValue(value)
    }

    private fun setBus1Stat(priceSum: PriceSum, total: Double){
        bus1Stat.postValue(LineStat(priceSum.linea, priceSum.suma, total))
    }

    private fun setBus2Stat(priceSum: PriceSum, total: Double){
        bus2Stat.postValue(LineStat(priceSum.linea, priceSum.suma, total))
    }

    private fun setBus3Stat(priceSum: PriceSum, total: Double){
        bus3Stat.postValue(LineStat(priceSum.linea, priceSum.suma, total))
    }

    private fun setTypeStats(buses: Double, trains: Double, coffee: Double, total: Double) {
        busStat.postValue(Stat(buses, total))
        trainStat.postValue(Stat(trains, total))
        coffeeStat.postValue(Stat(coffee, total))
    }
}