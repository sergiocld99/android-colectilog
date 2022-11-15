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
    private lateinit var editingLine: CustomBusLine


    fun load(linesDao: LinesDao, rootVM: RootVM){
        rootVM.enableLoading()

        viewModelScope.launch(Dispatchers.IO){
            val lines = linesDao.getAll()
            val numbers1 = linesDao.getCustomNumbers()

            linesDao.getAllFromViajes().forEach { number ->
                if (!numbers1.contains(number)) {
                    val obj = CustomBusLine(0, number, null, 0)
                    linesDao.insert(obj)
                    lines.add(obj)
                    numbers1.add(number)
                }
            }

            lines.sort()
            myLines.postValue(lines)

            rootVM.disableLoading()
        }
    }

    fun observe(lifecycleOwner: LifecycleOwner, observer: Observer<List<CustomBusLine>>) {
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