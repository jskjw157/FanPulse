package com.fanpulse.domain.content

import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDate
import java.util.*

/**
 * Artist Aggregate Root
 * Represents a K-POP artist (solo or group).
 */
@Entity
@Table(name = "artists")
class Artist private constructor(
    @Id
    @Column(columnDefinition = "uuid")
    val id: UUID,

    @Column(nullable = false, length = 100)
    var name: String,

    @Column(name = "english_name", length = 100)
    var englishName: String?,

    @Column(length = 100)
    var agency: String?,

    @Column(name = "is_group", nullable = false)
    val isGroup: Boolean,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant
) {
    @Column(columnDefinition = "TEXT")
    var description: String? = null
        private set

    @Column(name = "profile_image_url", columnDefinition = "TEXT")
    var profileImageUrl: String? = null
        private set

    @Column(name = "debut_date")
    var debutDate: LocalDate? = null
        private set

    @Column(nullable = false)
    var active: Boolean = true
        private set

    @ElementCollection
    @CollectionTable(
        name = "artist_members",
        joinColumns = [JoinColumn(name = "artist_id")]
    )
    @Column(name = "member_name", length = 100)
    private val _members: MutableSet<String> = mutableSetOf()

    val members: Set<String>
        get() = _members.toSet()

    companion object {
        fun create(
            name: String,
            englishName: String?,
            agency: String?,
            isGroup: Boolean = false
        ): Artist {
            require(name.isNotBlank()) { "Artist name cannot be blank" }

            return Artist(
                id = UUID.randomUUID(),
                name = name,
                englishName = englishName,
                agency = agency,
                isGroup = isGroup,
                createdAt = Instant.now()
            )
        }
    }

    fun updateDescription(description: String?) {
        this.description = description
    }

    fun updateProfileImage(imageUrl: String?) {
        this.profileImageUrl = imageUrl
    }

    fun updateDebutDate(date: LocalDate?) {
        this.debutDate = date
    }

    fun deactivate() {
        this.active = false
    }

    fun activate() {
        this.active = true
    }

    fun addMember(memberName: String) {
        check(isGroup) { "Cannot add members to a solo artist" }
        require(memberName.isNotBlank()) { "Member name cannot be blank" }
        _members.add(memberName)
    }

    fun removeMember(memberName: String) {
        check(isGroup) { "Cannot remove members from a solo artist" }
        _members.remove(memberName)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Artist) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
