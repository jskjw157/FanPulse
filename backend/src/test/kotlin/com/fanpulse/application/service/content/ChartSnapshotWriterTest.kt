package com.fanpulse.application.service.content

import com.fanpulse.domain.content.Chart
import com.fanpulse.domain.content.ChartType
import com.fanpulse.domain.content.port.ChartPort
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyOrder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@DisplayName("ChartSnapshotWriter")
class ChartSnapshotWriterTest {
    private val chartPort = mockk<ChartPort>()
    private val writer = ChartSnapshotWriter(chartPort)

    @Test
    fun `deletes same-day snapshot before saving replacement`() {
        val existing = Chart.create(ChartType.APPLE_MUSIC, LocalDate.of(2026, 8, 14))
        val replacement = Chart.create(ChartType.APPLE_MUSIC, LocalDate.of(2026, 8, 14))
        every { chartPort.delete(existing) } returns Unit
        every { chartPort.flush() } returns Unit
        every { chartPort.save(replacement) } returns replacement

        assertEquals(replacement, writer.replace(existing, replacement))

        verifyOrder {
            chartPort.delete(existing)
            chartPort.flush()
            chartPort.save(replacement)
        }
    }

    @Test
    fun `replacement method is transactional`() {
        val annotation = ChartSnapshotWriter::class.java
            .getMethod("replace", Chart::class.java, Chart::class.java)
            .getAnnotation(Transactional::class.java)
        assertNotNull(annotation)
    }
}
