package com.prashik.firewallapp.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.prashik.firewallapp.data.local.dao.BlockLogDao
import com.prashik.firewallapp.data.local.dao.AppAddressDao
import com.prashik.firewallapp.data.local.modal.BlockLogEntity
import com.prashik.firewallapp.data.local.modal.AppAddressEntity


@Database(entities = [BlockLogEntity::class, AppAddressEntity::class], version = 3, exportSchema = false)
abstract class BlockLogDatabase: RoomDatabase() {
    abstract fun getBlockLogDao(): BlockLogDao
    abstract fun getAppAddressDao(): AppAddressDao
}