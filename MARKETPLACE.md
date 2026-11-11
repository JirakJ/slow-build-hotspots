# Slow Build Hotspots — JetBrains Marketplace Listing

## Plugin Name

Slow Build Hotspots

## Tagline

Find what's slowing your Gradle build.

## Short Description

Parses Gradle build profiles to find your slowest tasks, cache misses, non-incremental tasks and configuration overhead, with a concrete suggestion for each and build-time statistics over time.

## Full Description

```html
<h2>Slow Build Hotspots</h2>

<p>Your Gradle build takes 3 minutes but you don't know why. Is it a single slow task? Poor cache
utilization? Configuration overhead? <b>Slow Build Hotspots</b> parses Gradle's
<code>--profile</code> report to pinpoint what's slowing you down and suggests what to change.</p>

<h3>Key Features</h3>
<ul>
    <li><b>Build Profile Parsing</b> — reads the latest Gradle <code>--profile</code> HTML report</li>
    <li><b>Hotspot Detection</b> — slow tasks, cache misses, non-incremental tasks and high configuration time</li>
    <li><b>Severity &amp; Suggestions</b> — Critical / Warning / Info with a concrete recommendation per hotspot</li>
    <li><b>Task Breakdown</b> — every task sorted by duration with cache status and incremental flag</li>
    <li><b>Trend Tab</b> — slowest-task bar chart, average / median / P95 over analyzed builds, and regressions (≥15% slower than the previous five builds)</li>
    <li><b>Configurable History</b> — keep 10–500 analyzed builds</li>
</ul>

<h3>Free &amp; Open Source</h3>
<p>Every feature is free for everyone — no license, no limits. The source code is available under the Apache-2.0 license at <a href="https://github.com/JirakJ/slow-build-hotspots">github.com/JirakJ/slow-build-hotspots</a>. Issues and pull requests are welcome.</p>

<h3>Getting Started</h3>
<ol>
    <li><b>Install</b> — search "Slow Build Hotspots" in Settings → Plugins → Marketplace</li>
    <li><b>Run a profiled build</b> — execute <code>./gradlew build --profile</code> from your terminal or IDE</li>
    <li><b>Analyze</b> — go to Tools → Analyze Build Profile, then open the Build Hotspots tool window</li>
</ol>

<h3>Compatibility</h3>
<p>IntelliJ IDEA 2024.3+, Android Studio 2024.3+, and all compatible JetBrains IDEs.
Requires Gradle 7.0+ and Java 17+.</p>

<h3>Part of Developer Productivity Suite</h3>
<p>Slow Build Hotspots helps you reclaim minutes from every build cycle. Pair it with other
developer productivity plugins to eliminate friction across your entire workflow.</p>
```

## Tags

`gradle`, `build-performance`, `android`, `optimization`, `developer-tools`

## Category

Build & DevOps

## Pricing

Free & open source (Apache-2.0) — every feature is available to everyone, no license needed.

## Screenshots

1. **Hotspots tab** — hotspots with 🔴🟠🟡 severity and suggestions
2. **Task Breakdown tab** — tasks sorted by duration with cache status
3. **Trend tab** — slowest-task bars, history statistics and a detected regression
