package com.fanpulse.infrastructure.config

import net.javacrumbs.shedlock.core.LockProvider
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import javax.sql.DataSource

/**
 * ShedLock 설정 - 분산 환경에서 스케줄러 동시 실행 방지
 *
 * W4 Fix: 스케줄러가 여러 인스턴스에서 중복 실행되는 것을 방지합니다.
 *
 * 사전 조건: shedlock 테이블이 필요합니다.
 * ```sql
 * CREATE TABLE shedlock (
 *     name VARCHAR(64) NOT NULL,
 *     lock_until TIMESTAMP NOT NULL,
 *     locked_at TIMESTAMP NOT NULL,
 *     locked_by VARCHAR(255) NOT NULL,
 *     PRIMARY KEY (name)
 * );
 * ```
 */
@Configuration
@EnableSchedulerLock(defaultLockAtMostFor = "50m")
class ShedLockConfig {

    @Bean
    fun lockProvider(dataSource: DataSource): LockProvider {
        return JdbcTemplateLockProvider(
            JdbcTemplateLockProvider.Configuration.builder()
                .withJdbcTemplate(JdbcTemplate(dataSource))
                .usingDbTime()
                .build()
        )
    }
}
