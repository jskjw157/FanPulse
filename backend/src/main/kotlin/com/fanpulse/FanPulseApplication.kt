package com.fanpulse

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * K-Pop 팬 커뮤니티 라이브 스트리밍 플랫폼.
 */
@SpringBootApplication
@EnableScheduling
class FanPulseApplication

fun main(args: Array<String>) {
    runApplication<FanPulseApplication>(*args)
}
