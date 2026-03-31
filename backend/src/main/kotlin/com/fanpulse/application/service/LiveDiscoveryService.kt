package com.fanpulse.application.service

/**
 * 라이브 스트리밍 자동 탐색 실행 결과
 *
 * @property total total channels processed
 * @property upserted successfully created or updated events
 * @property failed channels that failed during discovery
 * @property errors error messages for failed channels
 */
data class LiveDiscoveryResult(
    val total: Int,
    val upserted: Int,
    val failed: Int,
    val errors: List<String> = emptyList()
)

/**
 * 등록된 아티스트 채널에서 라이브 스트리밍을 자동 탐색한다.
 */
interface LiveDiscoveryService {

    /**
     * 모든 활성 아티스트 채널에서 라이브 스트림을 탐색한다.
     * 모든 활성 채널을 순회하며 라이브 스트리밍 이벤트를 upsert한다.
     */
    suspend fun discoverAllChannels(): LiveDiscoveryResult
}
