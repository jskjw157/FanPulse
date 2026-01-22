package com.fanpulse.domain.identity.port

import com.fanpulse.domain.identity.User
import java.util.UUID

/**
 * User Repository Port (출력 포트)
 *
 * 사용자 저장소 인터페이스
 */
interface UserPort {
    /**
     * 사용자 저장
     */
    fun save(user: User): User

    /**
     * ID로 사용자 조회
     */
    fun findById(id: UUID): User?

    /**
     * 이메일로 사용자 조회
     */
    fun findByEmail(email: String): User?

    /**
     * 사용자명으로 사용자 조회
     */
    fun findByUsername(username: String): User?

    /**
     * 사용자명 존재 여부 확인
     */
    fun existsByUsername(username: String): Boolean

    /**
     * 이메일 존재 여부 확인
     */
    fun existsByEmail(email: String): Boolean

    /**
     * 주어진 prefix로 시작하는 사용자명 중 가장 큰 suffix 조회
     * N+1 쿼리 방지를 위한 최적화 메서드
     *
     * @param prefix 사용자명 prefix (예: "john_doe")
     * @return 가장 큰 suffix 번호 (없으면 0, "john_doe_5"가 있으면 5)
     */
    fun findMaxUsernameSuffix(prefix: String): Int
}
