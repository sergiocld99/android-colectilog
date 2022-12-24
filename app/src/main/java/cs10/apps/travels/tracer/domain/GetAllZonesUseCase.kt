package cs10.apps.travels.tracer.domain

import cs10.apps.travels.tracer.db.ZonesDao
import cs10.apps.travels.tracer.model.Zone

class GetAllZonesUseCase(private val dao: ZonesDao) {

    suspend operator fun invoke(): MutableList<Zone> {
        val data = dao.getAll()

        if (data.isNullOrEmpty()) return mutableListOf()
        return data
    }
}