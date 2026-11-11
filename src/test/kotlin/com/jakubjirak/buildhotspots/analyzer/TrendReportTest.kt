package com.jakubjirak.buildhotspots.analyzer

import com.jakubjirak.buildhotspots.model.BuildProfile
import com.jakubjirak.buildhotspots.model.TaskProfile
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.ZoneOffset

class TrendReportTest {

    private val current = BuildProfile(
        totalTime = 1000,
        tasks = listOf(TaskProfile(":slow", 800), TaskProfile(":fast", 200)),
        timestamp = 0,
    )

    @Test
    fun `breakdown lists tasks slowest first with share of total`() {
        val text = TrendReport.render(current, emptyList(), ZoneOffset.UTC)
        assertTrue(text.contains("Total build time: 1000ms"))
        assertTrue(text.indexOf(":slow") < text.indexOf(":fast"))
        assertTrue(text.contains("800ms (80%)"))
        assertTrue(text.contains("No builds stored yet."))
    }

    @Test
    fun `short history reports statistics but not regressions`() {
        val history = (1..3).map { BuildProfile(totalTime = it * 1000L, tasks = emptyList(), timestamp = it.toLong()) }
        val text = TrendReport.render(current, history, ZoneOffset.UTC)
        assertTrue(text.contains("Builds analyzed: 3"))
        assertTrue(text.contains("Median: 2000ms"))
        assertTrue(text.contains("needs more than 5 analyzed builds (have 3)"))
    }

    @Test
    fun `regressions are listed with their build date`() {
        val history = buildList {
            repeat(5) { add(BuildProfile(totalTime = 1000, tasks = emptyList(), timestamp = it.toLong())) }
            add(BuildProfile(totalTime = 2000, tasks = emptyList(), timestamp = 86_400_000L))
        }
        val text = TrendReport.render(current, history.shuffled(java.util.Random(1)), ZoneOffset.UTC)
        assertTrue(text.contains("Regressions ("))
        assertTrue(text.contains("1970-01-02 00:00  1000ms → 2000ms (+100%)"))
    }

    @Test
    fun `stable history reports no regressions`() {
        val history = (0..6).map { BuildProfile(totalTime = 1000, tasks = emptyList(), timestamp = it.toLong()) }
        val text = TrendReport.render(current, history, ZoneOffset.UTC)
        assertTrue(text.contains("No regressions"))
        assertFalse(text.contains("Regressions ("))
    }

    @Test
    fun `profile without tasks still renders`() {
        val text = TrendReport.render(BuildProfile(totalTime = 0, tasks = emptyList()), emptyList(), ZoneOffset.UTC)
        assertTrue(text.contains("Tasks executed: 0"))
    }
}
