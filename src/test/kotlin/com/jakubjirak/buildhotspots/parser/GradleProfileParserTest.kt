package com.jakubjirak.buildhotspots.parser

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GradleProfileParserTest {

    private val parser = GradleProfileParser()

    @Test
    fun `parse valid profile with task execution table`() {
        val html = """
            <html>
            <body>
            <h2>Task Execution</h2>
            <table>
                <tr><td>Task</td><td>Duration</td></tr>
                <tr><td>:compileKotlin</td><td>3.456s</td></tr>
                <tr><td>:compileJava</td><td>1.234s</td></tr>
                <tr><td>:test</td><td>5.678s</td></tr>
            </table>
            </body>
            </html>
        """.trimIndent()

        val profile = parser.parse(html)

        assertNotNull(profile)
        assertEquals(3, profile!!.tasks.size)
        assertEquals(":compileKotlin", profile.tasks[0].name)
        assertEquals(3456, profile.tasks[0].duration)
        assertEquals(":compileJava", profile.tasks[1].name)
        assertEquals(1234, profile.tasks[1].duration)
        assertEquals(":test", profile.tasks[2].name)
        assertEquals(5678, profile.tasks[2].duration)
    }

    @Test
    fun `parse profile with total build time`() {
        val html = """
            <html>
            <body>
            <p>Total Build Time: <span>12.345s</span></p>
            <h2>Task Execution</h2>
            <table>
                <tr><td>:compileKotlin</td><td>3.456s</td></tr>
            </table>
            </body>
            </html>
        """.trimIndent()

        val profile = parser.parse(html)

        assertNotNull(profile)
        assertEquals(12345, profile!!.totalTime)
    }

    @Test
    fun `parse duration in milliseconds`() {
        assertEquals(500, parser.parseDuration("500ms"))
    }

    @Test
    fun `parse duration in seconds`() {
        assertEquals(3456, parser.parseDuration("3.456s"))
    }

    @Test
    fun `parse duration in minutes and seconds`() {
        assertEquals(83456, parser.parseDuration("1m 23.456s"))
    }

    @Test
    fun `parse returns negative for invalid duration`() {
        assertEquals(-1, parser.parseDuration("invalid"))
    }

    @Test
    fun `parse empty content returns null`() {
        val profile = parser.parse("")
        assertNull(profile)
    }

    @Test
    fun `parse content without tasks returns null`() {
        val html = "<html><body><p>Nothing here</p></body></html>"
        val profile = parser.parse(html)
        assertNull(profile)
    }

    @Test
    fun `parse profile with UP-TO-DATE tasks detects cache hit`() {
        val html = """
            <html>
            <body>
            <h2>Task Execution</h2>
            <table>
                <tr><td>:compileKotlin UP-TO-DATE</td><td>0.001s</td></tr>
                <tr><td>:test</td><td>5.0s</td></tr>
            </table>
            </body>
            </html>
        """.trimIndent()

        val profile = parser.parse(html)

        assertNotNull(profile)
        assertEquals(2, profile!!.tasks.size)

        val compileTask = profile.tasks.find { it.name.contains("compileKotlin") }
        assertNotNull(compileTask)
        assertEquals(com.jakubjirak.buildhotspots.model.CacheStatus.HIT, compileTask!!.cacheStatus)
    }

    @Test
    fun `parse fallback to all task rows when no section header`() {
        val html = """
            <html>
            <body>
            <table>
                <tr><td>:compileKotlin</td><td>2.5s</td></tr>
                <tr><td>:processResources</td><td>0.5s</td></tr>
            </table>
            </body>
            </html>
        """.trimIndent()

        val profile = parser.parse(html)

        assertNotNull(profile)
        assertEquals(2, profile!!.tasks.size)
    }

    @Test
    fun `total time falls back to task sum when not in content`() {
        val html = """
            <html>
            <body>
            <h2>Task Execution</h2>
            <table>
                <tr><td>:a</td><td>1000ms</td></tr>
                <tr><td>:b</td><td>2000ms</td></tr>
            </table>
            </body>
            </html>
        """.trimIndent()

        val profile = parser.parse(html)

        assertNotNull(profile)
        assertEquals(3000, profile!!.totalTime)
    }

    @Test
    fun `parseTotalTime extracts total build time`() {
        val content = "Total Build Time: 45.678s"
        val result = parser.parseTotalTime(content)
        assertEquals(45678, result)
    }

    @Test
    fun `parseTotalTime returns null when not found`() {
        val result = parser.parseTotalTime("No total time here")
        assertNull(result)
    }

    @Test
    fun `parse skips header rows`() {
        val html = """
            <html>
            <body>
            <h2>Task Execution</h2>
            <table>
                <tr><td>Task</td><td>Duration</td></tr>
                <tr><td>:compileKotlin</td><td>2.0s</td></tr>
            </table>
            </body>
            </html>
        """.trimIndent()

        val profile = parser.parse(html)

        assertNotNull(profile)
        assertEquals(1, profile!!.tasks.size)
        assertEquals(":compileKotlin", profile.tasks[0].name)
    }

    @Test
    fun `parse detects non-incremental tasks`() {
        val html = """
            <html>
            <body>
            <h2>Task Execution</h2>
            <table>
                <tr><td>:compileKotlin full recompilation</td><td>5.0s</td></tr>
            </table>
            </body>
            </html>
        """.trimIndent()

        val profile = parser.parse(html)

        assertNotNull(profile)
        val task = profile!!.tasks[0]
        assertTrue(!task.incremental)
    }

    @Test
    fun `parse file uses report path and modification time`(@org.junit.jupiter.api.io.TempDir dir: java.io.File) {
        val report = java.io.File(dir, "profile-2026.html")
        report.writeText("<table><tr><td>:compileKotlin</td><td>1.5s</td></tr></table>")
        report.setLastModified(1_700_000_000_000L)

        val profile = parser.parse(report)!!

        assertEquals(report.absolutePath, profile.projectPath)
        assertEquals(1_700_000_000_000L, profile.timestamp)
        assertEquals(1500L, profile.tasks.single().duration)
    }

    @Test
    fun `findLatestProfile picks newest html and ignores other files`(@org.junit.jupiter.api.io.TempDir dir: java.io.File) {
        assertNull(parser.findLatestProfile(java.io.File(dir, "missing")))
        val old = java.io.File(dir, "profile-a.html").apply { writeText("x"); setLastModified(1_000_000L) }
        val new = java.io.File(dir, "profile-b.html").apply { writeText("x"); setLastModified(2_000_000L) }
        java.io.File(dir, "notes.txt").apply { writeText("x"); setLastModified(3_000_000L) }

        assertEquals(new, parser.findLatestProfile(dir))
        assertTrue(old.exists())
    }

    @Test
    fun `unreadable file yields null`(@org.junit.jupiter.api.io.TempDir dir: java.io.File) {
        assertNull(parser.parse(java.io.File(dir, "does-not-exist.html")))
    }
}
