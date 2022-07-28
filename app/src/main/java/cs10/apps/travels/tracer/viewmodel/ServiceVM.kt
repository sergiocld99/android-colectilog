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
        MutableLiveData<List<HorarioTren>>().also { emptyList<HorarioTren>() }
    }

    val current = MutableLiveData<Int>()
    val isEnded = MutableLiveData(false)
    val next = MutableLiveData<ServicioTren>()

    fun setData(id: Long, ramal: String){
        val aux = ServicioTren()
        aux.id = id
        aux.ramal = ramal

        service.postValue(aux)
    }

    private fun alreadyEnded(): Boolean{
        return isEnded.value ?: false
    }

    fun setCurrentTime(hour: Int, minute: Int){
        val list = schedules.value ?: emptyList()

        if (!list.isNullOrEmpty()){
            val max = hour * 60 + minute
            var aux = list.size-1

            while (aux > 0 && (list[aux].hour * 60 + list[aux].minute) > max) aux--
            if (current.value ?: -1 != aux) current.postValue(aux)

            if (!alreadyEnded() && aux == list.size-1) isEnded.postValue(true)
        }

    }

    fun getFinalStation(): HorarioTren? {
        return if (!schedules.value.isNullOrEmpty()){
            schedules.value!!.last()
        } else null
    }
}