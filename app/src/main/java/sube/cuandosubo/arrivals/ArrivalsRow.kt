package sube.cuandosubo.arrivals

import org.jsoup.nodes.Element
import java.util.*

class ArrivalsRow(val line: String, var destination: String, val status: String) {
    val arrivalTime: Calendar = Calendar.getInstance()
    var ramal: String? = null

    companion object {

        fun from(e: Element) : ArrivalsRow? {
            val l = e.selectFirst(".arrivalsRouteEntry")?.child(0)?.text()
            val d = e.selectFirst(".arrivalsDestinationEntry")?.child(0)?.text()
            val s = e.selectFirst(".arrivalsStatusEntry")?.text()
            // val at = element.selectFirst(".arrivalsTimeEntry")?.text()

            if (!l.isNullOrEmpty() && !d.isNullOrEmpty() && !s.isNullOrEmpty()){
                val nl = l.replace(Regex("[A-Z]"), "")
                val nd = if (d.startsWith("a ")) d.substring(2) else d
                val ns = if (s == "NOW") "0" else s
                return ArrivalsRow(nl, nd, ns)
            }

            return null
        }
    }

    init {
        val minutes = status.toInt()
        arrivalTime.add(Calendar.MINUTE, minutes)

        val i = destination.indexOf(" x ")

        if (i > 0){
            ramal = destination.substringAfter(" x ")
            destination = destination.substringBefore(" x ")

            // Fix: 338B es Pasco, no Centenario
            if (ramal == "CENTENARIO") ramal = "Pasco"
        }
    }

    fun getArrivalTimeStr() : String {
        val h = arrivalTime[Calendar.HOUR_OF_DAY]
        val m = arrivalTime[Calendar.MINUTE]
        val mStr = if (m < 10) "0$m" else "$m"
        return "$h:$mStr"
    }

    override fun toString(): String {
        return "line: $line, destination: $destination, status: $status', arrives at: ${getArrivalTimeStr()}"
    }
}