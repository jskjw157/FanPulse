package com.fanpulse.domain.content

/**
 * 뉴스 기사의 제목과 본문에서 아티스트를 매칭하는 도메인 클래스.
 *
 * ## 매칭 정책
 *
 * ### 정규화
 * 모든 비교는 정규화된 문자열 기준으로 수행한다.
 * 정규화는 소문자 변환 후 공백(스페이스) 제거를 의미한다.
 * 예) "New Jeans" → "newjeans", "AESPA" → "aespa"
 *
 * ### 매칭 키
 * 각 아티스트에 대해 다음 키를 순서대로 검사한다:
 * 1. `name` (한글명)
 * 2. `englishName` (영문명, nullable)
 * 3. `members` (멤버명 목록, 각각 개별 검사)
 *
 * ### 단어 경계 처리
 * - **한글 키**: 정규화 후 단순 contains 매칭. 한글은 단어 경계 개념이 다르므로
 *   조사가 붙어도 (예: "에스파의") 올바르게 매칭된다.
 * - **영문 키**: 정규화 후 해당 키의 앞·뒤가 영문자/숫자가 아닌 경우에만 매칭.
 *   이를 통해 "aespa"가 "kespace" 안에 숨어 있을 때 오매칭을 방지한다.
 *   구체적으로: 정규화된 키와 정규화된 본문(소문자화, 공백 제거) 모두에서
 *   lookahead/lookbehind 기반 regex로 단어 경계를 검사한다.
 *
 * ### 비활성 아티스트
 * `active = false` 아티스트는 항상 결과에서 제외한다.
 */
class NewsMatcher {

    /**
     * 뉴스 제목과 본문에서 아티스트를 매칭하여 매칭된 아티스트 목록을 반환한다.
     *
     * @param title 뉴스 제목 (필수)
     * @param content 뉴스 본문 (선택, null 허용)
     * @param artists 매칭 대상 아티스트 목록
     * @return 매칭된 활성 아티스트 목록 (중복 없음, 입력 순서 유지)
     */
    fun match(title: String, content: String?, artists: List<Artist>): List<Artist> {
        val normalizedText = normalize(title + " " + (content ?: ""))

        return artists
            .filter { it.active }
            .filter { artist -> matchesArtist(artist, normalizedText) }
    }

    /**
     * 주어진 아티스트의 이름/영문명/멤버명 중 하나라도 본문과 매칭되는지 확인한다.
     */
    private fun matchesArtist(artist: Artist, normalizedText: String): Boolean {
        // 한글명 매칭 (정규화 후 단순 contains)
        if (containsKoreanKey(normalize(artist.name), normalizedText)) return true

        // 영문명 매칭 (정규화 후 단어 경계 기반)
        val englishName = artist.englishName
        if (englishName != null && containsEnglishKey(normalize(englishName), normalizedText)) return true

        // 멤버명 매칭
        return artist.members.any { member ->
            val normalizedMember = normalize(member)
            val isEnglishKey = !normalizedMember.any { c -> c.isKoreanCharacter() }
            if (isEnglishKey) {
                containsEnglishKey(normalizedMember, normalizedText)
            } else {
                containsKoreanKey(normalizedMember, normalizedText)
            }
        }
    }

    /**
     * 한글 키가 정규화된 본문에 포함되는지 확인한다.
     * 한글은 공백 제거 후 단순 contains로 매칭한다.
     */
    private fun containsKoreanKey(normalizedKey: String, normalizedText: String): Boolean =
        normalizedKey.isNotEmpty() && normalizedText.contains(normalizedKey)

    /**
     * 정규화된 영문 키가 정규화된 본문에서 단어 경계 기준으로 포함되는지 확인한다.
     * 정규화된 본문(소문자화, 공백 제거)에서 lookahead/lookbehind를 사용하여
     * 다른 영단어 안에 포함되는 오매칭을 방지한다.
     * 예) "aespa"는 "kespacetechnologynews" 안에서 매칭되지 않는다.
     * 예) "newjeans"는 "newjeans신보발매" 안에서 올바르게 매칭된다.
     */
    private fun containsEnglishKey(normalizedKey: String, normalizedText: String): Boolean {
        if (normalizedKey.isEmpty()) return false
        val escapedKey = Regex.escape(normalizedKey)
        val pattern = Regex("(?<![a-z0-9])$escapedKey(?![a-z0-9])")
        return pattern.containsMatchIn(normalizedText)
    }

    /**
     * 입력 문자열을 소문자로 변환한 후 모든 공백을 제거하여 정규화한다.
     */
    private fun normalize(input: String): String =
        input.lowercase().replace(" ", "")

    /**
     * 해당 문자가 한글 문자(가-힣, 자모 포함)인지 여부를 반환한다.
     */
    private fun Char.isKoreanCharacter(): Boolean =
        this in '\uAC00'..'\uD7A3' ||
            this in '\u1100'..'\u11FF' ||
            this in '\u3130'..'\u318F'
}
