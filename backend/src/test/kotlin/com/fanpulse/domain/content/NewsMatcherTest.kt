package com.fanpulse.domain.content

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * NewsMatcher 도메인 클래스 단위 테스트
 */
@DisplayName("NewsMatcher")
class NewsMatcherTest {

    private val matcher = NewsMatcher()

    // 테스트용 아티스트 픽스처
    private val aespa = Artist.create(
        name = "에스파",
        englishName = "aespa",
        agency = "SM Entertainment",
        isGroup = true,
    ).also { it.addMember("카리나"); it.addMember("지젤"); it.addMember("윈터"); it.addMember("닝닝") }

    private val bts = Artist.create(
        name = "방탄소년단",
        englishName = "BTS",
        agency = "HYBE",
        isGroup = true,
    ).also { it.addMember("RM"); it.addMember("진") }

    private val newJeans = Artist.create(
        name = "뉴진스",
        englishName = "New Jeans",
        agency = "ADOR",
        isGroup = true,
    )

    private val inactiveArtist = Artist.create(
        name = "해체그룹",
        englishName = "Disbanded",
        agency = "Agency",
        isGroup = true,
    ).also { it.deactivate() }

    @Nested
    @DisplayName("기본 매칭")
    inner class BasicMatching {

        @Test
        @DisplayName("한글 아티스트명이 title에 포함되면 해당 아티스트를 반환한다")
        fun `한글 아티스트명이 title에 포함되면 해당 아티스트 반환`() {
            val result = matcher.match(
                title = "에스파 신곡 발매 예정",
                content = null,
                artists = listOf(aespa, bts),
            )

            assertEquals(1, result.size)
            assertEquals(aespa.id, result[0].id)
        }

        @Test
        @DisplayName("영문 아티스트명이 content에 포함되면 해당 아티스트를 반환한다")
        fun `영문 아티스트명이 content에 포함되면 해당 아티스트 반환`() {
            val result = matcher.match(
                title = "K-pop 최신 소식",
                content = "aespa가 새 앨범을 준비 중입니다.",
                artists = listOf(aespa, bts),
            )

            assertEquals(1, result.size)
            assertEquals(aespa.id, result[0].id)
        }

        @Test
        @DisplayName("멤버명이 title에 포함되면 해당 아티스트를 반환한다")
        fun `멤버명이 title에 포함되면 아티스트 반환`() {
            val result = matcher.match(
                title = "카리나 단독 화보 공개",
                content = null,
                artists = listOf(aespa, bts),
            )

            assertEquals(1, result.size)
            assertEquals(aespa.id, result[0].id)
        }

        @Test
        @DisplayName("복수 아티스트가 매칭되면 모두 반환한다")
        fun `복수 아티스트 매칭 시 모두 반환`() {
            val result = matcher.match(
                title = "에스파 방탄소년단 콜라보 확정",
                content = null,
                artists = listOf(aespa, bts, newJeans),
            )

            assertEquals(2, result.size)
            assertTrue(result.any { it.id == aespa.id })
            assertTrue(result.any { it.id == bts.id })
        }

        @Test
        @DisplayName("어떤 아티스트도 매칭되지 않으면 빈 리스트를 반환한다")
        fun `어떤 아티스트도 매칭 안 되면 빈 리스트 반환`() {
            val result = matcher.match(
                title = "K-pop 최신 뉴스",
                content = "올해 음악 시장 현황 분석",
                artists = listOf(aespa, bts),
            )

            assertTrue(result.isEmpty())
        }
    }

    @Nested
    @DisplayName("정규화 매칭")
    inner class NormalizedMatching {

        @Test
        @DisplayName("대소문자를 무시하고 매칭한다 (aespa vs AESPA vs Aespa)")
        fun `대소문자 무시 매칭 (aespa vs AESPA vs aespa)`() {
            val resultUpper = matcher.match(
                title = "AESPA 컴백 확정",
                content = null,
                artists = listOf(aespa),
            )
            val resultMixed = matcher.match(
                title = "Aespa 컴백 확정",
                content = null,
                artists = listOf(aespa),
            )

            assertEquals(1, resultUpper.size)
            assertEquals(aespa.id, resultUpper[0].id)
            assertEquals(1, resultMixed.size)
            assertEquals(aespa.id, resultMixed[0].id)
        }

        @Test
        @DisplayName("띄어쓰기 차이를 무시하고 매칭한다 (New Jeans vs NewJeans vs newjeans)")
        fun `띄어쓰기 차이 무시 (New Jeans vs NewJeans vs newjeans)`() {
            val resultNoSpace = matcher.match(
                title = "NewJeans 신보 발매",
                content = null,
                artists = listOf(newJeans),
            )
            val resultLower = matcher.match(
                title = "newjeans 신보 발매",
                content = null,
                artists = listOf(newJeans),
            )

            assertEquals(1, resultNoSpace.size)
            assertEquals(newJeans.id, resultNoSpace[0].id)
            assertEquals(1, resultLower.size)
            assertEquals(newJeans.id, resultLower[0].id)
        }
    }

    @Nested
    @DisplayName("단어 경계 매칭")
    inner class WordBoundaryMatching {

        @Test
        @DisplayName("한글 조사가 붙은 경우에도 매칭한다 (에스파의, aespa의)")
        fun `부분 문자열이 단어 경계에 걸쳐도 한글 조사 포함 매칭`() {
            val result = matcher.match(
                title = "aespa의 새 앨범 소식",
                content = null,
                artists = listOf(aespa),
            )

            assertEquals(1, result.size)
            assertEquals(aespa.id, result[0].id)
        }

        @Test
        @DisplayName("영문 아티스트명이 다른 영단어 일부로 포함된 경우 매칭하지 않는다 (kespace 안의 aespa)")
        fun `영문 아티스트명이 다른 영단어 안에 포함되면 매칭 안 됨 (kespace)`() {
            val result = matcher.match(
                title = "kespace technology news",
                content = null,
                artists = listOf(aespa),
            )

            assertTrue(result.isEmpty())
        }
    }

    @Nested
    @DisplayName("비활성 아티스트 필터링")
    inner class InactiveArtistFiltering {

        @Test
        @DisplayName("비활성(active=false) 아티스트는 매칭 결과에서 제외한다")
        fun `비활성(active=false) 아티스트는 매칭에서 제외`() {
            val result = matcher.match(
                title = "해체그룹 Disbanded 관련 소식",
                content = null,
                artists = listOf(aespa, inactiveArtist),
            )

            assertTrue(result.isEmpty())
        }
    }
}
