package com.example.utils

import android.content.Context
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class P2pEndpoint(val id: String, val name: String)

class P2pManager(private val context: Context) {
    private val connectionsClient = Nearby.getConnectionsClient(context)
    private val STRATEGY = Strategy.P2P_STAR
    private val SERVICE_ID = "com.example.untisneo.p2p"

    private val _discoveredEndpoints = MutableStateFlow<List<P2pEndpoint>>(emptyList())
    val discoveredEndpoints: StateFlow<List<P2pEndpoint>> = _discoveredEndpoints

    private val _connectedEndpoint = MutableStateFlow<P2pEndpoint?>(null)
    val connectedEndpoint: StateFlow<P2pEndpoint?> = _connectedEndpoint

    private val _incomingMessages = MutableStateFlow<String?>(null)
    val incomingMessages: StateFlow<String?> = _incomingMessages

    var currentName = ""
    var isAdvertising = false
    var isDiscovering = false

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            if (payload.type == Payload.Type.BYTES) {
                payload.asBytes()?.let {
                    _incomingMessages.value = String(it)
                }
            }
        }
        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {}
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            connectionsClient.acceptConnection(endpointId, payloadCallback)
            _connectedEndpoint.value = P2pEndpoint(endpointId, connectionInfo.endpointName)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                Log.d("P2P", "Connected to $endpointId")
            } else {
                if (_connectedEndpoint.value?.id == endpointId) {
                    _connectedEndpoint.value = null
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            if (_connectedEndpoint.value?.id == endpointId) {
                _connectedEndpoint.value = null
            }
        }
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            val list = _discoveredEndpoints.value.toMutableList()
            // Avoid duplicates
            if (list.none { it.id == endpointId }) {
                list.add(P2pEndpoint(endpointId, info.endpointName))
                _discoveredEndpoints.value = list
            }
        }

        override fun onEndpointLost(endpointId: String) {
            val list = _discoveredEndpoints.value.toMutableList()
            list.removeAll { it.id == endpointId }
            _discoveredEndpoints.value = list
        }
    }

    fun startAdvertising(name: String) {
        currentName = name
        val options = AdvertisingOptions.Builder().setStrategy(STRATEGY).build()
        connectionsClient.startAdvertising(
            name, SERVICE_ID, connectionLifecycleCallback, options
        ).addOnSuccessListener {
            isAdvertising = true
        }.addOnFailureListener {
            Log.e("P2P", "Advertising failed", it)
        }
    }

    fun startDiscovery(name: String) {
        currentName = name
        _discoveredEndpoints.value = emptyList()
        val options = DiscoveryOptions.Builder().setStrategy(STRATEGY).build()
        connectionsClient.startDiscovery(
            SERVICE_ID, endpointDiscoveryCallback, options
        ).addOnSuccessListener {
            isDiscovering = true
        }.addOnFailureListener {
            Log.e("P2P", "Discovery failed", it)
        }
    }

    fun requestConnection(endpointId: String) {
        connectionsClient.requestConnection(currentName, endpointId, connectionLifecycleCallback)
            .addOnFailureListener {
                Log.e("P2P", "Request connection failed", it)
            }
    }

    fun sendMessage(text: String) {
        val endpoint = _connectedEndpoint.value ?: return
        val payload = Payload.fromBytes(text.toByteArray())
        connectionsClient.sendPayload(endpoint.id, payload)
    }

    fun disconnect() {
        connectionsClient.stopAllEndpoints()
        _connectedEndpoint.value = null
    }

    fun stop() {
        connectionsClient.stopAdvertising()
        connectionsClient.stopDiscovery()
        isAdvertising = false
        isDiscovering = false
    }

    fun clearIncoming() {
        _incomingMessages.value = null
    }
}
