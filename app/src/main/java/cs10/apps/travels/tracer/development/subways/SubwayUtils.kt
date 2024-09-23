package cs10.apps.travels.tracer.development.subways

import cs10.apps.common.android.NumberUtils
import cs10.apps.travels.tracer.pages.stops.db.ParadasDao
import cs10.apps.travels.tracer.common.enums.TransportType
import cs10.apps.travels.tracer.model.Parada

object SubwayUtils {

    class SubwayLineBuilder(val line: Char) {

        fun get(x: Double, y: Double, name: String): Parada {
            val obj = Parada()
            obj.nombre = "$name ($line)"
            obj.tipo = TransportType.METRO.ordinal
            obj.latitud = x
            obj.longitud = y
            return obj
        }
    }

    fun createStations(dao: ParadasDao){
        val existantByLine = getStationsGroupedByLine(dao)

        // line A
        val existantOnes = existantByLine['A']?.map { p -> p.nombre } ?: listOf()
        val builder = SubwayLineBuilder('A')
        val toAdd = listOf(
            builder.get(-34.60873, -58.37148, "Plaza de Mayo"),
            builder.get( -34.60856, -58.37501, "Perú"),
            builder.get( -34.60879, -58.37907, "Piedras"),
            builder.get(-34.60898, -58.38306, "Lima"),
            builder.get(-34.60932, -58.38719, "Sáenz Peña"),
            builder.get(-34.60919, -58.39296, "Congreso"),
            builder.get(-34.60956, -58.39866, "Pasco"),
            builder.get(-34.60982, -58.40118, "Alberti"),
            builder.get(-34.60981, -58.40709, "Plaza Miserere"),
            builder.get(-34.61054, -58.41613, "Loria"),
            builder.get(-34.61174, -58.42285, "Castro Barros"),
            builder.get(-34.61512, -58.43039, "Río de Janeiro"),
            builder.get(-34.61811, -58.43705, "Acoyte"),
            builder.get(-34.62047, -58.44256, "Primera Junta"),
            builder.get(-34.62355, -58.44997, "Puan"),
            builder.get(-34.62640, -58.45720, "Carabobo"),
            builder.get(-34.62910, -58.46463, "San José de Flores"),
            builder.get(-34.63089, -58.47165, "San Pedrito")
        ).filter { p -> !existantOnes.contains(p.nombre) }

        // line D
        // line H
    }

    fun getStationsGroupedByLine(dao: ParadasDao): Map<Char, List<Parada>> {
        return dao.getAllByType(TransportType.METRO.ordinal)
            .groupBy { p -> p.nombre[p.nombre.length - 2] }
    }


    fun translateLine(str: String): Int? {
        return when(str){
            "A" -> -301
            "B" -> -302
            "C" -> -303
            "D" -> -304
            "E" -> -305
            "H" -> -308
            else -> null
        }
    }

    /**
     * @return if distance is less than 300 meters
     */
    fun isCombinationByNearing(x0: Double, y0: Double, x1: Double, y1: Double): Boolean {
        return NumberUtils.coordsDistanceToKm(NumberUtils.hyp(x1-x0, y1-y0)) < 0.3
    }

    fun mustCombine(start: String, end: String): Boolean {
        return extractLine(start) != extractLine(end)
    }

    fun extractLine(stopName: String): Char {
        return stopName[stopName.length - 2]
    }
}