package cs10.apps.travels.tracer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import cs10.apps.travels.tracer.db.MiDB

class RootVM(application: Application) : AndroidViewModel(application) {
    val database: MiDB
    val loading = MutableLiveData(false)

    init {
        val context = getApplication<Application>().applicationContext
        database = MiDB.getInstance(context)
    }

    fun enableLoading(){
        loading.postValue(true)
    }

    fun disableLoading(){
        loading.postValue(false)
    }
}