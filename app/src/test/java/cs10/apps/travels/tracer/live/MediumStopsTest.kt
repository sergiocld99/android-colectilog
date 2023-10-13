package cs10.apps.travels.tracer.live

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.db.SafeStopsDao
import cs10.apps.travels.tracer.enums.TransportType
import cs10.apps.travels.tracer.model.Parada
import cs10.apps.travels.tracer.model.Viaje
import cs10.apps.travels.tracer.modules.live.entity.MediumStop
import cs10.apps.travels.tracer.modules.live.utils.MediumStopsManager
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MediumStopsTest {
    private lateinit var db: MiDB
    private lateinit var dao: SafeStopsDao
    private lateinit var viajecito: Viaje
    private val BUS_TYPE = TransportType.BUS.ordinal

    @Before
    fun setup() {
        //val context: Context = ApplicationProvider.getApplicationContext()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, MiDB::class.java).allowMainThreadQueries().build()
        dao = db.safeStopsDao()

        // crear paradas
        val p1 = Parada().fillMandatoryFields("Cruce Varela", -34.8, -58.6, BUS_TYPE)
        val p2 = Parada().fillMandatoryFields("Av. 1 y 48", -35.0, -57.9, BUS_TYPE)
        db.paradasDao().insert(p1)
        db.paradasDao().insert(p2)

        // crear viaje de prueba
        viajecito = Viaje().fillMandatoryBusFields(12,10,2023,8,15,
            338, p1.nombre, p2.nombre, 146.50, 4)

        db.viajesDao().insert(viajecito)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun emptyBehaviour() {
        runBlocking {
            // levantar paradas intermedias
            val mediumStopsManager = MediumStopsManager(viajecito).buildStops(db)
            assert(mediumStopsManager.stops.size == 2)
        }
    }

    @Test
    fun addingFirstOne() {
        runBlocking {
            val mediumStopsManager = MediumStopsManager(viajecito).buildStops(db)
            mediumStopsManager.add("Luján y RP36", "Cruce Varela", "Av. 1 y 48", db)

            // levantar paradas intermedias
            mediumStopsManager.rebuildStops(db)
            assert(mediumStopsManager.countStops() == 3)
        }
    }

    @Test
    fun addingInFirstHalf() {
        addingFirstOne()

        runBlocking {
            val mediumStopsManager = MediumStopsManager(viajecito).buildStops(db)
            mediumStopsManager.add("Thevenet y RP36", "Cruce Varela", "Luján y RP36", db)

            // levantar paradas intermedias
            mediumStopsManager.rebuildStops(db)
            assert(mediumStopsManager.countStops() == 4)
        }
    }

    @Test
    fun addingInSecondHalf() {
        addingFirstOne()

        runBlocking {
            val mediumStopsManager = MediumStopsManager(viajecito).buildStops(db)
            mediumStopsManager.add("419 y Belgrano", "Luján y RP36", "Av. 1 y 48", db)

            // levantar paradas intermedias
            mediumStopsManager.rebuildStops(db)
            assert(mediumStopsManager.countStops() == 4)
        }
    }

    @Test
    fun deleteTest(){
        runBlocking {
            val ms1 = MediumStop(0, BUS_TYPE, 338, null, "Cruce Varela",
                "Luján y RP36", "Alpargatas", "Av. 1 y 48")

            val ms2 = MediumStop(0, BUS_TYPE, 338, null, "Luján y RP36",
                "Alpargatas", "Av. 1 y 48", "Av. 1 y 48")

            val msFake = MediumStop(0, BUS_TYPE, 343, null, "Cruce Varela",
                "Luján y RP36", "Av. 1 y 48", "Av. 1 y 48")

            // insertar y contar
            val id = db.safeStopsDao().insertMediumStop(ms1)
            db.safeStopsDao().insertMediumStop(ms2)
            db.safeStopsDao().insertMediumStop(msFake)
            assert(db.safeStopsDao().getMediumStopsCreatedForBusTo(338, null, "Av. 1 y 48").size == 2)

            // actualizar id
            val ms3 = MediumStop(id, BUS_TYPE, 338, null, "Cruce Varela",
                "Luján y RP36", "Alpargatas", "Av. 1 y 48")

            // borrar y contar
            db.safeStopsDao().deleteMediumStop(ms3)
            assert(db.safeStopsDao().getMediumStopsCreatedForBusTo(338, null, "Av. 1 y 48").size == 1)
        }
    }

    @Test
    fun addingSeveralInRandomOrder() {
        runBlocking {
            val man = MediumStopsManager(viajecito).buildStops(db)

            // agregar parada intermedia
            man.add("419 y Belgrano", "Cruce Varela", "Av. 1 y 48", db)
            man.rebuildStops(db)
            assert(man.countStops() == 3)

            // agregar parada intermedia
            man.add("El Ombú", "Cruce Varela", "419 y Belgrano", db)
            man.rebuildStops(db)
            assert(man.countStops() == 4)

            // agregar parada intermedia
            man.add("Alpargatas", "El Ombú", "419 y Belgrano", db)
            man.rebuildStops(db)
            assert(man.countStops() == 5)

            // agregar parada intermedia
            man.add("Senzabello y RP36", "Cruce Varela", "El Ombú", db)
            man.rebuildStops(db)
            assert(man.countStops() == 6)

            // agregar parada intermedia
            man.add("Plaza Italia", "419 y Belgrano", "Av. 1 y 48", db)
            man.rebuildStops(db)
            assert(man.countStops() == 7)
        }
    }
}