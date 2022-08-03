package cs10.apps.travels.tracer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class RootVM(application: Application) : AndroidViewModel(application) {
    val loading = MutableLiveData(false)

    fun enableLoading(){
        loading.postValue(true)
    }

    fun disableLoading(){
        loading.postValue(false)
    }
}