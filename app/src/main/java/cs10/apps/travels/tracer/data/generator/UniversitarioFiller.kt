package cs10.apps.travels.tracer.data.generator

import cs10.apps.travels.tracer.db.MiDB

class UniversitarioFiller(delayData: DelayData) : CircuitoFiller(UniversitarioHelper(delayData)) {

    fun createIda(miDB: MiDB){
        val salidas = listOf("7:15", "8:35", "10:00", "11:20", "12:37", "13:40", "15:10",
                                "16:17", "17:30", "18:50", "19:55", "20:52", "21:50").toTypedArray()
        createAux(Station.LA_PLATA, salidas, Station.POLICLINICO, true, miDB)
    }

    fun createVuelta(miDB: MiDB){
        val salidas = listOf("7:45", "9:15", "10:35", "11:55", "13:10", "14:10", "15:40",
                                "16:50", "18:00", "19:18", "20:22", "21:22", "22:20").toTypedArray()
        createAux(Station.POLICLINICO, salidas, Station.LA_PLATA, false, miDB)
    }

}