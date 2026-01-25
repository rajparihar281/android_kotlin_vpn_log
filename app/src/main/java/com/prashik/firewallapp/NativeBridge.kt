package com.prashik.firewallapp

object NativeBridge {
    external fun parseRealPacket(packet: ByteArray, length: Int): String
    external fun extractUdpPayload(packetData: ByteArray, length: Int): ByteArray?
    external fun buildUdpResponsePacket(
        payload: ByteArray,
        srcIp: String,
        srcPort: Int,
        dstIp: String,
        dstPort: Int
    ): ByteArray
    external fun extractTcpPayload(packetData: ByteArray, length: Int): ByteArray?
    external fun buildTcpResponsePacket(
        payload: ByteArray,
        srcIp: String,
        srcPort: Int,
        dstIp: String,
        dstPort: Int
    ): ByteArray
    external fun getTcpFlags(packetData: ByteArray, length: Int): Int
    external fun buildTcpPacket(
        srcIp: String,
        dstIp: String,
        srcPort: Int,
        dstPort: Int,
        flags: Int,
        seqNum: Long,
        ackNum: Long,
        payload: ByteArray?
    ): ByteArray
}