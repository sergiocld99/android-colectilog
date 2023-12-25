package cs10.apps.travels.tracer.modules.lines.entity

class FrequentTravel(val nombrePdaInicio: String, val nombrePdaFin: String){

    override fun toString(): String {
        return "$nombrePdaInicio > $nombrePdaFin"
    }
}