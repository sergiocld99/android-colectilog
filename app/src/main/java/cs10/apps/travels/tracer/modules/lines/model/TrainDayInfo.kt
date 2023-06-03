package cs10.apps.travels.tracer.modules.lines.model

import cs10.apps.travels.tracer.Utils

class TrainDayInfo: TrainInfo() {
    var wd = -1

    override fun getIdentifier(): String {
        return Utils.getWdCompleteString(wd)
    }

    override fun getTypeKey(): String {
        return "wd"
    }
}