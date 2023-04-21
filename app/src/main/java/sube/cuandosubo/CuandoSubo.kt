package sube.cuandosubo

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import sube.cuandosubo.arrivals.ArrivalsRow

class CuandoSubo(stopId: String) {
    private val baseUrl = "https://cuandosubo.sube.gob.ar/onebusaway-webapp/where/iphone/stop.action"
    private val url = "$baseUrl?id=$stopId"
    private val doc = Jsoup.connect(url).get()

    fun raw() : Element? = doc.body()
    fun getAddress() : String? = doc.selectFirst(".arrivalsStopAddress")?.text()

    fun getArrivals() : List<ArrivalsRow> {
        val res = mutableListOf<ArrivalsRow>()
        val rows = doc.select(".arrivalsRow")

        for (r in rows){
            ArrivalsRow.from(r)?.let { res.add(it) }
        }

        return res
    }
}