package com.aos.fanpulse.presentation.common

import com.aos.fanpulse.data.remote.apiservice.Artist
import com.aos.fanpulse.data.remote.apiservice.ArtistDetail
import com.aos.fanpulse.data.remote.apiservice.NewsDetail
import com.aos.fanpulse.data.remote.apiservice.NewsItem
import com.aos.fanpulse.data.remote.apiservice.NewsListResponse
import com.aos.fanpulse.data.remote.apiservice.StreamingEventDetail
import com.aos.fanpulse.data.remote.apiservice.StreamingEventItem
import com.aos.fanpulse.data.remote.apiservice.StreamingEventSimpleItem

object DummyData {
    val streamingEventDummyList = listOf(
        // 1. 현재 방송 중인 이벤트 (LIVE)
        StreamingEventItem(
            id = "event_001",
            title = "2026 월드투어 서울 콘서트 라이브",
            artistId = "artist_01",
            artistName = "NewJeans",
            thumbnailUrl = "https://dummyimage.com/600x400/000/fff&text=NewJeans+Live",
            status = "LIVE",
            scheduledAt = "2026-04-14T18:00:00+09:00",
            startedAt = "2026-04-14T18:05:30+09:00",
            viewerCount = 125400
        ),

        // 2. 방송 예정인 이벤트 (UPCOMING)
        StreamingEventItem(
            id = "event_002",
            title = "미니 4집 발매 기념 쇼케이스",
            artistId = "artist_02",
            artistName = "aespa",
            thumbnailUrl = "https://dummyimage.com/600x400/000/fff&text=aespa+Showcase",
            status = "UPCOMING",
            scheduledAt = "2026-04-15T20:00:00+09:00",
            startedAt = null, // 아직 시작되지 않았으므로 null
            viewerCount = 0
        ),

        // 3. 종료된 이벤트 (ENDED)
        StreamingEventItem(
            id = "event_003",
            title = "컴백 카운트다운 라이브",
            artistId = "artist_03",
            artistName = "IVE",
            thumbnailUrl = "https://dummyimage.com/600x400/000/fff&text=IVE+Countdown",
            status = "ENDED",
            scheduledAt = "2026-04-10T17:00:00+09:00",
            startedAt = "2026-04-10T17:01:15+09:00",
            viewerCount = 85000
        ),

        // 4. 게릴라 라이브 (LIVE)
        StreamingEventItem(
            id = "event_004",
            title = "연습실 깜짝 라이브 🎤",
            artistId = "artist_04",
            artistName = "RIIZE",
            thumbnailUrl = "https://dummyimage.com/600x400/000/fff&text=RIIZE+Practice",
            status = "LIVE",
            scheduledAt = "2026-04-14T22:00:00+09:00",
            startedAt = "2026-04-14T22:02:00+09:00",
            viewerCount = 34200
        ),

        // 5. 팬미팅 방송 예정 (UPCOMING)
        StreamingEventItem(
            id = "event_005",
            title = "데뷔 3주년 기념 랜선 팬미팅",
            artistId = "artist_05",
            artistName = "ZEROBASEONE",
            thumbnailUrl = "https://dummyimage.com/600x400/000/fff&text=ZB1+Fanmeeting",
            status = "UPCOMING",
            scheduledAt = "2026-04-20T19:00:00+09:00",
            startedAt = null,
            viewerCount = 0
        )
    )

    val streamingEventSimpleDummyList = listOf(
        // 1. 현재 방송 중인 메인 이벤트 (LIVE)
        StreamingEventSimpleItem(
            id = "simple_evt_001",
            title = "LE SSERAFIM 미니 5집 컴백 카운트다운 라이브",
            thumbnailUrl = "https://dummyimage.com/600x400/000/fff&text=LE+SSERAFIM+Live",
            artistId = "artist_lsrfm",
            scheduledAt = "2026-04-14T18:00:00+09:00",
            status = "LIVE",
            viewerCount = 450000,
            platform = "Weverse"
        ),

        // 2. 방송 예정인 대형 이벤트 (UPCOMING)
        StreamingEventSimpleItem(
            id = "simple_evt_002",
            title = "SEVENTEEN 11주년 기념 온라인 팬미팅",
            thumbnailUrl = "https://dummyimage.com/600x400/000/fff&text=SEVENTEEN+11th",
            artistId = "artist_svt",
            scheduledAt = "2026-05-26T19:00:00+09:00",
            status = "UPCOMING",
            viewerCount = 0, // 아직 시작 전
            platform = "Weverse"
        ),

        // 3. 종료된 VOD 제공용 데이터 (ENDED)
        StreamingEventSimpleItem(
            id = "simple_evt_003",
            title = "aespa 2026 World Tour in Seoul (Replay)",
            thumbnailUrl = "https://dummyimage.com/600x400/000/fff&text=aespa+World+Tour",
            artistId = "artist_aespa",
            scheduledAt = "2026-04-10T18:00:00+09:00",
            status = "ENDED",
            viewerCount = 1250000,
            platform = "BeyondLive"
        ),

        // 4. 타 플랫폼 라이브 방송 (LIVE)
        StreamingEventSimpleItem(
            id = "simple_evt_004",
            title = "T1 Faker 롤챔스 리뷰 및 소통 방송",
            thumbnailUrl = "https://dummyimage.com/600x400/000/fff&text=T1+Faker+Live",
            artistId = "artist_faker",
            scheduledAt = "2026-04-14T21:00:00+09:00",
            status = "LIVE",
            viewerCount = 85000,
            platform = "Chzzk"
        ),

        // 5. 유튜브 라이브 예정 (UPCOMING)
        StreamingEventSimpleItem(
            id = "simple_evt_005",
            title = "QWER 신곡 발매 기념 게릴라 콘서트",
            thumbnailUrl = "https://dummyimage.com/600x400/000/fff&text=QWER+Guerrilla",
            artistId = "artist_qwer",
            scheduledAt = "2026-04-15T20:00:00+09:00",
            status = "UPCOMING",
            viewerCount = 0,
            platform = "YouTube"
        )
    )

    val newsItemDummyList = listOf(
        // 앨범 발매(RELEASE) 관련 일반 기사
        NewsItem(
            id = "550e8400-e29b-41d4-a716-446655440001",
            artistId = "artist_nj",
            title = "뉴진스(NewJeans), 컴백 타이틀곡 뮤직비디오 티저 첫 선",
            thumbnailUrl = "https://dummyimage.com/400x300/000/fff&text=NewJeans+MV",
            sourceName = "스타뉴스",
            category = "RELEASE",
            publishedAt = "2026-04-14T09:00:00+09:00"
        ),

        // 투어(TOUR) 카테고리, 썸네일이 없는 케이스
        NewsItem(
            id = "550e8400-e29b-41d4-a716-446655440002",
            artistId = "artist_aespa",
            title = "에스파, 2026 월드투어 북미 일정 전석 매진 기염",
            thumbnailUrl = null,
            sourceName = "OSEN",
            category = "TOUR",
            publishedAt = "2026-04-13T17:30:00+09:00"
        ),

        // 공식 SNS 공지(ANNOUNCEMENT), 출처가 트위터인 케이스
        NewsItem(
            id = "550e8400-e29b-41d4-a716-446655440003",
            artistId = "artist_riize",
            title = "[공지] RIIZE 미니 3집 발매 기념 팬사인회 안내",
            thumbnailUrl = "https://dummyimage.com/400x300/000/fff&text=RIIZE+Notice",
            sourceName = "Official Twitter",
            category = "ANNOUNCEMENT",
            publishedAt = "2026-04-13T12:00:00+09:00"
        ),

        // 방송 출연(TV), 출처가 방송국인 케이스
        NewsItem(
            id = "550e8400-e29b-41d4-a716-446655440004",
            artistId = "artist_ive",
            title = "아이브, 이번 주 '엠카운트다운' 출격... 화려한 컴백 무대 예고",
            thumbnailUrl = "https://dummyimage.com/400x300/000/fff&text=IVE+Mcountdown",
            sourceName = "Mnet",
            category = "TV",
            publishedAt = "2026-04-12T15:15:00+09:00"
        ),

        // 수상(AWARD) 소식
        NewsItem(
            id = "550e8400-e29b-41d4-a716-446655440005",
            artistId = "artist_svt",
            title = "세븐틴, '올해의 아티스트상' 3년 연속 수상 달성",
            thumbnailUrl = "https://dummyimage.com/400x300/000/fff&text=SVT+Award",
            sourceName = "디스패치",
            category = "AWARD",
            publishedAt = "2026-04-11T21:40:00+09:00"
        )
    )

    // 2. 최종 API 응답(Pagination) 더미 데이터
    val newsListResponseDummy = NewsListResponse(
        content = newsItemDummyList, // 위에서 정의한 5개의 뉴스 아이템
        totalElements = 142,         // 전체 뉴스 개수 (가정)
        page = 0,                    // 현재 페이지 번호 (0부터 시작)
        size = 5,                    // 페이지당 요청/반환 사이즈
        totalPages = 29              // 전체 페이지 수 (142 / 5 = 28.4 -> 29페이지)
    )

    val newsDetailDummyList = listOf(
        // 1. 일반적인 기사 (HTML 형식 본문, 모든 필드 존재)
        NewsDetail(
            id = "7b2e38c5-9f1d-4a6c-b8e7-1234567890ab",
            artistId = "artist_nj",
            title = "뉴진스(NewJeans), 빌보드 '핫 100' 5주 연속 진입... 글로벌 인기 입증",
            content = """
            <p>그룹 <b>뉴진스(NewJeans)</b>가 미국 빌보드 메인 송차트 '핫 100'에서 굳건한 인기를 과시하고 있다.</p>
            <p>14일(현지시간) 발표된 최신 차트에 따르면...</p>
            <br>
            <p>자세한 내용은 공식 홈페이지에서 확인할 수 있습니다.</p>
        """.trimIndent(),
            sourceUrl = "https://example.com/news/1001",
            sourceName = "스타뉴스",
            thumbnailUrl = "https://dummyimage.com/800x600/000/fff&text=NewJeans+Billboard",
            category = "MUSIC",
            viewCount = 15420,
            publishedAt = "2026-04-14T09:00:00+09:00",
            createdAt = "2026-04-14T09:10:00+09:00"
        ),

        // 2. 공식 발표/공지사항 (마크다운 형식 본문, sourceUrl 없음)
        NewsDetail(
            id = "c3f192b8-5d4e-48a1-9c7f-abcdef123456",
            artistId = "artist_riize",
            title = "라이즈(RIIZE), 5월 첫 정규 앨범 전격 컴백 안내",
            content = """
            ## RIIZE 1st Full Album [BRIIZE] 발매 안내
            
            안녕하세요. SM엔터테인먼트입니다.
            **라이즈(RIIZE)** 가 오는 5월 12일 첫 정규 앨범으로 돌아옵니다.
            
            * 예약 판매: 2026년 4월 15일 (수) 15:00 (KST) ~
            * 발매 일시: 2026년 5월 12일 (화) 18:00 (KST)
            
            팬 여러분의 많은 관심 부탁드립니다.
        """.trimIndent(),
            sourceUrl = null, // 공식 앱/웹 자체 공지이므로 외부 링크 없음
            sourceName = "SM Entertainment",
            thumbnailUrl = "https://dummyimage.com/800x600/000/fff&text=RIIZE+Comeback",
            category = "ANNOUNCEMENT",
            viewCount = 89000,
            publishedAt = "2026-04-13T18:00:00+09:00",
            createdAt = "2026-04-13T18:05:00+09:00"
        ),

        // 3. 독점 인터뷰 (썸네일이 없는 텍스트 위주 기사)
        NewsDetail(
            id = "f8a91b2c-3d4e-5f6a-7b8c-9d0e1f2a3b4c",
            artistId = "artist_tws",
            title = "[독점] 투어스(TWS), 데뷔 첫 단독 팬미팅 성료 '눈물의 소감'",
            content = """
            <p>투어스(TWS)가 팬들과 잊지 못할 첫 번째 추억을 나눴다.</p>
            <p>멤버들은 앙코르 무대 후 "앞으로도 여러분의 자랑이 되겠다"며 눈물을 보였다...</p>
        """.trimIndent(),
            sourceUrl = "https://example.com/news/1002",
            sourceName = "디스패치",
            thumbnailUrl = null, // 썸네일 이미지가 제공되지 않은 케이스
            category = "INTERVIEW",
            viewCount = 32500,
            publishedAt = "2026-04-12T10:30:00+09:00",
            createdAt = "2026-04-12T11:00:00+09:00"
        )
    )

    val artistDetailDummyList = listOf(
        // 1. 정상적인 그룹 아티스트 (모든 데이터 포함)
        ArtistDetail(
            id = "550e8400-e29b-41d4-a716-446655440000",
            name = "에스파",
            englishName = "aespa",
            agency = "SM Entertainment",
            description = "현실 세계와 가상 세계를 넘나드는 메타버스 세계관을 가진 다국적 걸그룹입니다.",
            profileImageUrl = "https://dummyimage.com/600x600/000/fff&text=aespa+Profile",
            isGroup = true,
            members = listOf("카리나", "지젤", "윈터", "닝닝"),
            active = true,
            debutDate = "2020-11-17",
            createdAt = "2026-04-14T09:00:00+09:00"
        ),

        // 2. 솔로 아티스트 (멤버 리스트가 비어있음)
        ArtistDetail(
            id = "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
            name = "아이유",
            englishName = "IU",
            agency = "EDAM Entertainment",
            description = "대한민국의 대표적인 여성 솔로 싱어송라이터이자 배우.",
            profileImageUrl = "https://dummyimage.com/600x600/000/fff&text=IU+Profile",
            isGroup = false,
            members = emptyList(), // 솔로이므로 빈 리스트
            active = true,
            debutDate = "2008-09-18",
            createdAt = "2026-04-14T09:15:30+09:00"
        ),

        // 3. 신인 그룹 (아직 설명이나 프로필 이미지가 없는 경우)
        ArtistDetail(
            id = "123e4567-e89b-12d3-a456-426614174000",
            name = "아일릿",
            englishName = "ILLIT",
            agency = "BELIFT LAB",
            description = null, // 아직 상세 설명이 업데이트되지 않음
            profileImageUrl = null, // 프로필 이미지 미등록 상태
            isGroup = true,
            members = listOf("윤아", "민주", "모카", "원희", "이로하"),
            active = true,
            debutDate = "2024-03-25",
            createdAt = "2026-04-14T09:30:00+09:00"
        ),

        // 4. 활동을 종료한(비활성) 인디/무명 밴드 (Null 값이 많은 케이스)
        ArtistDetail(
            id = "8f14c2b9-2a3b-4c5d-6e7f-8a9b0c1d2e3f",
            name = "이름 없는 밴드",
            englishName = null, // 영문명 없음
            agency = null, // 소속사 없음
            description = "2010년대 홍대 인근에서 활동했던 인디 밴드.",
            profileImageUrl = null,
            isGroup = true,
            members = listOf("기타리스트", "보컬", "드러머"),
            active = false, // 활동 종료 상태
            debutDate = null, // 데뷔일 불분명
            createdAt = null // 생성일 미상
        )
    )
    val streamingEventDetailDummyList = listOf(
        // 1. 현재 방송 중 (LIVE) - 스트림 URL과 시작 시간이 존재함
        StreamingEventDetail(
            id = "evt_detail_001",
            title = "LE SSERAFIM 컴백 카운트다운 라이브 🎉",
            description = "미니 5집 발매를 앞두고 피어나(FEARNOT)와 함께하는 특별한 시간! 스포일러가 가득한 라이브 방송에 참여하세요.",
            artistId = "artist_lsrfm",
            artistName = "LE SSERAFIM",
            thumbnailUrl = "https://dummyimage.com/800x450/000/fff&text=LE+SSERAFIM+Live",
            streamUrl = "https://www.example.com/stream/live/lsrfm_001.m3u8", // 실제 플레이어에 넣을 HLS URL 예시
            status = "LIVE",
            scheduledAt = "2026-04-14T18:00:00+09:00",
            startedAt = "2026-04-14T18:03:15+09:00",
            endedAt = null,
            viewerCount = 350200,
            createdAt = "2026-04-10T10:00:00+09:00"
        ),

        // 2. 방송 예정 (UPCOMING) - 아직 시작 전이라 URL, 시작/종료 시간 없음
        StreamingEventDetail(
            id = "evt_detail_002",
            title = "RIIZE 데뷔 3주년 랜선 파티",
            description = null, // 아직 상세 설명이 등록되지 않은 상태
            artistId = "artist_riize",
            artistName = "RIIZE",
            thumbnailUrl = "https://dummyimage.com/800x450/000/fff&text=RIIZE+3rd+Anniversary",
            streamUrl = null, // 방송 시작 전이므로 스트리밍 링크 없음
            status = "UPCOMING",
            scheduledAt = "2026-05-01T20:00:00+09:00",
            startedAt = null,
            endedAt = null,
            viewerCount = 0,
            createdAt = "2026-04-12T14:30:00+09:00"
        ),

        // 3. 종료된 방송 (ENDED) - VOD를 위한 스트림 URL과 종료 시간이 존재함
        StreamingEventDetail(
            id = "evt_detail_003",
            title = "aespa 2026 World Tour in Seoul (Replay)",
            description = "뜨거웠던 서울 콘서트의 열기를 다시 한번 느껴보세요. (VOD 다시보기 제공 중)",
            artistId = "artist_aespa",
            artistName = "aespa",
            thumbnailUrl = "https://dummyimage.com/800x450/000/fff&text=aespa+Concert+VOD",
            streamUrl = "https://www.example.com/stream/vod/aespa_seoul_tour.mp4",
            status = "ENDED",
            scheduledAt = "2026-03-15T18:00:00+09:00",
            startedAt = "2026-03-15T18:10:00+09:00",
            endedAt = "2026-03-15T21:30:00+09:00",
            viewerCount = 1250000, // 누적 시청자 수
            createdAt = "2026-02-01T09:00:00+09:00"
        )
    )

    val artistDummyList = listOf(
        // 1. 모든 데이터가 꽉 차 있는 그룹 아티스트
        Artist(
            id = "550e8400-e29b-41d4-a716-446655440001",
            name = "뉴진스",
            englishName = "NewJeans",
            agency = "ADOR",
            profileImageUrl = "https://dummyimage.com/400x400/000/fff&text=NewJeans",
            isGroup = true
        ),

        // 2. 모든 데이터가 꽉 차 있는 솔로 아티스트
        Artist(
            id = "550e8400-e29b-41d4-a716-446655440002",
            name = "아이유",
            englishName = "IU",
            agency = "EDAM Entertainment",
            profileImageUrl = "https://dummyimage.com/400x400/000/fff&text=IU",
            isGroup = false
        ),

        // 3. 프로필 이미지가 없는(null) 신인/인디 그룹
        Artist(
            id = "550e8400-e29b-41d4-a716-446655440003",
            name = "이름 없는 밴드",
            englishName = "Nameless Band",
            agency = "Indie Label",
            profileImageUrl = null, // UI에서 placeholder(기본 이미지)가 잘 뜨는지 테스트용
            isGroup = true
        ),

        // 4. 영문 이름과 소속사가 없는(null) 중견 솔로 아티스트
        Artist(
            id = "550e8400-e29b-41d4-a716-446655440004",
            name = "나훈아",
            englishName = null, // 영문명이 없는 케이스 테스트
            agency = null,      // 소속사가 없는 케이스 테스트
            profileImageUrl = "https://dummyimage.com/400x400/000/fff&text=Trot+King",
            isGroup = false
        ),

        // 5. 또 다른 메이저 그룹
        Artist(
            id = "550e8400-e29b-41d4-a716-446655440005",
            name = "에스파",
            englishName = "aespa",
            agency = "SM Entertainment",
            profileImageUrl = "https://dummyimage.com/400x400/000/fff&text=aespa",
            isGroup = true
        )
    )
}