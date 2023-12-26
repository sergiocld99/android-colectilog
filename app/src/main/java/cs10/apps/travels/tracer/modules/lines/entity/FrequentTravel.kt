package cs10.apps.travels.tracer.modules.lines.entity

class FrequentTravel(val nombrePdaInicio: String, val nombrePdaFin: String){

    override fun toString(): String {
        return short(nombrePdaInicio) + " > " + short(nombrePdaFin)
    }

    private fun short(str: String): String {
        return  if (str.length > 16) str.substring(0,15) + "..." else str
    }
}