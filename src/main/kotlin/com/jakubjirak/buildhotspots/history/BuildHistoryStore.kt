package com.jakubjirak.buildhotspots.history

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.RoamingType
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.diagnostic.Logger
import com.jakubjirak.buildhotspots.model.BuildProfile
import com.jakubjirak.buildhotspots.model.CacheStatus
import com.jakubjirak.buildhotspots.model.TaskProfile

/**
 * Persists build profiles over time for trend analysis.
 */
@Service(Service.Level.APP)
@State(
    name = "BuildHistoryStore",
    storages = [Storage(value = "buildHotspotsHistory.xml", roamingType = RoamingType.LOCAL)],
)
class BuildHistoryStore : PersistentStateComponent<BuildHistoryStore.State> {

    data class State(
        var entries: MutableList<BuildEntry> = mutableListOf(),
        var maxEntries: Int = DEFAULT_MAX_ENTRIES,
    )

    data class BuildEntry(
        var totalTime: Long = 0,
        var taskCount: Int = 0,
        var timestamp: Long = 0,
        var projectPath: String = "",
        var taskNames: String = "",
        var taskDurations: String = "",
        var taskCacheStatuses: String = "",
    ) {
        fun toBuildProfile(): BuildProfile {
            val names = if (taskNames.isBlank()) emptyList() else taskNames.split(SEPARATOR)
            val durations = if (taskDurations.isBlank()) emptyList() else taskDurations.split(SEPARATOR)
            val cacheStatuses = if (taskCacheStatuses.isBlank()) emptyList() else taskCacheStatuses.split(SEPARATOR)

            val tasks = names.mapIndexed { index, name ->
                TaskProfile(
                    name = name,
                    duration = durations.getOrNull(index)?.toLongOrNull() ?: 0,
                    cacheStatus = cacheStatuses.getOrNull(index)?.let {
                        try {
                            CacheStatus.valueOf(it)
                        } catch (_: IllegalArgumentException) {
                            CacheStatus.NOT_CACHEABLE
                        }
                    } ?: CacheStatus.NOT_CACHEABLE,
                )
            }

            return BuildProfile(
                totalTime = totalTime,
                tasks = tasks,
                timestamp = timestamp,
                projectPath = projectPath,
            )
        }
    }

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }

    fun addProfile(profile: BuildProfile) {
        val entry = BuildEntry(
            totalTime = profile.totalTime,
            taskCount = profile.tasks.size,
            timestamp = profile.timestamp,
            projectPath = profile.projectPath,
            taskNames = profile.tasks.joinToString(SEPARATOR) { it.name },
            taskDurations = profile.tasks.joinToString(SEPARATOR) { it.duration.toString() },
            taskCacheStatuses = profile.tasks.joinToString(SEPARATOR) { it.cacheStatus.name },
        )

        myState.entries.add(entry)

        while (myState.entries.size > myState.maxEntries) {
            myState.entries.removeAt(0)
        }

        LOG.info("Build profile stored. History size: ${myState.entries.size}")
    }

    fun getHistory(): List<BuildProfile> {
        return myState.entries.map { it.toBuildProfile() }
    }

    fun getHistorySize(): Int = myState.entries.size

    fun clear() {
        myState.entries.clear()
    }

    companion object {
        private val LOG = Logger.getInstance(BuildHistoryStore::class.java)
        private const val SEPARATOR = "|"
        const val DEFAULT_MAX_ENTRIES = 50

        fun getInstance(): BuildHistoryStore =
            ApplicationManager.getApplication().getService(BuildHistoryStore::class.java)
    }
}
