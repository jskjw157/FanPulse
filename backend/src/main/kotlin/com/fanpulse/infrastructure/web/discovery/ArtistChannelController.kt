package com.fanpulse.infrastructure.web.discovery

import com.fanpulse.application.dto.discovery.*
import com.fanpulse.application.service.LiveDiscoveryResult
import com.fanpulse.application.service.LiveDiscoveryService
import com.fanpulse.domain.discovery.ArtistChannel
import com.fanpulse.domain.discovery.ArtistChannelRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import kotlinx.coroutines.runBlocking
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/admin/artist-channels")
@Tag(name = "Artist Channels (Admin)", description = "Artist channel management for live stream discovery")
@SecurityRequirement(name = "bearerAuth")
class ArtistChannelController(
    private val artistChannelRepository: ArtistChannelRepository,
    private val liveDiscoveryService: LiveDiscoveryService
) {

    @GetMapping
    @Operation(
        summary = "Get all artist channels",
        description = "Returns all registered artist channels for live stream discovery"
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Channels retrieved successfully",
            content = [Content(schema = Schema(implementation = ArtistChannelListResponse::class))]
        )
    )
    fun getAllChannels(): ResponseEntity<ArtistChannelListResponse> {
        val channels = artistChannelRepository.findAll()
        val response = ArtistChannelListResponse(
            content = channels.map { ArtistChannelResponse.from(it) },
            totalElements = channels.size.toLong()
        )
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get artist channel by ID",
        description = "Returns a specific artist channel"
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Channel retrieved successfully",
            content = [Content(schema = Schema(implementation = ArtistChannelResponse::class))]
        ),
        ApiResponse(responseCode = "404", description = "Channel not found")
    )
    fun getChannel(
        @Parameter(description = "Channel ID")
        @PathVariable id: UUID
    ): ResponseEntity<ArtistChannelResponse> {
        val channel = artistChannelRepository.findById(id).orElse(null)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(ArtistChannelResponse.from(channel))
    }

    @GetMapping("/artist/{artistId}")
    @Operation(
        summary = "Get channels by artist",
        description = "Returns all channels for a specific artist"
    )
    fun getChannelsByArtist(
        @Parameter(description = "Artist ID")
        @PathVariable artistId: UUID
    ): ResponseEntity<ArtistChannelListResponse> {
        val channels = artistChannelRepository.findByArtistId(artistId)
        val response = ArtistChannelListResponse(
            content = channels.map { ArtistChannelResponse.from(it) },
            totalElements = channels.size.toLong()
        )
        return ResponseEntity.ok(response)
    }

    @PostMapping
    @Operation(
        summary = "Create artist channel",
        description = "Registers a new artist channel for live stream discovery"
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "201",
            description = "Channel created successfully",
            content = [Content(schema = Schema(implementation = ArtistChannelResponse::class))]
        ),
        ApiResponse(responseCode = "400", description = "Invalid request"),
        ApiResponse(responseCode = "409", description = "Channel already exists for this platform/handle")
    )
    fun createChannel(
        @Valid @RequestBody request: CreateArtistChannelRequest
    ): ResponseEntity<ArtistChannelResponse> {
        // Check for duplicate
        val existing = artistChannelRepository.findByPlatformAndChannelHandle(
            request.platform,
            request.channelHandle
        )
        if (existing != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build()
        }

        val channel = ArtistChannel(
            artistId = request.artistId,
            platform = request.platform,
            channelHandle = request.channelHandle,
            channelId = request.channelId,
            channelUrl = request.channelUrl,
            isOfficial = request.isOfficial,
            isActive = request.isActive
        )

        val saved = artistChannelRepository.save(channel)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ArtistChannelResponse.from(saved))
    }

    @PatchMapping("/{id}")
    @Operation(
        summary = "Update artist channel",
        description = "Updates an existing artist channel"
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Channel updated successfully",
            content = [Content(schema = Schema(implementation = ArtistChannelResponse::class))]
        ),
        ApiResponse(responseCode = "404", description = "Channel not found")
    )
    fun updateChannel(
        @Parameter(description = "Channel ID")
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateArtistChannelRequest
    ): ResponseEntity<ArtistChannelResponse> {
        val channel = artistChannelRepository.findById(id).orElse(null)
            ?: return ResponseEntity.notFound().build()

        request.channelHandle?.let { channel.channelHandle = it }
        request.channelId?.let { channel.channelId = it }
        request.channelUrl?.let { channel.channelUrl = it }
        request.isOfficial?.let { channel.isOfficial = it }
        request.isActive?.let { channel.isActive = it }

        val updated = artistChannelRepository.save(channel)
        return ResponseEntity.ok(ArtistChannelResponse.from(updated))
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete artist channel",
        description = "Removes an artist channel from discovery"
    )
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "Channel deleted successfully"),
        ApiResponse(responseCode = "404", description = "Channel not found")
    )
    fun deleteChannel(
        @Parameter(description = "Channel ID")
        @PathVariable id: UUID
    ): ResponseEntity<Void> {
        if (!artistChannelRepository.existsById(id)) {
            return ResponseEntity.notFound().build()
        }
        artistChannelRepository.deleteById(id)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/discover")
    @Operation(
        summary = "Trigger live stream discovery",
        description = "Manually triggers discovery of live streams from all active channels"
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Discovery completed",
            content = [Content(schema = Schema(implementation = LiveDiscoveryResult::class))]
        )
    )
    fun triggerDiscovery(): ResponseEntity<LiveDiscoveryResult> {
        val result = runBlocking { liveDiscoveryService.discoverAllChannels() }
        return ResponseEntity.ok(result)
    }
}
