package com.prashik.firewallapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.prashik.firewallapp.data.local.modal.AppAddressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppAddressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddress(address: AppAddressEntity)

    @Query("SELECT * FROM app_addresses WHERE packageName = :packageName ORDER BY lastSeen DESC")
    fun getAddressesForApp(packageName: String): Flow<List<AppAddressEntity>>

    @Query("SELECT * FROM app_addresses GROUP BY packageName ORDER BY appName")
    fun getAppsWithAddresses(): Flow<List<AppAddressEntity>>

    @Query("UPDATE app_addresses SET isBlocked = :isBlocked WHERE id = :id")
    suspend fun updateBlockStatus(id: Int, isBlocked: Boolean)

    @Query("UPDATE app_addresses SET isBlocked = :isBlocked WHERE packageName = :packageName")
    suspend fun updateAllAddressesForApp(packageName: String, isBlocked: Boolean)

    @Query("SELECT * FROM app_addresses WHERE packageName = :packageName AND ipAddress = :ip AND port = :port AND protocol = :protocol LIMIT 1")
    suspend fun getAddress(packageName: String, ip: String, port: Int, protocol: String): AppAddressEntity?

    @Query("UPDATE app_addresses SET lastSeen = :timestamp WHERE packageName = :packageName AND ipAddress = :ip AND port = :port AND protocol = :protocol")
    suspend fun updateLastSeen(packageName: String, ip: String, port: Int, protocol: String, timestamp: String)
}