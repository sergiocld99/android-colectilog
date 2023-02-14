package cs10.apps.travels.tracer.domain

import cs10.apps.travels.tracer.db.ViajesDao
import cs10.apps.travels.tracer.model.location.TravelDistance
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class GetMaxTravelDistanceUseCaseTest {

    @MockK
    private lateinit var dao: ViajesDao

    lateinit var getMaxTravelDistanceUseCase: GetMaxTravelDistanceUseCase

    @Before
    fun doBefore() {
        MockKAnnotations.init(this)
        getMaxTravelDistanceUseCase = GetMaxTravelDistanceUseCase(dao)
    }

    @Test
    fun whenDatabaseIsEmptyReturnNull() = runBlocking {
        coEvery { dao.travelDistances } returns null
        val result = getMaxTravelDistanceUseCase()
        assert(result == null)
    }

    @Test
    fun whenNoDistanceFoundReturnNull() = runBlocking {
        coEvery { dao.travelDistances } returns emptyList()
        val result = getMaxTravelDistanceUseCase()
        assert(result == null)
    }

    @Test
    fun whenDistanceFoundReturnAll() = runBlocking {
        val list = listOf(
            TravelDistance(0, 0.10, 0.25),
            TravelDistance(1, 1.30, 1.50),
            TravelDistance(2, 0.20, 0.60)
        )

        coEvery { dao.travelDistances } returns list

        val result = getMaxTravelDistanceUseCase()
        assert(result == TravelDistance(1, 1.30, 1.50).distance)

        // comprobar que se ordenó la lista
        assert(list[0].distance < list[1].distance)
        assert(list[1].distance < list[2].distance)
        assert(list[2].id == 1L)
    }

}