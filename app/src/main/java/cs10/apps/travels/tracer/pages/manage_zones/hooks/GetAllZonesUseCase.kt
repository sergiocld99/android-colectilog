package cs10.apps.travels.tracer.pages.manage_zones.hooks

import cs10.apps.travels.tracer.pages.manage_zones.db.ZonesDao
import cs10.apps.travels.tracer.model.Zone

class GetAllZonesUseCase(private val dao: ZonesDao) {

    suspend operator fun invoke(): MutableList<Zone> {
        val data = dao.getAll()

        if (data.isEmpty()) return mutableListOf()
        return data
    }
}