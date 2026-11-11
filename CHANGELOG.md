# Changelog

## [Unreleased]

## [2026.9.0] - 2026-11-11

### Added
- The Trend tab now shows statistics (average, median, P95, min, max) over analyzed builds and lists build-time regressions.

### Fixed
- Builds analyzed from the tool window were not stored in history, and the "Max history entries" setting was ignored (history was always capped at 50).
- Re-analyzing the same profile report no longer adds a duplicate history entry; history entries use the report's modification time.

### Removed
- "Enable" and "Auto-analyze after build" settings — neither had any effect.
- The unused Git4Idea optional dependency and the unused build impact predictor.
- Documentation of features that do not exist (inline build-file hints, export, 30-day graphs, before/after projections).

## [2026.8.0] - 2026-11-10

### Changed
- The plugin is now completely free — all features unlocked, no license needed.
- Source code is now open source under Apache-2.0: https://github.com/JirakJ/slow-build-hotspots

## [2026.3.6] - 2026-04-17

### Changed
- Enabled freemium licensing via `<product-descriptor optional="true">`. Plugin remains fully functional without a license; premium features may be introduced in future minor releases.


## [2026.2.5] - 2026-02-13

### Added
- Added ProGuard obfuscation for code protection


## [2026.2.4] - 2026-02-13

### Changed
- Version bump for JetBrains Marketplace publication


## [2026.2.3] - 2026-02-13

### Changed
- Expanded compatibility range: sinceBuild 232.0+ (IntelliJ 2023.2+)
- Removed upper version bound for future IDE compatibility


## [2026.2.2] - 2026-02-13

### Added
- Initial release
- Gradle `--profile` HTML report parsing with task extraction
- Build performance dashboard in the **Build Hotspots** tool window
- Hotspot detection with severity levels (Critical, Slow, Notable)
- Inline performance hints in `build.gradle(.kts)` for dependencies, tasks, and plugins
- Cache hit-rate tracking and non-incremental build detection
- Optimization suggestions with quick-fixes: enable build cache, parallel execution, configuration cache
- Build time trend analysis with regression detection
- Build history storage
- CSV/JSON data export
- Configurable thresholds for slow task and configuration time warnings
- Auto-analyze option after each build
- Custom profile directory override
