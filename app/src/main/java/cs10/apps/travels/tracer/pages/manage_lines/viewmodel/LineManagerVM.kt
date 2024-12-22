package cs10.apps.travels.tracer.pages.manage_lines.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import cs10.apps.travels.tracer.common.enums.TransportType
import cs10.apps.travels.tracer.model.joins.RatedBusLine
import cs10.apps.travels.tracer.model.lines.CustomBusLine
import cs10.apps.travels.tracer.pages.manage_lines.types.Since
import cs10.apps.travels.tracer.pages.registry.db.TravelsDao
import cs10.apps.travels.tracer.utils.Utils
import cs10.apps.travels.tracer.viewmodel.RootVM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * ViewModel para la administración de líneas de colectivos
 */
class LineManagerVM(application: Application) : AndroidViewModel(application) {

    // mis live data
    private var myLines = MutableLiveData<List<RatedBusLine>>()
    private lateinit var editingLine: CustomBusLine


    fun load(rootVM: RootVM, recent: Boolean = true){
        rootVM.enableLoading()

        viewModelScope.launch(Dispatchers.IO){
            val linesDao = rootVM.database.linesDao()
            val currentTime = Calendar.getInstance()
            val currentYear = currentTime.get(Calendar.YEAR)
            val startMonth = currentTime.get(Calendar.MONTH) - 2
            val since = Since(currentYear, startMonth)

            // remove failed creations
            rootVM.database.linesDao().deleteLine(-1)

            // create lines from travels that don't exist in lines dao yet
            val pendingLines = rootVM.database.travelsDao().getPendingToCreateLines()
            pendingLines.forEach {
                linesDao.insert(CustomBusLine(0, it, null, 0))
            }

            // load all lines from travel table
            val lines = if (recent) linesDao.getAllRecentWithRates(currentYear, startMonth)
            else linesDao.getAllWithRates()

            // calculate speed for each line using last travel
            lines.forEach {
                if (it.number != null){
                    val stats = rootVM.database.viajesDao().getRecentFinishedTravelsFromLine(it.number)
                    it.speed = Utils.calculateAverageSpeed(stats)
                }
            }

            buildOtherTransportTypes(rootVM.database.travelsDao(), lines, since)
            myLines.postValue(lines)
            rootVM.disableLoading()
        }
    }

    private suspend fun buildOtherTransportTypes(dao: TravelsDao, lines: MutableList<RatedBusLine>, since: Since) {
        lines.apply {
            buildTransportType(dao, TransportType.CAR, "En Auto", since)?.let { this.add(it) }
            buildTransportType(dao, TransportType.METRO, "En Subte", since)?.let { this.add(it) }
            buildTransportType(dao, TransportType.TRAIN, "En Tren", since)?.let { this.add(it) }
        }
    }

    private suspend fun buildTransportType(dao: TravelsDao, type: TransportType, label: String, since: Since): RatedBusLine? {
        val avgRate = dao.getAverageRateForTypeSince(type.ordinal, since.currentYear, since.startMonth) ?: return null
        val reviewsCount = dao.getReviewsCountForTypeSince(type.ordinal, since.currentYear, since.startMonth)
        val recentTravelStats = dao.getRecentFinishedTravelsFromType(type.ordinal)

        val line = RatedBusLine(-type.ordinal, -1, label, type.getColor(), avgRate, reviewsCount)
        line.speed = Utils.calculateAverageSpeed(recentTravelStats)
        return line
    }

    fun observe(lifecycleOwner: LifecycleOwner, observer: Observer<List<RatedBusLine>>) {
        myLines.observe(lifecycleOwner, observer)
    }

    fun selectEditing(line: CustomBusLine) {
        editingLine = line
    }
}