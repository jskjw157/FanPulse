package com.fanpulse.application.service

data class LiveDiscoveryResult(
    val total: Int,
    val upserted: Int,
    val failed: Int,
    val errors: List<String> = emptyList()
)

interface LiveDiscoveryService {
    suspend fun discoverAllChannels(): LiveDiscoveryResult
}
