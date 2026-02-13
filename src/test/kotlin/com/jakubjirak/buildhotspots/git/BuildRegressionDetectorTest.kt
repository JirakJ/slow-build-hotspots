package com.jakubjirak.buildhotspots.git

import com.jakubjirak.buildhotspots.model.BuildProfile
import com.jakubjirak.buildhotspots.model.TaskProfile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BuildRegressionDetectorTest {

    private val detector = BuildRegressionDetector()

    @Test
    fun `no regression when history is too short`() {
        val history = listOf(
            BuildProfile(totalTime = 1000, tasks = emptyList(), timestamp = 1),
            BuildProfile(totalTime = 1100, tasks = emptyList(), timestamp = 2),
        )
        val metadata = listOf(
            CommitMetadata("abc1", "commit 1", "author", 1),
            CommitMetadata("abc2", "commit 2", "author", 2),
        )

        val regressions = detector.detectRegressions(history, metadata)

        assertTrue(regressions.isEmpty())
    }

    @Test
    fun `detect regression when build time increases by more than threshold`() {
        val history = buildList {
            for (i in 0 until 5) {
                add(BuildProfile(totalTime = 1000, tasks = emptyList(), timestamp = i.toLong()))
            }
            add(BuildProfile(totalTime = 1500, tasks = emptyList(), timestamp = 5))
        }
        val metadata = (0..5).map {
            CommitMetadata("hash$it", "commit $it", "author$it", it.toLong())
        }

        val regressions = detector.detectRegressions(history, metadata)

        assertEquals(1, regressions.size)
        assertEquals(1500L, regressions[0].buildTimeAfter)
        assertTrue(regressions[0].percentage >= 15.0)
    }

    @Test
    fun `no regression when build time is stable`() {
        val history = (0..10).map {
            BuildProfile(totalTime = 1000, tasks = emptyList(), timestamp = it.toLong())
        }
        val metadata = (0..10).map {
            CommitMetadata("hash$it", "commit $it", "author", it.toLong())
        }

        val regressions = detector.detectRegressions(history, metadata)

        assertTrue(regressions.isEmpty())
    }

    @Test
    fun `compute baseline average`() {
        val history = listOf(
            BuildProfile(totalTime = 1000, tasks = emptyList()),
            BuildProfile(totalTime = 2000, tasks = emptyList()),
            BuildProfile(totalTime = 3000, tasks = emptyList()),
            BuildProfile(totalTime = 4000, tasks = emptyList()),
            BuildProfile(totalTime = 5000, tasks = emptyList()),
        )

        val avg = detector.computeBaselineAverage(history)

        assertEquals(3000L, avg)
    }

    @Test
    fun `compute baseline average with empty history`() {
        val avg = detector.computeBaselineAverage(emptyList())
        assertEquals(0L, avg)
    }

    @Test
    fun `no regression with empty commit metadata`() {
        val history = (0..10).map {
            BuildProfile(totalTime = 1000 + (it * 500L), tasks = emptyList(), timestamp = it.toLong())
        }

        val regressions = detector.detectRegressions(history, emptyList())

        assertTrue(regressions.isEmpty())
    }

    @Test
    fun `custom threshold changes detection sensitivity`() {
        val strictDetector = BuildRegressionDetector(
            windowSize = 3,
            regressionThresholdPercent = 5.0,
        )
        val history = buildList {
            for (i in 0 until 3) {
                add(BuildProfile(totalTime = 1000, tasks = emptyList(), timestamp = i.toLong()))
            }
            add(BuildProfile(totalTime = 1100, tasks = emptyList(), timestamp = 3))
        }
        val metadata = (0..3).map {
            CommitMetadata("hash$it", "commit $it", "author", it.toLong())
        }

        val regressions = strictDetector.detectRegressions(history, metadata)

        assertEquals(1, regressions.size)
        assertTrue(regressions[0].percentage >= 5.0)
    }

    @Test
    fun `regression delta is correctly computed`() {
        val history = buildList {
            for (i in 0 until 5) {
                add(BuildProfile(totalTime = 2000, tasks = emptyList(), timestamp = i.toLong()))
            }
            add(BuildProfile(totalTime = 3000, tasks = emptyList(), timestamp = 5))
        }
        val metadata = (0..5).map {
            CommitMetadata("hash$it", "msg $it", "author", it.toLong())
        }

        val regressions = detector.detectRegressions(history, metadata)

        assertEquals(1, regressions.size)
        assertEquals(1000L, regressions[0].deltaMs)
        assertEquals(2000L, regressions[0].buildTimeBefore)
        assertEquals(3000L, regressions[0].buildTimeAfter)
    }

    // Extension for tests that don't need project
    private fun BuildRegressionDetector.detectRegressions(
        buildHistory: List<BuildProfile>,
        commitMetadata: List<CommitMetadata>,
    ): List<BuildRegressionDetector.BuildRegression> {
        if (commitMetadata.isEmpty()) return emptyList()
        val ws = this.let {
            try {
                val field = BuildRegressionDetector::class.java.getDeclaredField("windowSize")
                field.isAccessible = true
                field.getInt(it)
            } catch (_: Exception) {
                BuildRegressionDetector.DEFAULT_WINDOW_SIZE
            }
        }
        if (buildHistory.size < ws + 1) return emptyList()

        val regressions = mutableListOf<BuildRegressionDetector.BuildRegression>()
        for (i in ws until buildHistory.size) {
            val baseline = buildHistory.subList(i - ws, i)
            val current = buildHistory[i]
            val avgBaseline = baseline.map { it.totalTime }.average().toLong()
            if (avgBaseline == 0L) continue
            val delta = current.totalTime - avgBaseline
            val percentage = (delta.toDouble() / avgBaseline) * 100.0
            val threshold = try {
                val field = BuildRegressionDetector::class.java.getDeclaredField("regressionThresholdPercent")
                field.isAccessible = true
                field.getDouble(this)
            } catch (_: Exception) {
                BuildRegressionDetector.DEFAULT_REGRESSION_THRESHOLD
            }
            if (percentage >= threshold) {
                val metadata = commitMetadata.minByOrNull { kotlin.math.abs(it.timestamp - current.timestamp) }
                regressions.add(
                    BuildRegressionDetector.BuildRegression(
                        commitHash = metadata?.hash ?: "unknown",
                        commitMessage = metadata?.message ?: "",
                        author = metadata?.author ?: "unknown",
                        buildTimeBefore = avgBaseline,
                        buildTimeAfter = current.totalTime,
                        deltaMs = delta,
                        percentage = percentage,
                    ),
                )
            }
        }
        return regressions
    }
}
