package com.jakubjirak.buildhotspots.parser

import com.intellij.openapi.diagnostic.Logger
import com.jakubjirak.buildhotspots.model.BuildProfile
import com.jakubjirak.buildhotspots.model.CacheStatus
import com.jakubjirak.buildhotspots.model.TaskProfile
import java.io.File

/**
 * Parses Gradle --profile HTML output to extract task execution data.
 * Profile reports are located in build/reports/profile/ directory.
 */
class GradleProfileParser {

    /**
     * Finds the latest profile report in the given directory.
     */
    fun findLatestProfile(profileDir: File): File? {
        if (!profileDir.exists() || !profileDir.isDirectory) return null
        return profileDir.listFiles { file -> file.name.endsWith(".html") }
            ?.maxByOrNull { it.lastModified() }
    }

    /**
     * Parses a Gradle profile HTML report file into a BuildProfile.
     */
    fun parse(file: File): BuildProfile? {
        return try {
            val content = file.readText()
            // The report's modification time is when the build ran, which keeps history ordering
            // and duplicate detection stable when the same report is analyzed twice.
            parse(content, file.absolutePath)?.copy(timestamp = file.lastModified())
        } catch (e: Exception) {
            LOG.warn("Failed to read profile file: ${file.absolutePath}", e)
            null
        }
    }

    /**
     * Parses Gradle profile HTML content into a BuildProfile.
     */
    fun parse(content: String, projectPath: String = ""): BuildProfile? {
        return try {
            val tasks = parseTaskTable(content)
            val totalTime = parseTotalTime(content) ?: tasks.sumOf { it.duration }

            if (tasks.isEmpty() && totalTime == 0L) {
                LOG.info("No task data found in profile report")
                return null
            }

            BuildProfile(
                totalTime = totalTime,
                tasks = tasks,
                projectPath = projectPath,
            )
        } catch (e: Exception) {
            LOG.warn("Failed to parse profile content", e)
            null
        }
    }

    internal fun parseTaskTable(content: String): List<TaskProfile> {
        val tasks = mutableListOf<TaskProfile>()

        // Gradle profile HTML contains tables with task execution data
        // Look for rows in the "Task Execution" section
        val taskSectionPattern = Regex(
            """<h2[^>]*>\s*Task [Ee]xecution\s*</h2>.*?<table[^>]*>(.*?)</table>""",
            RegexOption.DOT_MATCHES_ALL,
        )
        val sectionMatch = taskSectionPattern.find(content)
        val tableContent = sectionMatch?.groupValues?.get(1) ?: return parseAllTaskRows(content)

        return parseTableRows(tableContent)
    }

    private fun parseAllTaskRows(content: String): List<TaskProfile> {
        // Fallback: parse all table rows that look like task entries
        val tasks = mutableListOf<TaskProfile>()
        val rowPattern = Regex(
            """<tr>\s*<td[^>]*>\s*(:[^\s<]+)\s*</td>\s*<td[^>]*>\s*([^<]+)\s*</td>""",
            RegexOption.DOT_MATCHES_ALL,
        )

        for (match in rowPattern.findAll(content)) {
            val taskName = match.groupValues[1].trim()
            val durationStr = match.groupValues[2].trim()
            val duration = parseDuration(durationStr)
            if (duration >= 0) {
                tasks.add(createTaskProfile(taskName, duration, content))
            }
        }
        return tasks
    }

    private fun parseTableRows(tableContent: String): List<TaskProfile> {
        val tasks = mutableListOf<TaskProfile>()
        val rowPattern = Regex(
            """<tr>\s*<td[^>]*>\s*([^<]+)\s*</td>\s*<td[^>]*>\s*([^<]+)\s*</td>""",
            RegexOption.DOT_MATCHES_ALL,
        )

        for (match in rowPattern.findAll(tableContent)) {
            val taskName = match.groupValues[1].trim()
            val durationStr = match.groupValues[2].trim()

            // Skip header rows
            if (taskName.equals("Task", ignoreCase = true) || taskName.equals("Duration", ignoreCase = true)) {
                continue
            }

            val duration = parseDuration(durationStr)
            if (duration >= 0) {
                tasks.add(createTaskProfile(taskName, duration, tableContent))
            }
        }
        return tasks
    }

    private fun createTaskProfile(rawTaskName: String, duration: Long, context: String): TaskProfile {
        val taskName = cleanTaskName(rawTaskName)
        val cacheStatus = detectCacheStatus(rawTaskName, context)
        val incremental = !isNonIncremental(rawTaskName, context)
        return TaskProfile(
            name = taskName,
            duration = duration,
            cacheStatus = cacheStatus,
            incremental = incremental,
        )
    }

    private fun cleanTaskName(rawName: String): String {
        val statusPattern = Regex(
            """\s*(UP-TO-DATE|FROM-CACHE|NO-SOURCE|SKIPPED|full recompilation|non-incremental)""",
            RegexOption.IGNORE_CASE,
        )
        return rawName.replace(statusPattern, "").trim()
    }

    internal fun parseTotalTime(content: String): Long? {
        // Look for "Total Build Time" or similar patterns in HTML or plain text
        val patterns = listOf(
            Regex("""Total [Bb]uild [Tt]ime\s*[:]\s*(?:<[^>]*>\s*)?([^<\n]+)"""),
            Regex("""Total [Bb]uild [Tt]ime\s*<[^>]*>\s*([^<\n]+)"""),
        )
        for (pattern in patterns) {
            val match = pattern.find(content)
            val result = match?.groupValues?.get(1)?.trim()?.let { parseDuration(it) }
            if (result != null && result >= 0) return result
        }
        return null
    }

    internal fun parseDuration(durationStr: String): Long {
        // Handle formats: "1.234s", "1234ms", "1m 23.456s", "0.123s"
        val secondsPattern = Regex("""(\d+(?:\.\d+)?)\s*s""")
        val msPattern = Regex("""(\d+)\s*ms""")
        val minSecPattern = Regex("""(\d+)\s*m\s+(\d+(?:\.\d+)?)\s*s""")

        minSecPattern.find(durationStr)?.let { match ->
            val minutes = match.groupValues[1].toLong()
            val seconds = match.groupValues[2].toDouble()
            return minutes * 60_000 + (seconds * 1000).toLong()
        }

        msPattern.find(durationStr)?.let { match ->
            return match.groupValues[1].toLong()
        }

        secondsPattern.find(durationStr)?.let { match ->
            return (match.groupValues[1].toDouble() * 1000).toLong()
        }

        return -1
    }

    private fun detectCacheStatus(rawTaskName: String, context: String): CacheStatus {
        val cleanName = cleanTaskName(rawTaskName)
        val escapedName = Regex.escape(cleanName)

        return when {
            rawTaskName.contains("FROM-CACHE", ignoreCase = true) -> CacheStatus.HIT
            rawTaskName.contains("UP-TO-DATE", ignoreCase = true) -> CacheStatus.HIT
            rawTaskName.contains("NO-SOURCE", ignoreCase = true) -> CacheStatus.NO_SOURCE
            Regex("""$escapedName[^<]*FROM-CACHE""", RegexOption.IGNORE_CASE).containsMatchIn(context) -> CacheStatus.HIT
            Regex("""$escapedName[^<]*UP-TO-DATE""", RegexOption.IGNORE_CASE).containsMatchIn(context) -> CacheStatus.HIT
            Regex("""$escapedName[^<]*NO-SOURCE""", RegexOption.IGNORE_CASE).containsMatchIn(context) -> CacheStatus.NO_SOURCE
            else -> CacheStatus.MISS
        }
    }

    private fun isNonIncremental(rawTaskName: String, context: String): Boolean {
        if (rawTaskName.contains("non-incremental", ignoreCase = true) ||
            rawTaskName.contains("full recompilation", ignoreCase = true)
        ) {
            return true
        }
        val cleanName = cleanTaskName(rawTaskName)
        val escapedName = Regex.escape(cleanName)
        val nonIncrementalPattern = Regex(
            """$escapedName[^<]*(?:non-incremental|full recompilation|all input files are considered out-of-date)""",
            RegexOption.IGNORE_CASE,
        )
        return nonIncrementalPattern.containsMatchIn(context)
    }

    companion object {
        private val LOG = Logger.getInstance(GradleProfileParser::class.java)
    }
}
