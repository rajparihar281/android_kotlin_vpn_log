# Address Management Feature Usage Guide

## Overview
The new address management feature allows you to view and control network access for individual IP addresses and ports used by each app, providing granular control over network traffic.

## Features

### 1. View Unique Addresses per App
- Navigate to the "Network Apps" tab in the bottom navigation
- See all apps that have recorded network activity
- Tap on any app to view its unique IP addresses and ports

### 2. Address-Level Blocking/Allowing
For each app, you can:
- **View all unique addresses**: See IP:Port combinations the app has accessed
- **Block specific addresses**: Prevent the app from accessing specific servers
- **Allow specific addresses**: Allow access to specific servers while blocking others
- **Block all addresses**: Block all network access for the app
- **Allow all addresses**: Allow all network access for the app

### 3. Address Information
Each address entry shows:
- IP address and port number
- Protocol (TCP/UDP)
- First seen timestamp
- Last seen timestamp
- Current blocking status (red = blocked, green = allowed)

## How It Works

### 1. Traffic Monitoring
- The VPN service monitors all network traffic
- For each packet, it records the destination IP and port
- Addresses are stored in the database with app association

### 2. Selective Blocking
- When a packet is processed, the system checks:
  1. Is the entire app blocked?
  2. Is this specific address blocked for this app?
- If either condition is true, the packet is dropped

### 3. Database Storage
- `AppAddressEntity` stores unique addresses per app
- Tracks first/last seen timestamps
- Maintains blocking status per address

## Usage Examples

### Example 1: Block Social Media Ads
1. Open a social media app and let it load content
2. Go to "Network Apps" → Select the social media app
3. Look for advertising domains (e.g., ads.facebook.com)
4. Block specific ad server addresses while keeping content servers allowed

### Example 2: Allow Only Essential Services
1. Find an app that connects to multiple servers
2. Block all addresses for the app
3. Selectively allow only essential server addresses
4. The app will only be able to connect to allowed addresses

### Example 3: Monitor App Behavior
1. Use the "Network Apps" tab to see which apps are making network requests
2. View the addresses each app connects to
3. Identify suspicious or unexpected connections
4. Block unwanted connections while preserving app functionality

## Technical Implementation

### Database Schema
```kotlin
@Entity(tableName = "app_addresses")
data class AppAddressEntity(
    val packageName: String,
    val appName: String,
    val ipAddress: String,
    val port: Int,
    val protocol: String,
    val isBlocked: Boolean = false,
    val firstSeen: String,
    val lastSeen: String
)
```

### Key Components
- `AppAddressDao`: Database operations for address management
- `AppAddressScreen`: UI for managing addresses of a specific app
- `NetworkAppsScreen`: UI for listing apps with network activity
- Enhanced `FirewallVpnService`: Tracks addresses and applies blocking rules

## Benefits
1. **Granular Control**: Block specific services while keeping others functional
2. **Privacy Protection**: Block tracking and analytics endpoints
3. **Bandwidth Management**: Block high-bandwidth addresses
4. **Security**: Block suspicious or malicious endpoints
5. **App Functionality**: Maintain core app features while blocking unwanted connections