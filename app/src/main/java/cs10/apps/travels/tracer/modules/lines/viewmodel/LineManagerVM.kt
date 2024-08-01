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

            // TRAIN travels
            val train = RatedBusLine(-1, -1, "En Tren", R.color.train,
                avgUserRate = rootVM.database.travelsDao().getAverageRateForType(TransportType.TRAIN.ordinal),
                reviewsCount = rootVM.database.travelsDao().getReviewsCountForType(TransportType.TRAIN.ordinal))
            train.speed = Utils.calculateAverageSpeed(
                rootVM.database.viajesDao().getRecentFinishedTravelsFromType(TransportType.TRAIN.ordinal))
            lines.add(train)

            // CAR travels
            val cars = RatedBusLine(-2, -1, "En Auto", R.color.bus_159,
                avgUserRate = rootVM.database.travelsDao().getAverageRateForType(TransportType.CAR.ordinal),
                reviewsCount = rootVM.database.travelsDao().getReviewsCountForType(TransportType.CAR.ordinal))
            cars.speed = Utils.calculateAverageSpeed(
                rootVM.database.viajesDao().getRecentFinishedTravelsFromType(TransportType.CAR.ordinal))
            lines.add(cars)

            // METRO travels
            val subways = RatedBusLine(-3, -1, "En Subte", R.color.bus_148,
                rootVM.database.travelsDao().getAverageRateForType(TransportType.METRO.ordinal),
                rootVM.database.travelsDao().getReviewsCountForType(TransportType.METRO.ordinal))

            subways.speed = Utils.calculateAverageSpeed(rootVM.database.viajesDao().getRecentFinishedTravelsFromType(TransportType.METRO.ordinal))
            lines.add(subways)

            // Sort everything by speed
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