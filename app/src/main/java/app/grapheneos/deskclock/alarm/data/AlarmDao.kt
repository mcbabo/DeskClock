package app.grapheneos.deskclock.alarm.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAlarm(alarm: AlarmEntity): Long

    @Update
    suspend fun updateAlarm(alarm: AlarmEntity)

    @Delete
    suspend fun deleteAlarm(alarm: AlarmEntity)

    @Query("SELECT * FROM alarms WHERE id = :id")
    suspend fun getAlarmById(id: Long): AlarmEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstance(instance: AlarmInstance): Long

    @Update
    suspend fun updateInstance(instance: AlarmInstance)

    @Delete
    suspend fun deleteInstance(instance: AlarmInstance)

    @Query("SELECT * FROM instances WHERE id = :id")
    suspend fun getInstanceById(id: Long): AlarmInstance?

    @Query("SELECT * FROM instances WHERE alarmId = :alarmId")
    suspend fun getInstanceForAlarm(alarmId: Long): AlarmInstance?

    @Query("SELECT * FROM instances")
    suspend fun getAllInstances(): List<AlarmInstance>

    @Query("DELETE FROM instances WHERE alarmId = :alarmId")
    suspend fun deleteInstancesForAlarm(alarmId: Long)

    @Transaction
    @Query("SELECT * FROM alarms ORDER BY hour ASC, minute ASC")
    fun getAllAlarmsWithInstances(): Flow<List<AlarmWithInstance>>

    @Transaction
    @Query(
        """
        SELECT * FROM alarms 
        WHERE id = (SELECT alarmId FROM instances WHERE id = :instanceId)
    """
    )
    suspend fun getAlarmWithInstanceByInstanceId(instanceId: Long): AlarmWithInstance?

    @Query("SELECT * FROM instances WHERE timeInMillis > :currentTimeMs ORDER BY timeInMillis ASC")
    suspend fun getUpcomingInstances(currentTimeMs: Long): List<AlarmInstance>

    @Query("SELECT * FROM alarms WHERE ringtoneUri = :uri")
    suspend fun getAlarmsByRingtoneUri(uri: android.net.Uri): List<AlarmEntity>
}
