package com.jakubjirak.buildhotspots.history

import com.jakubjirak.buildhotspots.model.BuildProfile
import com.jakubjirak.buildhotspots.model.CacheStatus
import com.jakubjirak.buildhotspots.model.TaskProfile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BuildHistoryStoreTest {

    private fun profile(total: Long, ts: Long, path: String = "/p/profile-$ts.html") = BuildProfile(
        totalTime = total,
        tasks = listOf(
            TaskProfile(":compileKotlin", 700, CacheStatus.MISS),
            TaskProfile(":test", 300, CacheStatus.HIT),
        ),
        timestamp = ts,
        projectPath = path,
    )

    @Test
    fun `profiles round trip through the persisted entry format`() {
        val store = BuildHistoryStore()
        val original = profile(1000, 1)
        assertTrue(store.addProfile(original))

        val restored = store.getHistory().single()
        assertEquals(original.totalTime, restored.totalTime)
        assertEquals(original.timestamp, restored.timestamp)
        assertEquals(original.projectPath, restored.projectPath)
        assertEquals(original.tasks.map { it.name to it.duration }, restored.tasks.map { it.name to it.duration })
        assertEquals(listOf(CacheStatus.MISS, CacheStatus.HIT), restored.tasks.map { it.cacheStatus })
    }

    @Test
    fun `history is capped at maxEntries dropping the oldest`() {
        val store = BuildHistoryStore()
        (1..7).forEach { store.addProfile(profile(it * 100L, it.toLong()), maxEntries = 5) }
        assertEquals(5, store.getHistorySize())
        assertEquals((3..7).map { it.toLong() }, store.getHistory().map { it.timestamp })
    }

    @Test
    fun `re-analyzing the same report is ignored`() {
        val store = BuildHistoryStore()
        assertTrue(store.addProfile(profile(1000, 1)))
        assertFalse(store.addProfile(profile(1000, 1)))
        assertTrue(store.addProfile(profile(1000, 2)))
        assertEquals(2, store.getHistorySize())
    }

    @Test
    fun `non-positive limit keeps the latest build`() {
        val store = BuildHistoryStore()
        store.addProfile(profile(1, 1), maxEntries = 0)
        store.addProfile(profile(2, 2), maxEntries = 0)
        assertEquals(listOf(2L), store.getHistory().map { it.timestamp })
    }

    @Test
    fun `entries with missing or corrupt task data degrade gracefully`() {
        val entry = BuildHistoryStore.BuildEntry(
            totalTime = 10,
            taskNames = ":a|:b",
            taskDurations = "5|oops",
            taskCacheStatuses = "HIT|WHATEVER",
        )
        val tasks = entry.toBuildProfile().tasks
        assertEquals(listOf(5L, 0L), tasks.map { it.duration })
        assertEquals(listOf(CacheStatus.HIT, CacheStatus.NOT_CACHEABLE), tasks.map { it.cacheStatus })
        assertTrue(BuildHistoryStore.BuildEntry().toBuildProfile().tasks.isEmpty())
    }

    @Test
    fun `clear and loadState`() {
        val store = BuildHistoryStore()
        store.addProfile(profile(1, 1))
        store.clear()
        assertEquals(0, store.getHistorySize())

        store.loadState(BuildHistoryStore.State(entries = mutableListOf(BuildHistoryStore.BuildEntry(totalTime = 9))))
        assertEquals(9L, store.getHistory().single().totalTime)
    }
}
