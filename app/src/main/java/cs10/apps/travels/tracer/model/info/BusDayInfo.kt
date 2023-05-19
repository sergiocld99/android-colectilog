package cs10.apps.travels.tracer.model.info

import cs10.apps.travels.tracer.Utils

class BusDayInfo: BusInfo() {
    var wd = -1

    override fun getIdentifier(): String {
        return Utils.getWdCompleteString(wd)
    }

    override fun getTypeKey(): String {
        return "wd"
    }
}