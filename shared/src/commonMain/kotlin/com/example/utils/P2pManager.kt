package com.example.utils

import kotlinx.coroutines.flow.StateFlow

data class P2pEndpoint(val id: String, val name: String)

expect class P2pManager() {
    val discoveredEndpoints: StateFlow<List<P2pEndpoint>>
    val connectedEndpoint: StateFlow<P2pEndpoint?>
    val receivedMessages: StateFlow<List<String>>
    val incomingMessages: StateFlow<String?>

    fun startAdvertising(name: String)
    fun startDiscovery(name: String = "")
    fun requestConnection(endpointId: String)
    fun connectTo(endpointId: String)
    fun disconnect()
    fun sendData(data: String)
    fun clearIncoming()
    fun stop()
}
