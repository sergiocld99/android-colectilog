package cs10.apps.travels.tracer.data.generator

// No hay estaciones que saltear
class UniversitarioHelper(delayData: DelayData) : AdvancedHelper(
    delayData,
    listOf(Station.LA_PLATA, Station.ARQUI, Station.INFO, Station.MEDICINA,
        Station.PERIODISMO, Station.DIAG_73, Station.POLICLINICO).toTypedArray(),
    listOf<Station>().toTypedArray()
) {

    override fun getRamalFor(inicio: Station?, fin: Station?,
                             haciaAdelante: Boolean, salteo: Salteo?): String {

        return "${fin?.simplified} (Universitario)"
    }
}