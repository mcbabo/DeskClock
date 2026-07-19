package app.grapheneos.deskclock.clock.data

import kotlinx.coroutines.flow.Flow
import java.time.ZoneId

class ClockRepository(
    private val clockDao: ClockDao
) {
    fun getSelectedClocks(): Flow<List<ClockEntity>> = clockDao.getSelectedClocks()

    suspend fun addZone(zoneId: ZoneId) {
        clockDao.insertClock(ClockEntity(zoneId = zoneId))
    }

    suspend fun removeZone(zoneId: ZoneId) {
        clockDao.deleteClock(zoneId)
    }
}
