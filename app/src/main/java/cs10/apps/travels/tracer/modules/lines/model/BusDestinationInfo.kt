package cs10.apps.travels.tracer.modules.lines.model

class BusDestinationInfo: BusInfo() {
    var nombrePdaFin: String? = null

    override fun getIdentifier(): String? {
        return nombrePdaFin
    }

    override fun getTypeKey(): String {
        return "dest"
    }
}