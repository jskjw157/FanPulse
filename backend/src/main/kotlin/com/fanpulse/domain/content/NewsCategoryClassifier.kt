package com.fanpulse.domain.content

/**
 * 뉴스 제목과 본문을 분석하여 [NewsCategory]를 분류하는 순수 도메인 오브젝트.
 *
 * ## 분류 전략
 * 1. title을 소문자로 변환 후 우선순위 순서로 키워드 매칭
 * 2. title에서 매칭되지 않으면 content를 소문자로 변환 후 동일 순서로 재시도
 * 3. 어느 쪽에서도 매칭되지 않으면 [NewsCategory.GENERAL] 반환
 *
 * ## 우선순위 (높을수록 먼저)
 * RELEASE > TOUR > AWARD > VARIETY > SOCIAL_MEDIA > COLLABORATION > GENERAL
 *
 * DB 의존이 없는 순수 Kotlin 로직이므로 외부 DI 없이 object로 선언.
 */
object NewsCategoryClassifier {

    /**
     * 카테고리별 키워드 목록 (우선순위 순서로 정렬됨).
     *
     * 각 Pair의 첫 번째 요소는 [NewsCategory], 두 번째 요소는 해당 카테고리를 나타내는 소문자 키워드 목록.
     * 리스트 순서가 곧 우선순위이므로 순서를 변경하지 않도록 주의.
     */
    private val KEYWORDS: List<Pair<NewsCategory, List<String>>> = listOf(
        NewsCategory.RELEASE to listOf(
            "발매", "release", "컴백", "comeback", "신곡", "앨범 공개", "음원"
        ),
        NewsCategory.TOUR to listOf(
            "콘서트", "concert", "tour", "투어", "공연", "단독 공연"
        ),
        NewsCategory.AWARD to listOf(
            "시상식", "award", "수상", "어워드", "대상", "본상"
        ),
        NewsCategory.VARIETY to listOf(
            "예능", "variety", "방송 출연", "tv 출연"
        ),
        NewsCategory.SOCIAL_MEDIA to listOf(
            "instagram", "twitter", "sns", "인스타", "트위터", "틱톡", "tiktok"
        ),
        NewsCategory.COLLABORATION to listOf(
            "collab", "콜라보", "협업", "피처링", "featuring", "feat."
        ),
    )

    /**
     * 뉴스 제목과 본문을 기반으로 [NewsCategory]를 분류하여 반환한다.
     *
     * @param title 뉴스 제목 (우선적으로 사용)
     * @param content 뉴스 본문 (title에서 매칭 실패 시 보조로 사용, null 허용)
     * @return 분류된 [NewsCategory]. 매칭 키워드가 없으면 [NewsCategory.GENERAL]
     */
    fun classify(title: String, content: String?): NewsCategory {
        val lowerTitle = title.lowercase()
        matchCategory(lowerTitle)?.let { return it }

        if (!content.isNullOrBlank()) {
            val lowerContent = content.lowercase()
            matchCategory(lowerContent)?.let { return it }
        }

        return NewsCategory.GENERAL
    }

    /**
     * 주어진 텍스트에서 우선순위 순서로 키워드를 탐색하여 첫 번째 매칭 [NewsCategory]를 반환한다.
     *
     * @param text 소문자로 변환된 검색 대상 텍스트
     * @return 매칭된 [NewsCategory], 없으면 null
     */
    private fun matchCategory(text: String): NewsCategory? {
        for ((category, keywords) in KEYWORDS) {
            if (keywords.any { text.contains(it) }) {
                return category
            }
        }
        return null
    }
}
