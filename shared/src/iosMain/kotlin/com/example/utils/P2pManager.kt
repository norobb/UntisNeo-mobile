package com.example.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

actual class P2pManager actual constructor() {
    private val _discoveredEndpoints = MutableStateFlow<List<P2pEndpoint>>(emptyList())
    actual val discoveredEndpoints: StateFlow<List<P2pEndpoint>> = _discoveredEndpoints

    private val _connectedEndpoint = MutableStateFlow<P2pEndpoint?>(null)
    actual val connectedEndpoint: StateFlow<P2pEndpoint?> = _connectedEndpoint

    private val _receivedMessages = MutableStateFlow<List<String>>(emptyList())
    actual val receivedMessages: StateFlow<List<String>> = _receivedMessages

    private val _incomingMessages = MutableStateFlow<String?>(null)
    actual val incomingMessages: StateFlow<String?> = _incomingMessages

    actual fun startAdvertising(name: String) {
        println("[iOS P2P] startAdvertising: $name")
    }

    actual fun startDiscovery(name: String) {
        println("[iOS P2P] startDiscovery")
        // Simulate finding a peer
        _discoveredEndpoints.value = listOf(P2pEndpoint("ios_peer_1", "iOS Test Peer (Meshtastic)"))
    }

    actual fun requestConnection(endpointId: String) {
        println("[iOS P2P] requestConnection: $endpointId")
        _connectedEndpoint.value = _discoveredEndpoints.value.find { it.id == endpointId }
    }

    actual fun connectTo(endpointId: String) {
        requestConnection(endpointId)
    }

    actual fun disconnect() {
        _connectedEndpoint.value = null
    }

    actual fun sendData(data: String) {
        println("[iOS P2P] sendData: $data")
    }

    actual fun clearIncoming() {
        _incomingMessages.value = null
    }

    actual fun stop() {
        disconnect()
        _discoveredEndpoints.value = emptyList()
    }
}
