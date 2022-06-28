package cs10.apps.travels.tracer.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cs10.apps.travels.tracer.model.roca.HorarioTren
import cs10.apps.travels.tracer.model.roca.ServicioTren

class ServiceVM : ViewModel() {

    val service: MutableLiveData<ServicioTren> by lazy {
        MutableLiveData<ServicioTren>().also { ServicioTren() }
    }

    val schedules: MutableLiveData<List<HorarioTren>> by lazy {
        MutableLiveData<List<HorarioTren>>().also { listOf<HorarioTren>() }
    }

    fun setData(id: Long, ramal: String, station: String){
        val aux = ServicioTren()
        aux.id = id
        aux.ramal = ramal
        aux.cabecera = station

        service.postValue(aux)
    }
}