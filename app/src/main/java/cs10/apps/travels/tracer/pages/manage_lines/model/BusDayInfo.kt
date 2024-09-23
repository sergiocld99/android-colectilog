package cs10.apps.travels.tracer.pages.manage_lines.model

import cs10.apps.travels.tracer.utils.Utils

class BusDayInfo: BusInfo() {
    var wd = -1

    override fun getIdentifier(): String {
        return Utils.getWdCompleteString(wd)
    }

    override fun getTypeKey(): String {
        return "wd"
    }
}