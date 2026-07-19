package app.grapheneos.deskclock.clock.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.ZoneId

@Dao
interface ClockDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClock(clockEntity: ClockEntity)

    @Query("DELETE FROM clocks WHERE zoneId = :zoneId")
    suspend fun deleteClock(zoneId: ZoneId)

    @Query("SELECT * FROM clocks")
    fun getSelectedClocks(): Flow<List<ClockEntity>>
}
