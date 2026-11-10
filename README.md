# Slow Build Hotspots

[![Build](https://github.com/JirakJ/slow-build-hotspots/actions/workflows/build.yml/badge.svg)](https://github.com/JirakJ/slow-build-hotspots/actions/workflows/build.yml)
[![Version](https://img.shields.io/badge/version-2026.8.0-blue.svg)](https://github.com/JirakJ/slow-build-hotspots/releases)
[![License](https://img.shields.io/badge/license-Apache--2.0-blue)](LICENSE)

> **Free & open source** — every feature is unlocked for everyone, no license needed. Licensed under [Apache-2.0](LICENSE).

> Identify slow Gradle tasks, cache misses, and non-incremental builds — right inside your IDE.

## ✨ Features

- **Build profile parsing** — analyzes Gradle `--profile` HTML reports
- **Hotspot detection** — identifies slow tasks, cache misses, and non-incremental builds
- **Performance dashboard** — task breakdown sorted by duration in the Build Hotspots tool window
- **Inline performance hints** — hotspot inlay hints in `build.gradle(.kts)`
- **Build health report** — top 10 slowest tasks, cache hit-rate, configuration vs. execution time split
- **Severity levels** — 🔴 Critical (>30s), 🟠 Slow (10–30s), 🟡 Notable (5–10s)
- **Build history** — stores build profiles for comparison
- **Optimization suggestions** — actionable quick-fixes (enable build cache, parallel execution, configuration cache)
- **Before/after projections** — estimated time savings per suggestion
- **Build time trends** — 30-day trend graph with regression detection
- **Data export** — CSV/JSON export of build performance data

## 📦 Installation

### From JetBrains Marketplace
1. Open **Settings → Plugins → Marketplace**
2. Search for **Slow Build Hotspots**
3. Click **Install** and restart the IDE

### From ZIP
1. Download the latest release from [Releases](https://github.com/JirakJ/slow-build-hotspots/releases)
2. Open **Settings → Plugins → ⚙️ → Install Plugin from Disk**
3. Select the downloaded ZIP file

## 🚀 Quick Start

1. Run your Gradle build with profiling enabled:
   ```bash
   ./gradlew build --profile
   ```
2. Open the **Build Hotspots** tool window (bottom panel)
3. Click **Analyze Build Profile** to parse the latest profile report
4. Review the task breakdown, hotspots, and optimization suggestions

## ⚙️ Configuration

Open **Settings → Tools → Slow Build Hotspots** to configure the plugin.

| Setting | Default | Description |
|---------|---------|-------------|
| `enabled` | `true` | Enable/disable the plugin |
| `slowTaskThresholdPercent` | `10` | Minimum percentage of total build time to flag a task as slow |
| `configurationThresholdPercent` | `20` | Threshold for configuration time warnings |
| `profileDirectoryOverride` | _(empty)_ | Custom path to Gradle profile reports (default: `build/reports/profile`) |
| `autoAnalyze` | `false` | Automatically analyze profile after each build |
| `maxHistoryEntries` | `50` | Maximum number of stored build profile entries |

## 🎯 Usage

### Analyzing a Build Profile
1. **Tools → Analyze Build Profile** or use the action in the Build Hotspots tool window
2. The plugin parses the Gradle `--profile` HTML report from `build/reports/profile/`
3. Results are displayed in the **Build Hotspots** tool window with tasks sorted by duration

### Understanding Hotspot Severity
- 🔴 **Critical** — task takes more than 30 seconds
- 🟠 **Slow** — task takes 10–30 seconds
- 🟡 **Notable** — task takes 5–10 seconds

### Inline Hints (in `build.gradle.kts`)
- Dependency resolution time shown at `dependencies {}` blocks
- Cache invalidation warnings on specific dependencies
- Execution time and incremental status on custom tasks
- Configuration overhead on `plugins {}` blocks

## 📋 Requirements

- IntelliJ IDEA 2024.3+ (or any JetBrains IDE based on IntelliJ Platform 2024.3+)
- Android Studio (2024.3+)
- Gradle 7.0+ (for profile format support)
- JVM 17+

## 🏗️ Building from Source

```bash
git clone https://github.com/JirakJ/slow-build-hotspots.git
cd slow-build-hotspots
./gradlew build
```

## License

Apache License 2.0 — see [LICENSE](LICENSE) and [NOTICE](NOTICE). Copyright 2026 Jakub Jirák.

## Contributing

Issues and pull requests are welcome at https://github.com/JirakJ/slow-build-hotspots.

## 👤 Author

**Jakub Jirák** — [@JirakJ](https://github.com/JirakJ)
