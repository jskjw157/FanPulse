# FanPulse API Specification v1

## Base URL

- Development: `http://localhost:8080`
- Production: `https://api.fanpulse.app`

## Authentication

All endpoints except `/api/v1/auth/*` require JWT Bearer authentication.

```
Authorization: Bearer <access_token>
```

---

## 1. Identity Context - Authentication

### POST /api/v1/auth/signup

Registers a new user with email and password.

**Request Body:**
```json
{
  "email": "fan@example.com",
  "username": "kpop_fan123",
  "password": "myPassword123"
}
```

**Response (201 Created):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2...",
  "refreshExpiresIn": 604800
}
```

**Errors:**
- `400 Bad Request`: Invalid email format, password too weak, or email/username taken

---

### POST /api/v1/auth/login

Authenticates user with email and password.

**Request Body:**
```json
{
  "email": "fan@example.com",
  "password": "myPassword123"
}
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2...",
  "refreshExpiresIn": 604800
}
```

**Errors:**
- `401 Unauthorized`: Invalid credentials

---

### POST /api/v1/auth/google

Authenticates user with Google OAuth. Creates account if first login.

**Request Body:**
```json
{
  "idToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6Ikp..."
}
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2...",
  "refreshExpiresIn": 604800
}
```

**Errors:**
- `401 Unauthorized`: Invalid Google token

---

### POST /api/v1/auth/refresh

Exchanges refresh token for new access token.

**Request Body:**
```json
{
  "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2..."
}
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "refreshToken": "bmV3IHJlZnJlc2ggdG9rZW4...",
  "refreshExpiresIn": 604800
}
```

**Errors:**
- `401 Unauthorized`: Invalid or expired refresh token

---

### POST /api/v1/auth/logout

Invalidates the current access token.

**Headers:**
```
Authorization: Bearer <access_token>
```

**Response (200 OK):**
```json
{
  "message": "Logged out successfully"
}
```

---

## 2. Identity Context - User Profile

### GET /api/v1/me

Returns current user's profile.

**Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "fan@example.com",
  "username": "kpop_fan123",
  "hasPassword": true,
  "createdAt": "2025-01-15T10:30:00Z"
}
```

---

### PATCH /api/v1/me

Updates current user's profile.

**Request Body:**
```json
{
  "username": "new_username"
}
```

**Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "fan@example.com",
  "username": "new_username",
  "hasPassword": true,
  "createdAt": "2025-01-15T10:30:00Z"
}
```

**Errors:**
- `400 Bad Request`: Username already taken

---

### PATCH /api/v1/me/password

Changes current user's password.

**Request Body:**
```json
{
  "currentPassword": "oldPassword123",
  "newPassword": "newPassword456"
}
```

**Response (200 OK):**
```json
{
  "message": "Password changed successfully"
}
```

**Errors:**
- `400 Bad Request`: Current password incorrect or new password too weak
- `409 Conflict`: User is OAuth-only (no password set)

---

### GET /api/v1/me/settings

Returns current user's settings.

**Response (200 OK):**
```json
{
  "theme": "light",
  "language": "ko",
  "pushEnabled": true,
  "updatedAt": "2025-01-15T10:30:00Z"
}
```

---

### PATCH /api/v1/me/settings

Updates current user's settings.

**Request Body:**
```json
{
  "theme": "dark",
  "language": "en",
  "pushEnabled": false
}
```

All fields are optional.

**Response (200 OK):**
```json
{
  "theme": "dark",
  "language": "en",
  "pushEnabled": false,
  "updatedAt": "2025-01-17T14:20:00Z"
}
```

**Errors:**
- `400 Bad Request`: Invalid theme or language value

---

## 3. Streaming Context

### GET /api/v1/streaming-events

Returns paginated list of streaming events with optional filtering.

**Query Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| status | string | Filter by status: `SCHEDULED`, `LIVE`, `ENDED` |
| platform | string | Filter by platform: `YOUTUBE` |
| artistId | UUID | Filter by artist |
| scheduledAfter | ISO-8601 | Events scheduled after this time |
| scheduledBefore | ISO-8601 | Events scheduled before this time |
| page | int | Page number (0-based, default: 0) |
| size | int | Page size (default: 20, max: 100) |
| sortBy | string | Sort field (default: `scheduledAt`) |
| sortDir | string | Sort direction: `asc`, `desc` (default: `desc`) |

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440001",
      "title": "NewJeans Live",
      "thumbnailUrl": "https://i.ytimg.com/vi/xxx/hqdefault.jpg",
      "artistId": "550e8400-e29b-41d4-a716-446655440002",
      "scheduledAt": "2025-01-17T18:00:00Z",
      "status": "LIVE",
      "viewerCount": 15000,
      "platform": "YOUTUBE"
    }
  ],
  "totalElements": 42,
  "page": 0,
  "size": 20,
  "totalPages": 3
}
```

---

### GET /api/v1/streaming-events/{id}

Returns detailed information about a streaming event.

**Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "title": "NewJeans Live - OMG MV Reaction",
  "description": "Let's watch OMG MV together!",
  "platform": "YOUTUBE",
  "externalId": "dQw4w9WgXcQ",
  "streamUrl": "https://www.youtube.com/embed/dQw4w9WgXcQ",
  "sourceUrl": "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
  "thumbnailUrl": "https://i.ytimg.com/vi/dQw4w9WgXcQ/maxresdefault.jpg",
  "artistId": "550e8400-e29b-41d4-a716-446655440002",
  "scheduledAt": "2025-01-17T18:00:00Z",
  "startedAt": "2025-01-17T18:02:30Z",
  "endedAt": null,
  "status": "LIVE",
  "viewerCount": 15000,
  "createdAt": "2025-01-17T10:00:00Z"
}
```

**Errors:**
- `404 Not Found`: Event not found

---

### GET /api/v1/streaming-events/live

Returns currently live streaming events, ordered by viewer count (highest first).

**Query Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| page | int | Page number (default: 0) |
| size | int | Page size (default: 20) |

**Response (200 OK):**
Same format as `/api/v1/streaming-events`

---

### GET /api/v1/streaming-events/scheduled

Returns upcoming scheduled events, ordered by scheduled time (soonest first).

**Query Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| page | int | Page number (default: 0) |
| size | int | Page size (default: 20) |

**Response (200 OK):**
Same format as `/api/v1/streaming-events`

---

### GET /api/v1/streaming-events/artist/{artistId}

Returns streaming events for a specific artist.

**Response (200 OK):**
Same format as `/api/v1/streaming-events`

---

## 4. Discovery Context (Admin)

All endpoints require admin authentication.

### GET /admin/artist-channels

Returns all registered artist channels.

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440010",
      "artistId": "550e8400-e29b-41d4-a716-446655440002",
      "platform": "YOUTUBE",
      "channelHandle": "@NewJeans_official",
      "channelId": "UC3b_-jFthFWNLt_eFzAU6mg",
      "channelUrl": "https://www.youtube.com/@NewJeans_official",
      "isOfficial": true,
      "isActive": true,
      "lastCrawledAt": "2025-01-17T12:00:00Z",
      "createdAt": "2025-01-01T00:00:00Z"
    }
  ],
  "totalElements": 5
}
```

---

### GET /admin/artist-channels/{id}

Returns a specific artist channel.

**Response (200 OK):**
Single channel object (same format as list item)

---

### GET /admin/artist-channels/artist/{artistId}

Returns all channels for a specific artist.

---

### POST /admin/artist-channels

Creates a new artist channel for discovery.

**Request Body:**
```json
{
  "artistId": "550e8400-e29b-41d4-a716-446655440002",
  "platform": "YOUTUBE",
  "channelHandle": "@IVEstarship",
  "channelId": "UC_XXX",
  "channelUrl": "https://www.youtube.com/@IVEstarship",
  "isOfficial": true,
  "isActive": true
}
```

**Response (201 Created):**
Created channel object

**Errors:**
- `409 Conflict`: Channel already exists for this platform/handle

---

### PATCH /admin/artist-channels/{id}

Updates an existing artist channel.

**Request Body:**
```json
{
  "channelHandle": "@NewChannelHandle",
  "isActive": false
}
```

All fields are optional.

**Response (200 OK):**
Updated channel object

---

### DELETE /admin/artist-channels/{id}

Deletes an artist channel.

**Response (204 No Content)**

---

### POST /admin/artist-channels/discover

Manually triggers live stream discovery from all active channels.

**Response (200 OK):**
```json
{
  "total": 10,
  "upserted": 3,
  "failed": 0,
  "errors": []
}
```

---

## Error Response Format

All error responses follow this format:

```json
{
  "timestamp": "2025-01-17T14:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/auth/signup",
  "details": [
    {
      "field": "email",
      "message": "Invalid email format"
    }
  ]
}
```

---

## Rate Limiting

| Endpoint Category | Rate Limit |
|-------------------|------------|
| Authentication | 10 requests/minute |
| User Profile | 60 requests/minute |
| Streaming Events | 100 requests/minute |
| Admin | 30 requests/minute |

---

## Changelog

| Version | Date | Changes |
|---------|------|---------|
| v1.0.0 | 2025-01-17 | Initial API specification |
