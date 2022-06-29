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

    val current = MutableLiveData<Int>()

    fun setData(id: Long, ramal: String){
        val aux = ServicioTren()
        aux.id = id
        aux.ramal = ramal

        service.postValue(aux)
    }

    fun setCurrentTime(hour: Int, minute: Int){
        if (schedules.value == null) return

        val list = schedules.value!!
        val max = hour * 60 + minute
        var aux = list.size-1

        if (list.isEmpty()) return
        while (aux > 0 && (list[aux].hour * 60 + list[aux].minute) > max) aux--
        
        if (current.value == null || current.value!! != aux) current.postValue(aux)
    }
}