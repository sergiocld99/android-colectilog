package cs10.apps.travels.tracer.viewmodel

import android.app.Application
import androidx.lifecycle.*
import cs10.apps.travels.tracer.db.LinesDao
import cs10.apps.travels.tracer.model.lines.CustomBusLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LineManagerVM(application: Application) : AndroidViewModel(application) {

    // mis live data
    private var myLines = MutableLiveData<List<CustomBusLine>>()


    fun load(linesDao: LinesDao){
        viewModelScope.launch(Dispatchers.IO){
            val lines = linesDao.getAll()

            if (lines.isEmpty()){
                linesDao.getAllFromViajes().forEach { number ->
                    val obj = CustomBusLine(0, number, null, 0)
                    linesDao.insert(obj)
                    lines.add(obj)
                }
            }

            lines.sort()
            myLines.postValue(lines)
        }
    }

    fun observe(lifecycleOwner: LifecycleOwner, observer: Observer<List<CustomBusLine>>) {
        myLines.observe(lifecycleOwner, observer)
    }
}