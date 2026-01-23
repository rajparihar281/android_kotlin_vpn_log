# Granular Connection Filtering - Per-App IP:Port Blocking

## Overview
This feature allows you to view all unique IP addresses and ports that each app connects to, and selectively block or allow specific connections while keeping the app itself unblocked.

## How It Works

### 1. Connection Tracking
- Every packet is tracked and stored in the `app_addresses` table
- Tracks: Package name, IP address, Port, Protocol, First seen, Last seen
- Automatically updates when connections are reused

### 2. Granular Blocking
You can now:
- **Block specific IP:Port** for an app while allowing others
- **Allow specific IP:Port** for an app while blocking others
- View all unique connections per app
- See when each connection was first and last used

### 3. Database Schema

**AppAddressEntity:**
```kotlin
- id: Int (Primary Key)
- packageName: String
- appName: String
- ipAddress: String
- port: Int
- protocol: String (TCP/UDP)
- isBlocked: Boolean
- firstSeen: String
- lastSeen: String
```

## Usage Examples

### Example 1: Block Ads While Allowing App Functionality
```
App: "Social Media App"
Connections:
- 192.168.1.1:443 (API Server) → ALLOWED
- 203.0.113.50:443 (Ad Server) → BLOCKED
- 198.51.100.10:80 (Analytics) → BLOCKED
```

### Example 2: Allow Only Specific Services
```
App: "Messaging App"
Connections:
- 172.217.14.202:443 (Google Services) → ALLOWED
- 157.240.2.35:443 (Facebook CDN) → BLOCKED
- 13.107.42.14:443 (Microsoft Telemetry) → BLOCKED
```

## API Methods

### AppAddressDao Methods:
```kotlin
// Get all connections for an app
fun getAddressesForApp(packageName: String): Flow<List<AppAddressEntity>>

// Block/Allow specific connection
suspend fun updateBlockStatus(id: Int, isBlocked: Boolean)

// Block/Allow all connections for an app
suspend fun updateAllAddressesForApp(packageName: String, isBlocked: Boolean)

// Check if specific connection is blocked
suspend fun getAddress(packageName: String, ip: String, port: Int, protocol: String): AppAddressEntity?
```

### ConnectionViewModel Methods:
```kotlin
// Get connections for display
fun getConnectionsForApp(packageName: String): Flow<List<AppAddressEntity>>

// Toggle single connection
fun toggleConnectionBlock(id: Int, isBlocked: Boolean)

// Bulk operations
fun blockAllConnectionsForApp(packageName: String)
fun allowAllConnectionsForApp(packageName: String)
```

## Implementation Details

### Packet Processing Flow:
1. **Capture Packet** → Parse IP, Port, Protocol
2. **Identify App** → Get package name from UID
3. **Track Connection** → Store/Update in database
4. **Check Block Status** → Query if this specific IP:Port is blocked
5. **Forward or Drop** → Based on block status

### Key Code in FirewallVpnService:
```kotlin
// Track the connection
trackAppAddress(packageName, appName, trafficLogResponse)

// Check if this specific address is blocked
val isAddressBlocked = isAddressBlockedForApp(
    packageName, 
    trafficLogResponse.dstIp, 
    trafficLogResponse.dstPort, 
    trafficLogResponse.protocolByte
)

// Block if either app is blocked OR specific address is blocked
if (isAppBlocked || isAddressBlocked) {
    // Drop packet
}
```

## UI Components

### AppConnectionsScreen
- Displays all unique connections for an app
- Shows IP:Port, Protocol, Last seen timestamp
- Toggle button to block/allow each connection
- Visual indicator (red for blocked, green for allowed)

### ConnectionItem
- Card-based UI for each connection
- Displays connection details
- Icon button to toggle block status
- Color-coded status indicator

## Use Cases

### 1. Ad Blocking
Block known ad server IPs while allowing app functionality

### 2. Privacy Protection
Block analytics and tracking servers while keeping core features

### 3. Data Saving
Block high-bandwidth connections (video ads, auto-play) while allowing text content

### 4. Security
Block suspicious or malicious IPs while allowing legitimate services

### 5. Testing
Test app behavior when specific services are unavailable

## Performance Considerations

### Database Queries
- Indexed by packageName for fast lookups
- Cached in memory during packet processing
- Batch updates for multiple connections

### Memory Usage
- Only active connections are tracked
- Old connections can be pruned based on lastSeen timestamp
- Efficient data structures for fast lookups

## Future Enhancements

### Possible Additions:
1. **Domain Name Resolution** - Show domain names instead of IPs
2. **Connection Statistics** - Track bandwidth per connection
3. **Auto-blocking** - ML-based detection of ad/tracking servers
4. **Import/Export Rules** - Share blocking rules between devices
5. **Whitelist Mode** - Block all except allowed connections
6. **Time-based Rules** - Block connections during specific hours
7. **Geolocation** - Block connections to specific countries

## Testing

### Test Scenario 1: Block Single Connection
1. Open app and let it make connections
2. View connections in AppConnectionsScreen
3. Block one specific IP:Port
4. Verify that connection is blocked
5. Verify other connections still work

### Test Scenario 2: Allow Only Specific Connections
1. Block entire app
2. View connections (from logs)
3. Allow specific IP:Port
4. Verify only that connection works

### Test Scenario 3: Bulk Operations
1. Block all connections for an app
2. Verify all are blocked
3. Allow all connections
4. Verify all work again

This feature provides fine-grained control over app network access, enabling advanced firewall capabilities beyond simple app-level blocking.