package com.fanpulse.domain.content

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("NewsCategoryClassifier")
class NewsCategoryClassifierTest {

    @Nested
    @DisplayName("RELEASE 분류")
    inner class ReleaseClassification {

        @Test
        @DisplayName("title에 '발매' 포함 시 RELEASE를 반환해야 한다")
        fun `title에 발매 포함 시 RELEASE 반환`() {
            val result = NewsCategoryClassifier.classify(
                title = "BTS 새 앨범 발매 소식",
                content = null
            )
            assertEquals(NewsCategory.RELEASE, result)
        }

        @Test
        @DisplayName("title에 'release' 포함 시 RELEASE를 반환해야 한다")
        fun `title에 release 포함 시 RELEASE 반환`() {
            val result = NewsCategoryClassifier.classify(
                title = "aespa new release announced",
                content = null
            )
            assertEquals(NewsCategory.RELEASE, result)
        }
    }

    @Nested
    @DisplayName("TOUR 분류")
    inner class TourClassification {

        @Test
        @DisplayName("title에 '콘서트' 포함 시 TOUR를 반환해야 한다")
        fun `title에 콘서트 포함 시 TOUR 반환`() {
            val result = NewsCategoryClassifier.classify(
                title = "BTS 월드 콘서트 개최 확정",
                content = null
            )
            assertEquals(NewsCategory.TOUR, result)
        }

        @Test
        @DisplayName("title에 'tour' 포함 시 TOUR를 반환해야 한다")
        fun `title에 tour 포함 시 TOUR 반환`() {
            val result = NewsCategoryClassifier.classify(
                title = "BLACKPINK world tour dates confirmed",
                content = null
            )
            assertEquals(NewsCategory.TOUR, result)
        }
    }

    @Nested
    @DisplayName("AWARD 분류")
    inner class AwardClassification {

        @Test
        @DisplayName("title에 '시상식' 포함 시 AWARD를 반환해야 한다")
        fun `title에 시상식 포함 시 AWARD 반환`() {
            val result = NewsCategoryClassifier.classify(
                title = "멜론 뮤직 시상식 후보 발표",
                content = null
            )
            assertEquals(NewsCategory.AWARD, result)
        }

        @Test
        @DisplayName("title에 'award' 포함 시 AWARD를 반환해야 한다")
        fun `title에 award 포함 시 AWARD 반환`() {
            val result = NewsCategoryClassifier.classify(
                title = "MAMA award nominees announced",
                content = null
            )
            assertEquals(NewsCategory.AWARD, result)
        }
    }

    @Nested
    @DisplayName("VARIETY 분류")
    inner class VarietyClassification {

        @Test
        @DisplayName("title에 '예능' 포함 시 VARIETY를 반환해야 한다")
        fun `title에 예능 포함 시 VARIETY 반환`() {
            val result = NewsCategoryClassifier.classify(
                title = "IVE 예능 출연 확정",
                content = null
            )
            assertEquals(NewsCategory.VARIETY, result)
        }
    }

    @Nested
    @DisplayName("SOCIAL_MEDIA 분류")
    inner class SocialMediaClassification {

        @Test
        @DisplayName("title에 'instagram' 포함 시 SOCIAL_MEDIA를 반환해야 한다")
        fun `title에 instagram 포함 시 SOCIAL_MEDIA 반환`() {
            val result = NewsCategoryClassifier.classify(
                title = "Jennie instagram update 사진 공개",
                content = null
            )
            assertEquals(NewsCategory.SOCIAL_MEDIA, result)
        }
    }

    @Nested
    @DisplayName("COLLABORATION 분류")
    inner class CollaborationClassification {

        @Test
        @DisplayName("title에 'collab' 포함 시 COLLABORATION을 반환해야 한다")
        fun `title에 collab 포함 시 COLLABORATION 반환`() {
            val result = NewsCategoryClassifier.classify(
                title = "BTS and Ed Sheeran collab teaser",
                content = null
            )
            assertEquals(NewsCategory.COLLABORATION, result)
        }
    }

    @Nested
    @DisplayName("GENERAL 분류")
    inner class GeneralClassification {

        @Test
        @DisplayName("매칭 키워드 없으면 GENERAL을 반환해야 한다")
        fun `매칭 키워드 없으면 GENERAL 반환`() {
            val result = NewsCategoryClassifier.classify(
                title = "K-pop 최신 동향 정리",
                content = null
            )
            assertEquals(NewsCategory.GENERAL, result)
        }
    }

    @Nested
    @DisplayName("우선순위 처리")
    inner class PriorityHandling {

        @Test
        @DisplayName("복수 키워드 충돌 시 우선순위 순서(RELEASE > TOUR > AWARD > VARIETY > SOCIAL_MEDIA > COLLABORATION > GENERAL)를 따라야 한다")
        fun `복수 키워드 충돌 시 RELEASE가 TOUR보다 우선`() {
            // title에 RELEASE 키워드와 TOUR 키워드가 함께 있을 때 RELEASE가 우선
            val result = NewsCategoryClassifier.classify(
                title = "발매 기념 콘서트 일정 공개",
                content = null
            )
            assertEquals(NewsCategory.RELEASE, result)
        }
    }

    @Nested
    @DisplayName("title 우선, content 보조")
    inner class TitlePriorityOverContent {

        @Test
        @DisplayName("title에 매칭 없으면 content에서 키워드를 찾아야 한다")
        fun `title 매칭 없을 때 content에서 RELEASE 키워드 감지`() {
            val result = NewsCategoryClassifier.classify(
                title = "오늘의 K-pop 소식",
                content = "새 앨범 발매 일정이 공개되었습니다."
            )
            assertEquals(NewsCategory.RELEASE, result)
        }
    }
}
