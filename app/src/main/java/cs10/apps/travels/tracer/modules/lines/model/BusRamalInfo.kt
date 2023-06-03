package cs10.apps.travels.tracer.modules.lines.model

class BusRamalInfo: BusInfo() {
    var ramal: String? = null

    override fun getIdentifier(): String? = ramal
    override fun getTypeKey(): String = "ramal"
}
