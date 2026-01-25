#!/bin/bash

echo "=== Granular Connection Blocking Test ==="
echo ""

# Connect to device
adb shell "run-as com.prashik.firewallapp sqlite3 databases/BlockLogDatabase <<EOF
-- Show all tracked connections
.mode column
.headers on
SELECT packageName, ipAddress, port, protocol, isBlocked, lastSeen 
FROM app_addresses 
ORDER BY lastSeen DESC 
LIMIT 20;
EOF"

echo ""
echo "=== To block a specific connection ==="
echo "adb shell \"run-as com.prashik.firewallapp sqlite3 databases/BlockLogDatabase 'UPDATE app_addresses SET isBlocked=1 WHERE id=<ID>'\""
echo ""
echo "=== To allow a specific connection ==="
echo "adb shell \"run-as com.prashik.firewallapp sqlite3 databases/BlockLogDatabase 'UPDATE app_addresses SET isBlocked=0 WHERE id=<ID>'\""
