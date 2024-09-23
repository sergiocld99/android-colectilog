package cs10.apps.travels.tracer.live

import cs10.apps.travels.tracer.model.Point
import cs10.apps.travels.tracer.pages.live.model.StagedTravel
import org.junit.Test

class StageTravelTest {

    @Test
    fun progressTest(){
        val cruceVarela = Point(-34.780511, -58.263253)
        val lujan = Point(-34.825757, -58.224724)
        val alpargatas = Point(-34.840821, -58.189304)
        val pzaPaso = Point(-34.916457, -57.961546)
        val pzaItalia = Point(-34.910651, -57.955307)
        val terminal = Point(-34.905401, -57.954948)
        val facultad = Point(-34.908246, -57.944985)

        val travel = StagedTravel.withStops(arrayOf(cruceVarela, alpargatas, pzaItalia, facultad))
        val minDistance = cruceVarela.kmDistanceTo(facultad)

        assert(travel.totalKmDist > minDistance)
        assert(travel.calculateCurrentStage(cruceVarela) == travel.stages[0])
        assert(travel.calculateCloserPoint(terminal) == pzaItalia)

        println("Linear distance start-end: $minDistance km")
        println("Total distance: ${travel.totalKmDist} km")

        val places = listOf(cruceVarela, lujan, alpargatas, pzaPaso, pzaItalia, terminal, facultad)

        for (p in places){
            val r = travel.calculateCurrentStage(p)
            println("In $p, stage ${travel.stages.indexOf(r)}, prog ${r.progress}, pd ${r.primaryDirection}")
        }

        travel.stages.forEachIndexed { index, _ ->
            println("Stage $index relative prog is ${travel.relativeStageProg[index]}")
        }

    }

}