# Slow Build Hotspots — JetBrains Marketplace Listing

---

## 📝 Plugin Information

| Field | Value |
|-------|-------|
| **Name** | Slow Build Hotspots |
| **Plugin ID** | `com.jakubjirak.buildhotspots` |
| **Version** | 2026.2.11 |
| **Category** | Build Tools |
| **Tags** | `gradle`, `build-performance`, `build-optimization`, `cache`, `profiling`, `build-time`, `hotspot` |

---

## 🎯 Short Description

> Find what's slowing your Gradle build — and fix it in one click. Parses build profiles to surface slow tasks, cache misses, and configuration bottlenecks with actionable optimization suggestions.

---

## 📖 Full Description

### 🚀 Your Gradle Build Is Slow. Now You Can See Exactly Why.

**The Problem:** Gradle builds silently get slower over time. Developers waste hours waiting, but nobody knows which tasks are the bottleneck, whether the build cache is actually working, or if configuration time is eating into execution.

**The Solution:** Slow Build Hotspots parses Gradle `--profile` reports and surfaces the slowest tasks, worst cache misses, and configuration bottlenecks directly inside your IDE — with severity-coded hotspots and actionable optimization suggestions.

---

### ✨ Key Features

#### 📊 Build Profile Parsing
Analyzes Gradle `--profile` HTML reports to extract detailed task-level timing, cache hit rates, and configuration vs. execution time split.

#### 🔥 Hotspot Detection
Identifies your slowest tasks with color-coded severity levels:
- 🔴 **Critical** — task takes more than 30 seconds
- 🟠 **Slow** — task takes 10–30 seconds
- 🟡 **Notable** — task takes 5–10 seconds

#### 📈 Performance Dashboard
Task breakdown sorted by duration in the **Build Hotspots** tool window. See cache hit-rate, configuration vs. execution time, and the top 10 slowest tasks at a glance.

#### 💡 Inline Performance Hints
Inlay hints shown directly in `build.gradle(.kts)` — dependency resolution time at `dependencies {}` blocks, cache invalidation warnings, execution time on custom tasks, and configuration overhead on `plugins {}`.

#### 🛠️ Optimization Suggestions
Actionable quick-fixes: enable build cache, parallel execution, configuration cache. Each suggestion includes estimated time savings (before/after projections).

#### 📉 Build Time Trends
30-day trend graph with regression detection. Spot when builds started getting slower and correlate with code changes.

#### 💾 Build History
Track build performance over time with a configurable history (up to 500 builds).

#### 📤 Data Export
Export build performance data as CSV or JSON for external analysis and team reporting.

---

### 💰 Pricing Model

Free & open source (Apache-2.0) — every feature is available to everyone, no license needed.

---

### 🎯 Perfect For

- **Android Developers** tired of slow Gradle builds in Android Studio
- **Backend Developers** working on large multi-module Gradle projects
- **Build Engineers** optimizing CI/CD pipeline build times
- **Tech Leads** who want visibility into build performance trends and regressions
- **Teams** looking to reduce developer wait time and improve productivity

---

### ⚙️ Requirements

| Requirement | Value |
|-------------|-------|
| **IDE Version** | IntelliJ IDEA 2024.3+ / Android Studio 2024.3+ (build 232+) |
| **Java** | 17+ |
| **Build Tool** | Gradle 7.0+ |

---

### 🚀 Quick Start

1. **Install** — Settings → Plugins → Marketplace → Search "Slow Build Hotspots"
2. **Profile your build** — Run `./gradlew build --profile` in the terminal
3. **Analyze** — Open the **Build Hotspots** tool window → Click **Analyze Build Profile**
4. **Review hotspots** — Examine task breakdown, severity levels, and cache hit-rate
5. **Optimize** — Apply one-click optimization suggestions and compare before/after projections

---

## 📸 Screenshots & Media

| # | Screenshot | Description |
|---|-----------|-------------|
| 1 | `build-hotspots-dashboard.png` | Performance dashboard showing task breakdown sorted by duration with severity indicators |
| 2 | `inline-hints-build-gradle.png` | Inline performance hints in build.gradle.kts showing dependency resolution time and cache warnings |
| 3 | `hotspot-severity-levels.png` | Color-coded severity levels (Critical/Slow/Notable) on the slowest Gradle tasks |
| 4 | `optimization-suggestions.png` | Actionable optimization suggestions with estimated time savings |
| 5 | `build-time-trends.png` | 30-day trend graph showing build time history with regression detection |
| 6 | `cache-hit-rate.png` | Cache hit-rate analysis showing missed vs. cached tasks |
| 7 | `build-health-report.png` | Build health report with top 10 slowest tasks and configuration vs. execution split |

---

## 🏷️ Marketplace Categories

| Type | Category |
|------|----------|
| **Primary** | Build Tools |
| **Secondary** | Performance, Developer Productivity |

---

## 🔍 Keywords (SEO)

`gradle`, `build performance`, `slow build`, `build optimization`, `gradle profiler`, `build cache`, `build time`, `hotspot`, `gradle tasks`, `android build`, `configuration cache`, `parallel execution`, `build speed`, `ci cd`, `gradle profile`, `incremental build`, `build analysis`, `developer productivity`

---

## 📋 Compatibility

| IDE | Supported |
|-----|-----------|
| IntelliJ IDEA Community | ✅ |
| IntelliJ IDEA Ultimate | ✅ |
| Android Studio | ✅ |

| Requirement | Value |
|-------------|-------|
| **Minimum IDE Build** | 232 (2023.2) |
| **Java Version** | 17+ |
| **Gradle Version** | 7.0+ |

---

## 📞 Vendor Information

| Field | Value |
|-------|-------|
| **Vendor** | Jakub Jirak |
| **Email** | jirak@jakubjirak.com |
| **Website** | https://jakubjirak.com |
| **Support** | support@jakubjirak.com |
| **Source Code** | https://github.com/JirakJ/slow-build-hotspots |

---

## ✅ Pre-Launch Checklist

- [ ] Plugin icon (40×40 and 80×80 SVG/PNG) included in repository
- [ ] Plugin description under 255 characters for short description field
- [ ] Full description formatted in HTML for Marketplace submission
- [ ] Screenshots captured at 1280×800 minimum resolution
- [ ] Plugin JAR/ZIP tested on IntelliJ IDEA 2024.3 (build 232)
- [ ] Plugin JAR/ZIP tested on Android Studio 2024.3+
- [ ] Plugin JAR/ZIP tested on latest IntelliJ IDEA version
- [ ] Change notes prepared for current version
- [ ] Vendor information verified and support email tested
- [ ] Plugin ID (`com.jakubjirak.buildhotspots`) registered on Marketplace
