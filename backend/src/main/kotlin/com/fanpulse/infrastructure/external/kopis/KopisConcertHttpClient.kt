package com.fanpulse.infrastructure.external.kopis

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle
import kotlin.math.ceil

private const val KOPIS_API_BASE = "https://kopis.or.kr:9001"
private const val KOPIS_SITE_BASE = "https://kopis.or.kr"
private const val KOPIS_LIST_PATH = "/api/prs/v1/por/db/prfrdb/perfo-infos"
private const val KOPIS_DETAIL_PATH = "/api/prs/v1/por/db/prfrdb/perfo-infos"
private const val KOPIS_USER_AGENT = "FanPulse/1.0 (+https://fanpulse-psi.vercel.app)"
private const val KOPIS_PAGE_SIZE = 100
private const val KOPIS_MAX_PAGES = 10
private val KOPIS_ID = Regex("PF\\d{6,12}")
private val KOPIS_DATE_FORMATTER = DateTimeFormatter.ofPattern("uuuu.MM.dd")
    .withResolverStyle(ResolverStyle.STRICT)

class KopisConcertSourceException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

data class KopisConcertListItem(
    val externalId: String,
    val name: String,
    val venueName: String?,
    val venueHall: String?,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val status: String,
    val posterUrl: String?,
)

data class KopisConcertListPage(
    val totalElements: Int,
    val items: List<KopisConcertListItem>,
)

data class KopisConcertDetail(
    val externalId: String,
    val name: String,
    val venueName: String?,
    val venueHall: String?,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val status: String,
    val posterUrl: String?,
    val performanceTime: String?,
    val priceText: String?,
    val performers: String?,
    val runtime: String?,
    val ageRating: String?,
    val venueAddress: String?,
    val ticketUrl: String,
)

data class KopisConcertRecord(
    val externalId: String,
    val name: String,
    val venueName: String?,
    val venueHall: String?,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val status: String,
    val posterUrl: String?,
    val performanceTime: String?,
    val priceText: String?,
    val performers: String?,
    val runtime: String?,
    val ageRating: String?,
    val venueAddress: String?,
    val ticketUrl: String,
)

data class KopisConcertSnapshot(
    val records: List<KopisConcertRecord>,
    val detailFailures: List<String>,
)

interface KopisConcertSource {
    fun fetchUpcomingPopularMusic(maxItems: Int): KopisConcertSnapshot
}

@Component
class KopisConcertHttpClient(
    private val objectMapper: ObjectMapper,
    @Value("\${fanpulse.concert.kopis.timeout:20s}") private val timeout: Duration,
    @Value("\${fanpulse.concert.kopis.max-bytes:1048576}") private val maxBytes: Int,
) : KopisConcertSource {
    private val httpClient = HttpClient.newBuilder()
        .connectTimeout(timeout)
        .followRedirects(HttpClient.Redirect.NEVER)
        .build()

    init {
        require(!timeout.isZero && !timeout.isNegative) { "KOPIS timeout must be positive" }
        require(maxBytes in 1..2_097_152) { "KOPIS max-bytes must be between 1 and 2097152" }
    }

    override fun fetchUpcomingPopularMusic(maxItems: Int): KopisConcertSnapshot {
        require(maxItems in 1..100) { "KOPIS max items must be between 1 and 100" }
        val first = fetchListPage(1)
        if (first.totalElements <= 0 || first.items.isEmpty()) {
            throw KopisConcertSourceException("KOPIS upcoming popular music list was empty")
        }
        val totalPages = ceil(first.totalElements.toDouble() / KOPIS_PAGE_SIZE).toInt()
        if (totalPages !in 1..KOPIS_MAX_PAGES) {
            throw KopisConcertSourceException("KOPIS upcoming popular music page count was invalid")
        }
        val all = first.items.toMutableList()
        for (page in 2..totalPages) {
            val next = fetchListPage(page)
            if (next.totalElements != first.totalElements) {
                throw KopisConcertSourceException("KOPIS list metadata changed between pages")
            }
            all += next.items
        }
        if (all.size != first.totalElements || all.map { it.externalId }.toSet().size != all.size) {
            throw KopisConcertSourceException("KOPIS list rows were incomplete or duplicated")
        }
        val today = LocalDate.now(ZoneId.of("Asia/Seoul"))
        val selected = all.asSequence()
            .filter { !it.startDate.isBefore(today) }
            .sortedWith(compareBy<KopisConcertListItem> { it.startDate }.thenBy { it.externalId })
            .take(maxItems)
            .toList()
        if (selected.isEmpty()) {
            throw KopisConcertSourceException("KOPIS upcoming popular music rows were stale")
        }

        val failures = mutableListOf<String>()
        val records = selected.map { item ->
            val detail = try {
                fetchDetail(item.externalId)
            } catch (_: KopisConcertSourceException) {
                failures += item.externalId
                null
            }
            if (detail == null) {
                item.toRecord()
            } else {
                detail.toRecord()
            }
        }
        return KopisConcertSnapshot(records, failures)
    }

    internal fun fetchListPage(page: Int): KopisConcertListPage {
        require(page in 1..KOPIS_MAX_PAGES)
        val query = linkedMapOf(
            "sPageIndex" to page.toString(),
            "pageRcdPer" to KOPIS_PAGE_SIZE.toString(),
            "orderGubun" to "01",
            "tabno" to "cccd",
            "prfNm" to "",
            "srchVisit" to "",
            "signguCode" to "",
            "signguCodeSub" to "",
            "prfPdFrom" to "",
            "prfPdTo" to "",
            "prfState" to "^01",
            "srchOpenRun" to "",
            "mt2zGenreCode" to "",
            "seatScale" to "",
            "srchPrices" to "",
            "menuGubun" to "",
            "kidState" to "",
            "festival" to "",
            "fcltyChartr" to "",
            "prfAwarded" to "",
            "muscLicenAt" to "",
            "muscCreatAt" to "",
            "srchEtcs" to "",
            "srchDt" to "",
        ).entries.joinToString("&") { (key, value) ->
            "${encode(key)}=${encode(value)}"
        }
        return parseListResponse(fetch(URI.create("$KOPIS_API_BASE$KOPIS_LIST_PATH?$query")))
    }

    internal fun fetchDetail(externalId: String): KopisConcertDetail {
        validateExternalId(externalId)
        return parseDetailResponse(
            externalId,
            fetch(URI.create("$KOPIS_API_BASE$KOPIS_DETAIL_PATH/$externalId/null"))
        )
    }

    internal fun parseListResponse(body: ByteArray): KopisConcertListPage {
        val response = try {
            objectMapper.readValue(body, KopisListResponse::class.java)
        } catch (exc: Exception) {
            throw KopisConcertSourceException("KOPIS list response was malformed", exc)
        }
        val rows = response.result
            ?: throw KopisConcertSourceException("KOPIS list result was missing")
        if (rows.isEmpty() || rows.size > KOPIS_PAGE_SIZE) {
            throw KopisConcertSourceException("KOPIS list row count was invalid")
        }
        val totals = rows.mapNotNull { it.totcnt }.toSet()
        if (totals.size != 1 || totals.single() <= 0) {
            throw KopisConcertSourceException("KOPIS list total count was invalid")
        }
        val seen = mutableSetOf<String>()
        val items = rows.map { row ->
            val id = validateExternalId(row.prfrId)
            if (!seen.add(id)) {
                throw KopisConcertSourceException("KOPIS list identifiers were duplicated")
            }
            if (row.genreNm != "대중음악" || row.prfState != "공연예정") {
                throw KopisConcertSourceException("KOPIS list category or status was invalid")
            }
            val startDate = parseDate(row.prfrBgngDt, "start")
            val endDate = parseDate(row.prfrEndDt, "end")
            if (endDate.isBefore(startDate)) {
                throw KopisConcertSourceException("KOPIS concert date range was invalid")
            }
            KopisConcertListItem(
                externalId = id,
                name = requiredText(row.prfrNm, 255, "name"),
                venueName = optionalText(row.prfrFcltyNm, 255),
                venueHall = optionalText(row.prfrPlceNm, 255),
                startDate = startDate,
                endDate = endDate,
                status = row.prfState,
                posterUrl = posterUrl(row.pstrUrlAddr),
            )
        }
        return KopisConcertListPage(totals.single(), items)
    }

    internal fun parseDetailResponse(externalId: String, body: ByteArray): KopisConcertDetail {
        validateExternalId(externalId)
        val response = try {
            objectMapper.readValue(body, KopisDetailResponse::class.java)
        } catch (exc: Exception) {
            throw KopisConcertSourceException("KOPIS detail response was malformed", exc)
        }
        val row = response.result
            ?: throw KopisConcertSourceException("KOPIS detail result was missing")
        if (validateExternalId(row.prfrId) != externalId) {
            throw KopisConcertSourceException("KOPIS detail identifier did not match")
        }
        if (row.genreNm != "대중음악" || row.prfState != "공연예정") {
            throw KopisConcertSourceException("KOPIS detail category or status was invalid")
        }
        val startDate = parseDate(row.prfrBgngDt, "start")
        val endDate = parseDate(row.prfrEndDt, "end")
        if (endDate.isBefore(startDate)) {
            throw KopisConcertSourceException("KOPIS concert date range was invalid")
        }
        val address = listOfNotNull(
            optionalText(response.resultPlc?.fcltyAddr, 500),
            optionalText(response.resultPlc?.daddr, 255),
        ).joinToString(" ").ifBlank { null }
        return KopisConcertDetail(
            externalId = externalId,
            name = requiredText(row.prfrNm, 255, "name"),
            venueName = optionalText(row.prfrFcltyNm, 255),
            venueHall = optionalText(row.prfrPlceNm, 255),
            startDate = startDate,
            endDate = endDate,
            status = row.prfState,
            posterUrl = posterUrl(row.pstrUrlAddr),
            performanceTime = optionalText(row.prfrTmGdCn, 500),
            priceText = optionalText(row.prcSeCn, 1000),
            performers = optionalText(row.prfrmrCn, 1000),
            runtime = optionalText(row.rntmNm, 100),
            ageRating = optionalText(row.vwngAgrngCn, 100),
            venueAddress = address,
            ticketUrl = sourceUrl(externalId),
        )
    }

    private fun fetch(uri: URI): ByteArray {
        if (uri.scheme != "https" || uri.host != "kopis.or.kr" || uri.port != 9001) {
            throw KopisConcertSourceException("KOPIS request URI was invalid")
        }
        try {
            val request = HttpRequest.newBuilder(uri)
                .timeout(timeout)
                .header("Accept", "application/json")
                .header("User-Agent", KOPIS_USER_AGENT)
                .GET()
                .build()
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream())
            if (response.statusCode() != 200) {
                response.body().close()
                throw KopisConcertSourceException("KOPIS returned HTTP ${response.statusCode()}")
            }
            val contentType = response.headers().firstValue("Content-Type").orElse("").lowercase()
            if (!contentType.contains("application/json")) {
                response.body().close()
                throw KopisConcertSourceException("KOPIS response Content-Type was invalid")
            }
            val declared = response.headers().firstValueAsLong("Content-Length")
            if (declared.isPresent && declared.asLong > maxBytes) {
                response.body().close()
                throw KopisConcertSourceException("KOPIS response exceeded byte limit")
            }
            val bytes = response.body().use { it.readNBytes(maxBytes + 1) }
            if (bytes.size > maxBytes) {
                throw KopisConcertSourceException("KOPIS response exceeded byte limit")
            }
            return bytes
        } catch (exc: KopisConcertSourceException) {
            throw exc
        } catch (exc: InterruptedException) {
            Thread.currentThread().interrupt()
            throw KopisConcertSourceException("KOPIS request was interrupted", exc)
        } catch (exc: Exception) {
            throw KopisConcertSourceException("KOPIS request failed", exc)
        }
    }

    private fun validateExternalId(value: String?): String =
        value?.trim()?.takeIf { KOPIS_ID.matches(it) }
            ?: throw KopisConcertSourceException("KOPIS concert identifier was invalid")

    private fun parseDate(value: String?, field: String): LocalDate = try {
        LocalDate.parse(value, KOPIS_DATE_FORMATTER)
    } catch (exc: Exception) {
        throw KopisConcertSourceException("KOPIS concert $field date was invalid", exc)
    }

    private fun requiredText(value: String?, maxLength: Int, field: String): String =
        value?.trim()?.takeIf { it.isNotEmpty() && it.length <= maxLength }
            ?: throw KopisConcertSourceException("KOPIS concert $field was invalid")

    private fun optionalText(value: String?, maxLength: Int): String? =
        value?.trim()?.takeIf { it.isNotEmpty() && it.length <= maxLength }

    private fun posterUrl(value: String?): String? {
        val raw = optionalText(value, 1000) ?: return null
        if (raw.any { it.code < 0x20 } || ".." in raw) {
            throw KopisConcertSourceException("KOPIS poster URL was invalid")
        }
        val uri = when {
            raw.startsWith("/upload/") -> URI.create("$KOPIS_SITE_BASE$raw")
            else -> try {
                URI.create(raw)
            } catch (exc: Exception) {
                throw KopisConcertSourceException("KOPIS poster URL was invalid", exc)
            }
        }
        if (uri.scheme != "https" || uri.host != "kopis.or.kr" || uri.userInfo != null || uri.port !in listOf(-1, 443)) {
            throw KopisConcertSourceException("KOPIS poster URL was invalid")
        }
        return uri.toASCIIString()
    }

    private fun sourceUrl(externalId: String): String =
        "$KOPIS_SITE_BASE/por/db/pblprfr/pblprfrView.do?menuId=MNU_00020&mt20Id=$externalId"

    private fun encode(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8)

    private fun KopisConcertListItem.toRecord() = KopisConcertRecord(
        externalId = externalId,
        name = name,
        venueName = venueName,
        venueHall = venueHall,
        startDate = startDate,
        endDate = endDate,
        status = status,
        posterUrl = posterUrl,
        performanceTime = null,
        priceText = null,
        performers = null,
        runtime = null,
        ageRating = null,
        venueAddress = null,
        ticketUrl = sourceUrl(externalId),
    )

    private fun KopisConcertDetail.toRecord() = KopisConcertRecord(
        externalId = externalId,
        name = name,
        venueName = venueName,
        venueHall = venueHall,
        startDate = startDate,
        endDate = endDate,
        status = status,
        posterUrl = posterUrl,
        performanceTime = performanceTime,
        priceText = priceText,
        performers = performers,
        runtime = runtime,
        ageRating = ageRating,
        venueAddress = venueAddress,
        ticketUrl = ticketUrl,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class KopisListResponse(val result: List<KopisListRow>? = null)

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class KopisListRow(
        val prfrId: String? = null,
        val prfrNm: String? = null,
        val genreNm: String? = null,
        val prfrBgngDt: String? = null,
        val prfrEndDt: String? = null,
        val prfState: String? = null,
        val prfrFcltyNm: String? = null,
        val prfrPlceNm: String? = null,
        val pstrUrlAddr: String? = null,
        val totcnt: Int? = null,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class KopisDetailResponse(
        val result: KopisDetailRow? = null,
        val resultPlc: KopisVenueRow? = null,
    )
    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class KopisDetailRow(
        val prfrId: String? = null,
        val prfrNm: String? = null,
        val genreNm: String? = null,
        val prfrBgngDt: String? = null,
        val prfrEndDt: String? = null,
        val prfState: String? = null,
        val prfrFcltyNm: String? = null,
        val prfrPlceNm: String? = null,
        val prfrTmGdCn: String? = null,
        val prcSeCn: String? = null,
        val prfrmrCn: String? = null,
        val rntmNm: String? = null,
        val vwngAgrngCn: String? = null,
        val pstrUrlAddr: String? = null,
    )
    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class KopisVenueRow(
        val fcltyAddr: String? = null,
        val daddr: String? = null,
    )
}
