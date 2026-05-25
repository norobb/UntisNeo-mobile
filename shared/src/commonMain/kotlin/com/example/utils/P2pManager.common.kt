package com.example.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class P2pEndpoint(val id: String, val name: String)

// KMP Stub - P2P (Nearby Connections) is Android-specific
// On iOS, this is a no-op stub
class P2pManager {
    private val _discoveredEndpoints = MutableStateFlow<List<P2pEndpoint>>(emptyList())
    val discoveredEndpoints: StateFlow<List<P2pEndpoint>> = _discoveredEndpoints

    private val _connectedEndpoint = MutableStateFlow<P2pEndpoint?>(null)
    val connectedEndpoint: StateFlow<P2pEndpoint?> = _connectedEndpoint

    private val _receivedMessages = MutableStateFlow<List<String>>(emptyList())
    val receivedMessages: StateFlow<List<String>> = _receivedMessages

    private val _incomingMessages = MutableStateFlow<String?>(null)
    val incomingMessages: StateFlow<String?> = _incomingMessages

    fun startAdvertising(name: String) {
        println("[P2P] startAdvertising: $name (not available on this platform)")
    }

    fun startDiscovery(name: String = "") {
        println("[P2P] startDiscovery: $name (not available on this platform)")
    }

    fun requestConnection(endpointId: String) {
        println("[P2P] requestConnection: $endpointId (not available on this platform)")
    }

    fun connectTo(endpointId: String) {
        println("[P2P] connectTo: $endpointId (not available on this platform)")
    }

    fun disconnect() {
        println("[P2P] disconnect (not available on this platform)")
    }

    fun sendData(data: String) {
        println("[P2P] sendData: $data (not available on this platform)")
    }

    fun clearIncoming() {
        _incomingMessages.value = null
    }

    fun stop() {
        println("[P2P] stop (not available on this platform)")
    }
}
