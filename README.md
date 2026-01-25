# Android Kotlin VPN Log

A VPN-based firewall app for Android that monitors network traffic and blocks blacklisted applications while maintaining internet connectivity for allowed apps.

## Features

- **VPN-based Traffic Monitoring**: Uses Android VpnService to capture all network traffic
- **Packet Forwarding**: Forwards allowed traffic to maintain internet connectivity
- **App-based Blocking**: Block specific applications from accessing the internet
- **Traffic Logging**: Log and monitor network activity
- **Real-time Updates**: Dynamic blocking list updates without VPN restart

## Packet Forwarding Implementation

The app implements packet forwarding using:

1. **UDP Forwarding**: Creates individual DatagramSockets for each UDP request
2. **TCP Forwarding**: Maintains persistent Socket connections with proper state management
3. **DNS Resolution**: Configured with Google DNS (8.8.8.8, 8.8.4.4) for reliable name resolution
4. **Native Packet Processing**: Uses PcapPlusPlus library for efficient packet parsing and building

### Technical Details

- **VPN Interface**: 10.0.0.2/24 with route 0.0.0.0/0
- **Packet Parsing**: C++ native code using PcapPlusPlus
- **Concurrent Processing**: Kotlin coroutines for non-blocking packet forwarding
- **Socket Protection**: All forwarding sockets are protected from VPN routing

## Dependencies

- PcapPlusPlus for packet processing
- Android VpnService API
- Kotlin Coroutines for async operations