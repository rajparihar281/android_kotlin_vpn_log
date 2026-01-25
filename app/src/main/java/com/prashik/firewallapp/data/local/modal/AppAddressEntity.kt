package com.prashik.firewallapp.data.local.modal

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_addresses")
data class AppAddressEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val packageName: String,
    val appName: String,
    val ipAddress: String,
    val port: Int,
    val protocol: String,
    val isBlocked: Boolean = false,
    val firstSeen: String,
    val lastSeen: String
)