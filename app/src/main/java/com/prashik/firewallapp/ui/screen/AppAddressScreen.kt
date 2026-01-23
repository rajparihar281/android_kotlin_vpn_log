package com.prashik.firewallapp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prashik.firewallapp.data.local.modal.AppAddressEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppAddressScreen(
    appName: String,
    packageName: String,
    addresses: List<AppAddressEntity>,
    onBackClick: () -> Unit,
    onToggleAddress: (Int, Boolean) -> Unit,
    onBlockAll: () -> Unit,
    onAllowAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Top bar
        TopAppBar(
            title = { Text(appName) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )

        // Action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onBlockAll,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Block All", color = Color.White)
            }
            Button(
                onClick = onAllowAll,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
            ) {
                Text("Allow All", color = Color.White)
            }
        }

        // Address list
        if (addresses.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No network activity recorded for this app",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(addresses) { address ->
                    AddressItem(
                        address = address,
                        onToggle = { onToggleAddress(address.id, !address.isBlocked) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AddressItem(
    address: AppAddressEntity,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${address.ipAddress}:${address.port}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = address.protocol,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Last seen: ${address.lastSeen}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = if (address.isBlocked) Color.Red else Color.Green,
                        shape = RoundedCornerShape(6.dp)
                    )
            )
        }
    }
}