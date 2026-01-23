package com.prashik.firewallapp

import android.util.Log
import com.prashik.firewallapp.data.local.modal.TrafficLogResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.FileOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException
import java.util.concurrent.ConcurrentHashMap

class PacketForwarder(private val vpnService: FirewallVpnService) {
    
    private val tcpConnections = ConcurrentHashMap<String, TcpConnection>()
    
    data class TcpConnection(
        val socket: Socket,
        val key: String,
        var seqNum: Long = 0,
        var ackNum: Long = 0
    )
    
    fun forwardPacket(
        packetData: ByteArray,
        length: Int,
        traffic: TrafficLogResponse,
        vpnOutputStream: FileOutputStream
    ) {
        when (traffic.protocolByte) {
            "UDP" -> forwardUdpPacket(packetData, length, traffic, vpnOutputStream)
            "TCP" -> forwardTcpPacket(packetData, length, traffic, vpnOutputStream)
            else -> {
                // For other protocols, just pass through
                try {
                    vpnOutputStream.write(packetData, 0, length)
                } catch (e: Exception) {
                    Log.e("PACKET_FORWARD", "Error forwarding packet", e)
                }
            }
        }
    }
    
    private fun forwardUdpPacket(
        packetData: ByteArray,
        length: Int,
        traffic: TrafficLogResponse,
        vpnOutputStream: FileOutputStream
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val payload = NativeBridge.extractUdpPayload(packetData, length) ?: return@launch
                
                val socket = DatagramSocket()
                vpnService.protect(socket)
                socket.soTimeout = 5000
                
                val address = InetAddress.getByName(traffic.dstIp)
                val packet = DatagramPacket(payload, payload.size, address, traffic.dstPort)
                
                Log.d("UDP_FORWARD", "Sending UDP to ${traffic.dstIp}:${traffic.dstPort}")
                socket.send(packet)
                
                val buffer = ByteArray(2048)
                val response = DatagramPacket(buffer, buffer.size)
                
                try {
                    socket.receive(response)
                    val responseData = buffer.copyOf(response.length)
                    
                    val responsePacket = NativeBridge.buildUdpResponsePacket(
                        responseData,
                        response.address.hostAddress ?: "0.0.0.0",
                        response.port,
                        "10.0.0.2",
                        traffic.srcPort
                    )
                    
                    vpnOutputStream.write(responsePacket)
                    Log.d("UDP_FORWARD", "UDP response forwarded")
                    
                } catch (e: SocketTimeoutException) {
                    Log.w("UDP_FORWARD", "UDP timeout for ${traffic.dstIp}:${traffic.dstPort}")
                } finally {
                    socket.close()
                }
                
            } catch (e: Exception) {
                Log.e("UDP_FORWARD", "UDP forwarding error", e)
            }
        }
    }
    
    private fun forwardTcpPacket(
        packetData: ByteArray,
        length: Int,
        traffic: TrafficLogResponse,
        vpnOutputStream: FileOutputStream
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val key = "${traffic.srcIp}:${traffic.srcPort}->${traffic.dstIp}:${traffic.dstPort}"
                val flags = NativeBridge.getTcpFlags(packetData, length)
                val payload = NativeBridge.extractTcpPayload(packetData, length)
                
                // Handle SYN packets (new connections)
                if ((flags and 0x02) != 0) { // SYN flag
                    handleTcpSyn(key, traffic, vpnOutputStream)
                    return@launch
                }
                
                // Handle existing connections
                val connection = tcpConnections[key]
                if (connection != null && payload != null && payload.isNotEmpty()) {
                    try {
                        connection.socket.getOutputStream().write(payload)
                        connection.socket.getOutputStream().flush()
                        Log.d("TCP_FORWARD", "Sent ${payload.size} bytes to ${traffic.dstIp}:${traffic.dstPort}")
                    } catch (e: Exception) {
                        Log.e("TCP_FORWARD", "Error sending TCP data", e)
                        cleanupTcpConnection(key)
                    }
                }
                
                // Handle FIN packets (connection close)
                if ((flags and 0x01) != 0) { // FIN flag
                    cleanupTcpConnection(key)
                }
                
            } catch (e: Exception) {
                Log.e("TCP_FORWARD", "TCP forwarding error", e)
            }
        }
    }
    
    private fun handleTcpSyn(
        key: String,
        traffic: TrafficLogResponse,
        vpnOutputStream: FileOutputStream
    ) {
        try {
            val socket = Socket()
            vpnService.protect(socket)
            socket.soTimeout = 30000
            
            Log.d("TCP_FORWARD", "Establishing TCP connection to ${traffic.dstIp}:${traffic.dstPort}")
            socket.connect(InetSocketAddress(traffic.dstIp, traffic.dstPort), 10000)
            
            val connection = TcpConnection(socket, key)
            tcpConnections[key] = connection
            
            // Send SYN-ACK response
            val synAckPacket = NativeBridge.buildTcpPacket(
                "10.0.0.2",
                traffic.srcIp,
                traffic.dstPort,
                traffic.srcPort,
                0x12, // SYN + ACK
                1,
                1,
                null
            )
            vpnOutputStream.write(synAckPacket)
            
            // Start reading from the socket
            startTcpReader(connection, traffic, vpnOutputStream)
            
        } catch (e: Exception) {
            Log.e("TCP_FORWARD", "Error establishing TCP connection", e)
            cleanupTcpConnection(key)
        }
    }
    
    private fun startTcpReader(
        connection: TcpConnection,
        traffic: TrafficLogResponse,
        vpnOutputStream: FileOutputStream
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val buffer = ByteArray(4096)
                val inputStream = connection.socket.getInputStream()
                
                while (!connection.socket.isClosed) {
                    val bytesRead = inputStream.read(buffer)
                    if (bytesRead == -1) break
                    
                    val responseData = buffer.copyOf(bytesRead)
                    val responsePacket = NativeBridge.buildTcpResponsePacket(
                        responseData,
                        traffic.dstIp,
                        traffic.dstPort,
                        "10.0.0.2",
                        traffic.srcPort
                    )
                    
                    vpnOutputStream.write(responsePacket)
                    Log.d("TCP_FORWARD", "Forwarded ${bytesRead} bytes from ${traffic.dstIp}:${traffic.dstPort}")
                }
                
            } catch (e: Exception) {
                Log.e("TCP_FORWARD", "TCP reader error", e)
            } finally {
                cleanupTcpConnection(connection.key)
            }
        }
    }
    
    private fun cleanupTcpConnection(key: String) {
        tcpConnections.remove(key)?.let { connection ->
            try {
                connection.socket.close()
                Log.d("TCP_FORWARD", "Cleaned up TCP connection: $key")
            } catch (e: Exception) {
                Log.w("TCP_FORWARD", "Error closing TCP socket", e)
            }
        }
    }
    
    fun cleanup() {
        tcpConnections.values.forEach { connection ->
            try {
                connection.socket.close()
            } catch (e: Exception) {
                Log.w("TCP_FORWARD", "Error closing socket during cleanup", e)
            }
        }
        tcpConnections.clear()
    }
}