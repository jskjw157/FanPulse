package com.fanpulse

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class FanPulseApplication

fun main(args: Array<String>) {
    runApplication<FanPulseApplication>(*args)
}
