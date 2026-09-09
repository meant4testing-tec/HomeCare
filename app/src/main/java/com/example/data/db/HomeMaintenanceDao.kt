package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.Asset
import com.example.data.model.AssetWithDetails
import com.example.data.model.LogWithAsset
import com.example.data.model.MaintenanceLog
import com.example.data.model.MaintenanceSchedule
import com.example.data.model.ScheduleWithAsset
import kotlinx.coroutines.flow.Flow

@Dao
interface HomeMaintenanceDao {

    // --- Assets ---
    @Query("SELECT * FROM assets ORDER BY name ASC")
    fun getAllAssets(): Flow<List<Asset>>

    @Query("SELECT * FROM assets WHERE id = :id LIMIT 1")
    fun getAssetById(id: String): Flow<Asset?>

    @Transaction
    @Query("SELECT * FROM assets WHERE id = :id LIMIT 1")
    fun getAssetWithDetails(id: String): Flow<AssetWithDetails?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: Asset)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssets(assets: List<Asset>)

    @Update
    suspend fun updateAsset(asset: Asset)

    @Delete
    suspend fun deleteAsset(asset: Asset)

    @Query("DELETE FROM assets WHERE id = :id")
    suspend fun deleteAssetById(id: String)

    // --- Maintenance Schedules ---
    @Transaction
    @Query("SELECT * FROM maintenance_schedules ORDER BY nextDueDate ASC")
    fun getAllSchedulesWithAsset(): Flow<List<ScheduleWithAsset>>

    @Transaction
    @Query("SELECT * FROM maintenance_schedules ORDER BY nextDueDate ASC")
    suspend fun getAllSchedulesWithAssetDirect(): List<ScheduleWithAsset>

    @Query("SELECT * FROM maintenance_schedules WHERE assetId = :assetId ORDER BY nextDueDate ASC")
    fun getSchedulesForAsset(assetId: String): Flow<List<MaintenanceSchedule>>

    @Query("SELECT * FROM maintenance_schedules WHERE id = :id LIMIT 1")
    suspend fun getScheduleById(id: String): MaintenanceSchedule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: MaintenanceSchedule)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<MaintenanceSchedule>)

    @Update
    suspend fun updateSchedule(schedule: MaintenanceSchedule)

    @Delete
    suspend fun deleteSchedule(schedule: MaintenanceSchedule)

    @Query("DELETE FROM maintenance_schedules WHERE id = :id")
    suspend fun deleteScheduleById(id: String)

    // --- Maintenance Logs ---
    @Transaction
    @Query("SELECT * FROM maintenance_logs ORDER BY completedDate DESC")
    fun getAllLogsWithAsset(): Flow<List<LogWithAsset>>

    @Query("SELECT * FROM maintenance_logs WHERE assetId = :assetId ORDER BY completedDate DESC")
    fun getLogsForAsset(assetId: String): Flow<List<MaintenanceLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: MaintenanceLog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<MaintenanceLog>)

    @Query("DELETE FROM maintenance_logs WHERE id = :id")
    suspend fun deleteLogById(id: String)

    // --- Search Queries ---
    @Query("""
        SELECT * FROM assets 
        WHERE name LIKE '%' || :query || '%' 
           OR category LIKE '%' || :query || '%' 
           OR brandModel LIKE '%' || :query || '%' 
           OR location LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    fun searchAssets(query: String): Flow<List<Asset>>

    @Transaction
    @Query("""
        SELECT * FROM maintenance_schedules 
        WHERE taskName LIKE '%' || :query || '%' 
           OR defaultServiceProvider LIKE '%' || :query || '%'
        ORDER BY nextDueDate ASC
    """)
    fun searchSchedules(query: String): Flow<List<ScheduleWithAsset>>

    @Transaction
    @Query("""
        SELECT * FROM maintenance_logs 
        WHERE taskName LIKE '%' || :query || '%' 
           OR serviceProvider LIKE '%' || :query || '%'
           OR notes LIKE '%' || :query || '%'
        ORDER BY completedDate DESC
    """)
    fun searchLogs(query: String): Flow<List<LogWithAsset>>

    // --- Database Clear (for restore) ---
    @Query("DELETE FROM maintenance_logs")
    suspend fun clearLogs()

    @Query("DELETE FROM maintenance_schedules")
    suspend fun clearSchedules()

    @Query("DELETE FROM assets")
    suspend fun clearAssets()

    @Transaction
    suspend fun clearAllData() {
        clearLogs()
        clearSchedules()
        clearAssets()
    }
}
