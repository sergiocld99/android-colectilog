package cs10.apps.travels.tracer.modules.lines.viewmodel

import android.app.Application
import androidx.lifecycle.*
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.Utils
import cs10.apps.travels.tracer.enums.TransportType
import cs10.apps.travels.tracer.modules.lines.db.LinesDao
import cs10.apps.travels.tracer.model.joins.RatedBusLine
import cs10.apps.travels.tracer.model.lines.CustomBusLine
import cs10.apps.travels.tracer.viewmodel.RootVM
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
                    it.speed = Utils.calculateAverageSpeed(stats)
                }
            }

            // calculate speed for trains (all by now)
            val train = RatedBusLine(-1, -1, null, R.color.train, 0.0, 0)
            val stats = rootVM.database.viajesDao().getRecentFinishedTravelsFromType(TransportType.TRAIN.ordinal)
            train.speed = Utils.calculateAverageSpeed(stats)
            lines.add(train)

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