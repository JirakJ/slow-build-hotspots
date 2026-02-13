# Changelog

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
- Optimization suggestions with quick-fixes (Pro): enable build cache, parallel execution, configuration cache
- Build time trend analysis with regression detection (Pro)
- Build history storage (last 5 builds free, unlimited with Pro)
- CSV/JSON data export (Pro)
- Configurable thresholds for slow task and configuration time warnings
- Auto-analyze option after each build
- Custom profile directory override
