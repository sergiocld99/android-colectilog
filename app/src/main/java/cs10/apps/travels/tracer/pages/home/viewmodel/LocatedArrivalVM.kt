package cs10.apps.travels.tracer.pages.home.viewmodel

import android.app.Application
import android.content.Context
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import cs10.apps.travels.tracer.db.DynamicQuery
import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.model.Parada
import cs10.apps.travels.tracer.model.Zone
import cs10.apps.travels.tracer.model.joins.ColoredTravel
import cs10.apps.travels.tracer.model.roca.ArriboTren
import cs10.apps.travels.tracer.pages.registry.utils.AutoRater.Companion.calculateRate
import cs10.apps.travels.tracer.utils.Utils
import cs10.apps.travels.tracer.viewmodel.LocationVM
import cs10.apps.travels.tracer.viewmodel.RootVM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sube.cuandosubo.CuandoSubo
import sube.cuandosubo.StopFinder
import java.util.Calendar
import kotlin.math.max

class LocatedArrivalVM(application: Application) : AndroidViewModel(application) {

    val stop = MutableLiveData<Parada>()
    val arrivals = MutableLiveData<MutableList<ColoredTravel>>()
    private val proximity = MutableLiveData<Double>()
    val goingTo = MutableLiveData(false)
    val summary = MutableLiveData<Pair<Int, Int>>()
    val stopZone = MutableLiveData<Zone?>()

    fun recalculate(locationVM: LocationVM, homeVM: HomeVM) {
        locationVM.getLiveData().value?.let { timedLocation ->
            homeVM.maxDistance.value?.let { maxDistance ->
                recalculate(timedLocation.location, maxDistance)
            }
        }
    }

    fun recalculate(location: Location, maxD: Double) {
        if (stop.value == null) return
        val parada = stop.value!!

        parada.deltaX = parada.latitud - location.latitude
        parada.deltaY = parada.longitud - location.longitude

        setProximity(1 - parada.distance / maxD)
    }

    fun setStop(parada: Parada){
        if (stop.value != parada) stop.postValue(parada)
    }

    fun setStop(parada: Parada, forceSet: Boolean, rootVM: RootVM){
        if (forceSet) stop.postValue(parada) else setStop(parada)

        // find zone
        viewModelScope.launch(Dispatchers.IO) {
            val zone = rootVM.database.zonesDao().findFirstZoneIn(parada.latitud, parada.longitud)
            stopZone.postValue(zone)
        }
    }

    private fun setProximity(prox: Double){
        val prev = proximity.value
        proximity.postValue(max(prox, 0.0))

        if (prev != null){
            goingTo.postValue(prox > prev && prox > 0.9)
        }
    }

    fun fillData(parada: Parada, rootVM: RootVM, context: Context){
        viewModelScope.launch(Dispatchers.IO) {
            val calendar = Calendar.getInstance()
            val miDB = rootVM.database

            val hour = calendar[Calendar.HOUR_OF_DAY]
            val m = calendar[Calendar.MINUTE]
            val now = hour * 60 + m

            val stopName = parada.nombre
            val buildingArrivals = DynamicQuery.getNextBusArrivals(context, stopName)
            val trenes = DynamicQuery.getNextTrainArrivals(context, stopName)

            // PROCESAMIENTO DE BUSES
            calculateRate(buildingArrivals, miDB.viajesDao())

            // PROCESAMIENTO DE TRENES
            for (tren in trenes) {
                val v = ArriboTren()
                val target = tren.hour * 60 + tren.minute
                val end = miDB.servicioDao().getFinalStation(tren.service)

                v.tipo = 1
                v.ramal = tren.ramal
                v.startHour = tren.hour
                v.startMinute = tren.minute
                v.serviceId = tren.service
                v.nombrePdaFin = Utils.simplify(end.station)
                v.nombrePdaInicio = tren.cabecera
                v.recorrido = miDB.servicioDao().getRecorridoUntil(tren.service, now, target)
                v.recorridoDestino = miDB.servicioDao().getRecorridoFrom(tren.service, target)
                v.endHour = end.hour
                v.endMinute = end.minute
                v.restartAux()
                buildingArrivals.add(v)
            }

            // v2.29: web api
            try {
                val liveArrivals = askCuandoSubo(miDB, stopName)
                buildingArrivals.addAll(liveArrivals)
            } catch (e: Exception){
                e.printStackTrace()
            }

            buildingArrivals.sort()
            arrivals.postValue(buildingArrivals)
        }
    }

    private fun askCuandoSubo(miDB: MiDB, stopName: String) : List<ColoredTravel> {
        val res = mutableListOf<ColoredTravel>()

        StopFinder.getId(stopName)?.let { id ->
            CuandoSubo(id).getArrivals().forEach { la ->
                val num = la.line.toInt()
                val savedData = miDB.linesDao().getByNumber(num + 14)
                val item = ColoredTravel(savedData?.color)
                item.linea = num
                item.nombrePdaFin = la.destination
                item.startHour = la.arrivalTime[Calendar.HOUR_OF_DAY]
                item.startMinute = la.arrivalTime[Calendar.MINUTE]
                item.ramal = la.ramal
                res.add(item)
            }
        }

        return res
    }

    fun calculateSummary(rootVM: RootVM, stopName: String){
        viewModelScope.launch(Dispatchers.IO) {
            val travelCount = rootVM.database.paradasDao().getTravelCount(stopName)
            val rank = rootVM.database.paradasDao().getRank(travelCount)
            summary.postValue(Pair(travelCount, rank))
        }
    }
}