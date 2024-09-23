package cs10.apps.travels.tracer.live

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.pages.stops.db.SafeStopsDao
import cs10.apps.travels.tracer.common.enums.TransportType
import cs10.apps.travels.tracer.model.Parada
import cs10.apps.travels.tracer.model.Viaje
import cs10.apps.travels.tracer.pages.live.utils.MediumStopsManager
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TrainMediumStopsTest {
    private lateinit var db: MiDB
    private lateinit var dao: SafeStopsDao
    private val trainType = TransportType.TRAIN
    private lateinit var adrogueTravel: Viaje
    private lateinit var laPlataToVarelaTravel: Viaje

    @Before
    fun setup(){
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, MiDB::class.java).allowMainThreadQueries().build()
        dao = db.safeStopsDao()

        // create stops
        val stop1 = Parada().fillMandatoryFields("Km 26", -34.80, -58.30, trainType.ordinal)
        val stop2 = Parada().fillMandatoryFields("Estación Adrogué", -34.79, -58.39, trainType.ordinal)
        val stop3 = Parada().fillMandatoryFields("Estación La Plata", -34.90, -57.95, trainType.ordinal)
        val stop4 = Parada().fillMandatoryFields("Estación Varela", -34.81, -58.27, trainType.ordinal)

        // save stops
        db.paradasDao().insert(stop1)
        db.paradasDao().insert(stop2)
        db.paradasDao().insert(stop3)
        db.paradasDao().insert(stop4)

        // create travel
        adrogueTravel = Viaje().fillMandatoryFields(17, 11, 2023, 16, 0,
            trainType.ordinal, stop1.nombre, stop2.nombre, 20.0, 5)

        laPlataToVarelaTravel = Viaje().fillMandatoryFields(27, 11, 2023, 15, 30,
            trainType.ordinal, stop3.nombre, stop4.nombre, 20.0, 1)

        // save travel
        db.viajesDao().insert(adrogueTravel)
    }

    @After
    fun tearDown(){
        db.close()
    }

    // ---- TESTS ----------------------------------

    @Test
    fun insertBerazateguiMS(){
        runBlocking {
            val msm = MediumStopsManager(laPlataToVarelaTravel).buildStops(db)
            msm.add("Estación Berazategui", "Estación La Plata", "Estación Varela", db)
            msm.add("Estación Bosques", "Estación Berazategui", "Estación Varela", db)
            msm.rebuildStops(db)

            assert(msm.countStops() == 4)
        }
    }

    @Test
    fun insertTemperleyMS(){
        runBlocking {
            // levantar paradas intermedias
            val mediumStopsManager = MediumStopsManager(adrogueTravel).buildStops(db)
            assert(mediumStopsManager.countStops() == 2)

            // agregar temperley
            mediumStopsManager.add("Estación Temperley", "Km 26", "Estación Adrogué", db)

            // reconstruir y comparar
            mediumStopsManager.rebuildStops(db)
            assert(mediumStopsManager.countStops() == 3)

            // agregar lomas antes de temperley
            mediumStopsManager.add("Estación Lomas de Zamora", "Km 26", "Estación Temperley", db)

            // reconstruir y comparar
            mediumStopsManager.rebuildStops(db)
            assert(mediumStopsManager.countStops() == 4)
        }
    }
}