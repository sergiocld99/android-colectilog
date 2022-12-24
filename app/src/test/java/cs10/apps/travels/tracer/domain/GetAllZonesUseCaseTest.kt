package cs10.apps.travels.tracer.domain

import cs10.apps.travels.tracer.db.ZonesDao
import cs10.apps.travels.tracer.model.Zone
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class GetAllZonesUseCaseTest {

    @MockK
    private lateinit var zonesDao: ZonesDao

    lateinit var getAllZonesUseCase: GetAllZonesUseCase

    @Before
    fun doBefore() {
        MockKAnnotations.init(this)
        getAllZonesUseCase = GetAllZonesUseCase(zonesDao)
    }

    @Test
    fun whenDatabaseIsEmptyReturnEmpty() = runBlocking {
        coEvery { zonesDao.getAll() } returns mutableListOf()

        val result = getAllZonesUseCase()
        assert(result.isEmpty())
    }

    @Test
    fun whenDatabaseContainsZonesReturnAll() = runBlocking {
        val zone1 = Zone("Varela", -34.0, -33.7, -57.9, -57.4)
        val zone2 = Zone("Quilmes", -35.7, -34.9, -58.5, -58.2)
        coEvery { zonesDao.getAll() } returns mutableListOf(zone1, zone2)

        val result = getAllZonesUseCase()
        assert(result.size == 2)
        assert(result[0] == zone1)
        assert(result[1] == zone2)
    }
}