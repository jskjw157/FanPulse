package com.aos.fanpulse.domain.model

data class ArtistListResponse(
    val content: List<Artist>,      // 아티스트 객체 리스트
    val totalElements: Int,         // 전체 아이템 개수
    val page: Int,                  // 현재 페이지 번호
    val size: Int,                  // 한 페이지당 아이템 개수
    val totalPages: Int             // 전체 페이지 수
)

data class Artist(
    val id: String,                 // UUID 형식이므로 String으로 받습니다.
    val name: String,               // 아티스트 이름
    val englishName: String?,       // 영문 이름 (없을 수 있다면 ? 추가)
    val agency: String?,            // 소속사
    val profileImageUrl: String?,   // 프로필 이미지 URL
    val isGroup: Boolean            // 그룹 여부
)

data class ArtistDetail(
    val id: String,                 // UUID (String)
    val name: String,
    val englishName: String?,
    val agency: String?,
    val description: String?,       // 상세 설명 추가
    val profileImageUrl: String?,
    val isGroup: Boolean,
    val members: List<String>,      // 멤버 리스트 (String 리스트)
    val active: Boolean,            // 활성 상태 여부
    val debutDate: String?,         // "2026-04-01" 형태의 날짜
    val createdAt: String?          // "2026-04-01T05:08..." 형태의 ISO 8601 일시
){
    companion object {
        val EMPTY = ArtistDetail(
            id = "",
            name = "알 수 없는 아티스트",
            englishName = null,
            agency = null,
            description = null,
            profileImageUrl = null,
            isGroup = false,
            members = emptyList(), // 리스트는 null보다 빈 리스트가 안전합니다.
            active = false,
            debutDate = null,
            createdAt = null
        )
    }
}
