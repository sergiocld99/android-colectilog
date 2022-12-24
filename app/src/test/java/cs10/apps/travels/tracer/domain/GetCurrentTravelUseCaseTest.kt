package cs10.apps.travels.tracer.domain

import cs10.apps.travels.tracer.db.MiDB
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class GetCurrentTravelUseCaseTest {

    @RelaxedMockK
    private lateinit var db: MiDB

    lateinit var getCurrentTravelUseCase: GetCurrentTravelUseCase

    @Before
    fun doBefore() {
        MockKAnnotations.init(this)
        getCurrentTravelUseCase = GetCurrentTravelUseCase(db)
    }

    @Test
    fun whenDatabaseIsEmptyReturnNull() = runBlocking {
        // dado...
        coEvery { db.viajesDao().getCurrentTravel(any(), any(), any(), any()) } returns null

        // cuando...
        val result = getCurrentTravelUseCase()

        // hacer...
        assert(result == null)
    }
}