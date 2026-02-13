# Slow Build Hotspots — JetBrains Marketplace Listing

## Plugin Name

Slow Build Hotspots

## Tagline

Find what's slowing your Gradle build — and fix it in one click.

## Short Description

Parses Gradle build profiles to find your slowest tasks, worst cache misses, and configuration bottlenecks. Actionable optimization suggestions with time-saving estimates.

## Full Description

```html
<h2>Slow Build Hotspots</h2>

<p>Your Gradle build takes 3 minutes but you don't know why. Is it a single slow task? Poor cache
utilization? Configuration overhead? <b>Slow Build Hotspots</b> parses Gradle's
<code>--profile</code> output to pinpoint exactly what's slowing you down — and gives you one-click
fixes with estimated time savings.</p>

<h3>Key Features</h3>
<ul>
    <li><b>Build Profile Parsing</b> — automatically analyzes Gradle <code>--profile</code> HTML/JSON output to extract task times, cache outcomes, and configuration costs</li>
    <li><b>Hotspot Detection</b> — identifies tasks that are never UP-TO-DATE, cache-invalidating dependencies, and slow configuration modules</li>
    <li><b>Severity Levels</b> — 🔴 Critical (&gt;30s), 🟠 Slow (10–30s), 🟡 Notable (5–10s)</li>
    <li><b>Inline Performance Hints</b> — see resolution times and cache impact directly in build files</li>
    <li><b>Build Health Report</b> — tool window showing top 10 slowest tasks, cache hit-rate, and configuration vs. execution split</li>
    <li><b>Build History</b> — track the last N builds to spot patterns and regressions</li>
    <li><b>Optimization Suggestions (Pro)</b> — quick-fixes to enable build cache, parallel execution, and configuration cache with time-saving estimates</li>
    <li><b>Before/After Projections (Pro)</b> — see how much time each optimization would save</li>
    <li><b>30-Day Trend Graphs (Pro)</b> — visualize build performance over time with regression detection</li>
    <li><b>Unlimited Build History (Pro)</b> — free tier keeps last 5; Pro stores all builds</li>
    <li><b>All Inline Hints (Pro)</b> — free tier shows top 10 hotspots; Pro shows all</li>
    <li><b>Export Reports (Pro)</b> — CSV and JSON export for sharing and tracking</li>
</ul>

<h3>Free Tier</h3>
<ul>
    <li>Build profile parsing (last 5 builds)</li>
    <li>Hotspot detection with severity levels</li>
    <li>Top 10 slowest tasks in build health report</li>
    <li>Inline performance hints (top 10 hotspots)</li>
    <li>Cache hit-rate and configuration vs. execution breakdown</li>
</ul>

<h3>Pro Tier ($3.90/month)</h3>
<ul>
    <li>Unlimited build history</li>
    <li>Optimization suggestions with one-click quick-fixes</li>
    <li>Before/after time-saving projections</li>
    <li>30-day trend graphs with regression detection</li>
    <li>All inline hints (not just top 10)</li>
    <li>CSV and JSON export</li>
</ul>

<h3>Team Tier ($7.90/user/month)</h3>
<ul>
    <li>All Pro features</li>
    <li>Team-wide build performance metrics</li>
    <li>Shared optimization recommendations</li>
</ul>

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

| Tier | Price | Features |
|------|-------|----------|
| Free | $0 | Profile parsing (5 builds), hotspot detection, top 10 report, basic inline hints |
| Pro | $3.90/month or $39/year | Unlimited history, optimization suggestions, time-saving projections, 30-day trends, export |
| Team | $7.90/user/month | All Pro + team-wide build metrics, shared recommendations |

## Screenshots Needed

1. **Build Health Report** — Build Hotspots tool window showing top 10 slowest tasks with severity indicators and cache hit-rate
2. **Inline Performance Hints** — `build.gradle.kts` with resolution time and cache status shown inline next to dependencies and tasks
3. **Optimization Suggestions** — quick-fix popup offering to enable build cache with estimated time savings
4. **30-Day Trend Graph** — chart showing build time trend with a regression highlighted
5. **Hotspot Detection** — task list with 🔴🟠🟡 severity indicators showing tasks that are never UP-TO-DATE
