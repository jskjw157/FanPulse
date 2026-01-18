package com.fanpulse.infrastructure.persistence.identity

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface UserJpaRepository : JpaRepository<UserEntity, UUID> {
    fun findByEmail(email: String): UserEntity?
    fun findByUsername(username: String): UserEntity?
    fun existsByUsername(username: String): Boolean
    fun existsByEmail(email: String): Boolean

    /**
     * 주어진 prefix로 시작하는 사용자명 중 가장 큰 suffix 조회
     *
     * 예: "john_doe_1", "john_doe_5", "john_doe_10" 존재 시 → 10 반환
     */
    @Query("""
        SELECT COALESCE(MAX(
            CAST(
                SUBSTRING(username, LENGTH(:prefix) + 2) AS int
            )
        ), 0)
        FROM users
        WHERE username LIKE CONCAT(:prefix, '_%')
        AND SUBSTRING(username, LENGTH(:prefix) + 2) ~ '^[0-9]+$'
    """, nativeQuery = true)
    fun findMaxUsernameSuffix(@Param("prefix") prefix: String): Int
}
