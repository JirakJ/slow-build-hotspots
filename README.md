# Slow Build Hotspots

[![Build](https://github.com/JirakJ/slow-build-hotspots/actions/workflows/verify.yml/badge.svg)](https://github.com/JirakJ/slow-build-hotspots/actions/workflows/verify.yml)
[![Version](https://img.shields.io/badge/version-2026.9.0-blue.svg)](https://github.com/JirakJ/slow-build-hotspots/releases)
[![License](https://img.shields.io/badge/license-Apache--2.0-blue)](LICENSE)

> **Free & open source** — every feature is unlocked for everyone, no license needed. Licensed under [Apache-2.0](LICENSE).

> Identify slow Gradle tasks, cache misses, and non-incremental builds — right inside your IDE.

## ✨ Features

- **Build profile parsing** — reads the latest Gradle `--profile` HTML report
- **Hotspot detection** — slow tasks, cache misses, non-incremental tasks and high configuration time, each with a severity and an optimization suggestion
- **Task breakdown** — every task sorted by duration with cache status and incremental flag
- **Trend tab** — bar chart of the slowest tasks plus statistics (average, median, P95, min, max) over the analyzed builds and a list of regressions
- **Build history** — each analyzed build is stored locally (configurable cap) so the trend statistics grow over time

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
2. Open the **Build Hotspots** tool window (bottom panel) and click **Analyze Latest Build**, or use **Tools → Analyze Build Profile**
3. Review the **Task Breakdown**, **Hotspots** and **Trend** tabs

## ⚙️ Configuration

Open **Settings → Tools → Slow Build Hotspots**.

| Setting | Default | Description |
|---------|---------|-------------|
| Slow task threshold (%) | `10` | Tasks taking at least this share of total build time are reported as slow |
| Configuration threshold (%) | `20` | Total configuration time above this share of the build is reported |
| Profile directory | _(empty)_ | Directory with Gradle profile reports; empty means `<project>/build/reports/profile` |
| Max history entries | `50` | How many analyzed builds are kept for the Trend statistics (10–500) |

## 🎯 Usage

### Hotspot severity
- **Slow task** — 🔴 Critical ≥30% of total build time, 🟠 Warning ≥20%, 🟡 Info at or above the threshold
- **Cache miss / non-incremental** (tasks over 500 ms) — 🔴 Critical ≥5 s, 🟠 Warning ≥2 s, 🟡 Info otherwise
- **Configuration time** — 🔴 Critical ≥40%, 🟠 Warning ≥30%, 🟡 Info at or above the threshold

### Trend and regressions
Every analysis is added to a local history (re-analyzing the same report is ignored). The **Trend** tab shows
statistics over that history and lists builds that were at least 15% slower than the average of the five builds
before them. Regression detection starts once more than five builds have been analyzed.

## 📋 Requirements

- IntelliJ IDEA 2023.2+ (or another IntelliJ-based IDE, build 232+)
- A Gradle build that can run with `--profile`

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
