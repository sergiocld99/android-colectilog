package cs10.apps.travels.tracer.viewmodel

import android.app.Application
import androidx.lifecycle.*
import cs10.apps.travels.tracer.db.LinesDao
import cs10.apps.travels.tracer.model.joins.RatedBusLine
import cs10.apps.travels.tracer.model.lines.CustomBusLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LineManagerVM(application: Application) : AndroidViewModel(application) {

    // mis live data
    private var myLines = MutableLiveData<List<RatedBusLine>>()
    private lateinit var editingLine: CustomBusLine


    fun load(linesDao: LinesDao, rootVM: RootVM){
        rootVM.enableLoading()

        viewModelScope.launch(Dispatchers.IO){
            val numbers1 = linesDao.getCustomNumbers()

            linesDao.getAllFromViajes().forEach { number ->
                if (!numbers1.contains(number)) {
                    val obj = CustomBusLine(0, number, null, 0)
                    linesDao.insert(obj)
                    numbers1.add(number)
                }
            }

            // recargar con las lineas recién creadas desde viajes
            val lines = linesDao.getAllWithRates()

            // calculate speed for each line using last travel
            lines.forEach {
                if (it.number != null){
                    val stats = rootVM.database.viajesDao().getRecentFinishedTravelsFromLine(it.number)

                    if (stats.isEmpty()) it.speed = null
                    else {
                        var sum = 0.0
                        stats.forEach { stat -> sum += stat.calculateSpeedInKmH() }
                        it.speed = sum / stats.size
                    }
                }
            }

            lines.sort()
            myLines.postValue(lines)

            rootVM.disableLoading()
        }
    }

    fun observe(lifecycleOwner: LifecycleOwner, observer: Observer<List<RatedBusLine>>) {
        myLines.observe(lifecycleOwner, observer)
    }

    fun selectEditing(line: CustomBusLine) {
        editingLine = line
    }

    fun updateColor(linesDao: LinesDao, color: Int, rootVM: RootVM) {
        editingLine.color = color

        viewModelScope.launch(Dispatchers.IO) {
            linesDao.update(editingLine)

            // force reload
            load(linesDao, rootVM)
        }
    }
}