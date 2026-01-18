package com.fanpulse.domain.content

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.util.*

/**
 * Artist Aggregate TDD Tests
 */
@DisplayName("Artist Aggregate")
class ArtistTest {

    @Nested
    @DisplayName("아티스트 생성")
    inner class CreateArtist {

        @Test
        @DisplayName("유효한 정보로 아티스트를 생성하면 아티스트가 생성되어야 한다")
        fun `should create artist with valid info`() {
            // Given
            val name = "BTS"
            val englishName = "Bangtan Sonyeondan"
            val agency = "HYBE"

            // When
            val artist = Artist.create(
                name = name,
                englishName = englishName,
                agency = agency
            )

            // Then
            assertNotNull(artist.id)
            assertEquals(name, artist.name)
            assertEquals(englishName, artist.englishName)
            assertEquals(agency, artist.agency)
            assertTrue(artist.active)
            assertNotNull(artist.createdAt)
        }

        @Test
        @DisplayName("영문명 없이도 아티스트를 생성할 수 있어야 한다")
        fun `should create artist without english name`() {
            // When
            val artist = Artist.create(
                name = "아이유",
                englishName = null,
                agency = "EDAM Entertainment"
            )

            // Then
            assertEquals("아이유", artist.name)
            assertNull(artist.englishName)
        }

        @Test
        @DisplayName("빈 이름으로 아티스트를 생성하면 예외가 발생해야 한다")
        fun `should throw exception when name is blank`() {
            // When & Then
            assertThrows<IllegalArgumentException> {
                Artist.create(name = "", englishName = null, agency = "Agency")
            }
        }
    }

    @Nested
    @DisplayName("아티스트 정보 수정")
    inner class UpdateArtist {

        @Test
        @DisplayName("아티스트 프로필 이미지를 업데이트할 수 있어야 한다")
        fun `should update profile image`() {
            // Given
            val artist = Artist.create("BTS", "BTS", "HYBE")
            val imageUrl = "https://example.com/bts.jpg"

            // When
            artist.updateProfileImage(imageUrl)

            // Then
            assertEquals(imageUrl, artist.profileImageUrl)
        }

        @Test
        @DisplayName("아티스트 소개글을 업데이트할 수 있어야 한다")
        fun `should update description`() {
            // Given
            val artist = Artist.create("BTS", "BTS", "HYBE")
            val description = "세계적인 K-POP 그룹"

            // When
            artist.updateDescription(description)

            // Then
            assertEquals(description, artist.description)
        }

        @Test
        @DisplayName("아티스트를 비활성화할 수 있어야 한다")
        fun `should deactivate artist`() {
            // Given
            val artist = Artist.create("BTS", "BTS", "HYBE")
            assertTrue(artist.active)

            // When
            artist.deactivate()

            // Then
            assertFalse(artist.active)
        }

        @Test
        @DisplayName("비활성화된 아티스트를 재활성화할 수 있어야 한다")
        fun `should reactivate artist`() {
            // Given
            val artist = Artist.create("BTS", "BTS", "HYBE")
            artist.deactivate()
            assertFalse(artist.active)

            // When
            artist.activate()

            // Then
            assertTrue(artist.active)
        }
    }

    @Nested
    @DisplayName("아티스트 멤버 관리")
    inner class ManageMembers {

        @Test
        @DisplayName("그룹에 멤버를 추가할 수 있어야 한다")
        fun `should add member to group`() {
            // Given
            val artist = Artist.create("BTS", "BTS", "HYBE", isGroup = true)

            // When
            artist.addMember("RM")
            artist.addMember("Jin")
            artist.addMember("Suga")

            // Then
            assertEquals(3, artist.members.size)
            assertTrue(artist.members.contains("RM"))
        }

        @Test
        @DisplayName("솔로 아티스트에 멤버를 추가하면 예외가 발생해야 한다")
        fun `should throw exception when adding member to solo artist`() {
            // Given
            val artist = Artist.create("IU", "IU", "EDAM", isGroup = false)

            // When & Then
            assertThrows<IllegalStateException> {
                artist.addMember("IU")
            }
        }
    }
}
