package cs10.apps.travels.tracer.stops

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import cs10.apps.travels.tracer.common.enums.TransportType
import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.model.Parada
import cs10.apps.travels.tracer.model.Viaje
import cs10.apps.travels.tracer.pages.stops.editor.StopRenamer
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RenamerTest {
    private lateinit var db: MiDB
    private val busType = TransportType.BUS.ordinal

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, MiDB::class.java).allowMainThreadQueries().build()

        // crear paradas
        val p1 = Parada().fillMandatoryFields("Cruce Varela", -34.8, -58.6, busType)
        val p2 = Parada().fillMandatoryFields("Av. 1 y 48", -35.0, -57.9, busType)
        db.paradasDao().insert(p1)
        db.paradasDao().insert(p2)

        // crear viaje de prueba
        val viajecito = Viaje().fillMandatoryBusFields(12,10,2023,8,15,
            338, p1.nombre, p2.nombre, 146.50, 4)

        db.viajesDao().insert(viajecito)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testTravelUpdate() {
        val oldName = "Cruce Varela"
        val newName = "Cruce Varela 2"

        runBlocking {
            //val stop = db.paradasDao().getByName(oldName)
            //stop.nombre = newName
            //db.paradasDao().update(stop)
            db.safeStopsDao().renameStop(oldName, newName)

            StopRenamer.applyChanges(oldName, newName, db)

            val newTravel = db.viajesDao().getById(1)
            assert(newTravel.nombrePdaInicio == newName)
        }
    }
}