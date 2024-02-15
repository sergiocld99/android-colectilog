package cs10.apps.travels.tracer.data

import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.data.generator.Ramal
import cs10.apps.travels.tracer.data.generator.Station
import cs10.apps.travels.tracer.data.generator.FareData
import cs10.apps.travels.tracer.model.roca.HorarioTren

class ServiceRepository(val db: MiDB) {

    private val fareData = FareData()

    suspend fun getService(id: Long, targetStop: String): List<HorarioTren>? {
        val horarios = db.servicioDao().getRecorrido(id)
        if (horarios.isNullOrEmpty()) return null

        val start = horarios.first().station
        val dest = horarios.last().station
        val targetStation = Station.findByNombre(targetStop)

        for (horario in horarios) {
            horario.service = if (horario.station == targetStop) 1 else 0
            horario.tarifa = fareData.getTarifa(targetStation, horario.station)

            // combinations
            if (horario.station == Station.BERA.nombre) {
                if (start == Station.LA_PLATA.nombre) {
                    // El servicio mostrado arrancó en La Plata, me interesa a Bosques
                    horario.combination = findCombination(Ramal.BOSQUES_Q_TEMPERLEY.nombre, horario)
                    horario.combinationRamal = Station.TEMPERLEY.simplified
                } else if (dest == start){
                    // El servicio mostrado es un via circuito -> me interesa a La Plata
                    horario.combination = findCombination(Station.LA_PLATA.simplified, horario)
                    horario.combinationRamal = Station.LA_PLATA.simplified
                }
            } else if (horario.station == Station.TEMPERLEY.nombre) {
                if (start == Station.GLEW.nombre || start == Station.KORN.nombre){
                    // El servicio mostrado empezó en Glew o Korn -> me interesa a Bosques
                    horario.combination = findCombination(Ramal.BOSQUES_T.nombre, horario)
                    horario.combinationRamal = Station.BOSQUES.simplified
                } else if (dest == start || start == Station.BOSQUES.nombre){
                    // El servicio mostrado es un via circuito -> me interesa a Korn
                    horario.combination = findCombination(Station.KORN.simplified, horario)
                    horario.combinationRamal = "Korn"
                }
            }
        }

        return horarios
    }

    private suspend fun findCombination(ramal: String, horario: HorarioTren) : HorarioTren {
        val targetTime = horario.hour * 60 + horario.minute
        return db.servicioDao().getArrival(ramal, horario.station, targetTime, 10)
    }
}