# Slow Build Hotspots

## Find and Fix What Makes Your Gradle Builds Slow

---

## The Problem

Developers lose hours every week waiting for slow Gradle builds. Build profiles are raw HTML files full of data but no clear answers. You know the build is slow, but you do not know which tasks to optimize first or whether caching is actually working.

## The Solution

Slow Build Hotspots analyzes your Gradle build profiles to pinpoint the exact tasks, cache misses, and non-incremental operations that are slowing you down — with actionable optimization recommendations and before/after projections.

---

## Key Features

### 1. Build Profile Analysis
Parse Gradle `--profile` HTML reports automatically. The plugin extracts task durations, cache status, and incremental build information into a clear, sorted view.

### 2. Hotspot Detection
Find the tasks that consume the most build time. Tasks are ranked by duration and categorized by severity: Critical (>30s), Slow (10-30s), and Notable (5-10s).

### 3. Cache Miss Analysis
Detect tasks that should be cached but are not. See your cache hit rate and identify tasks that rebuild unnecessarily every time.

### 4. Performance Dashboard
A visual breakdown of build time by task, phase, and module. See exactly where time goes in every build.

### 5. Inline Performance Hints
See build hotspot annotations directly in your `build.gradle` or `build.gradle.kts` files. The slowest tasks are highlighted right where they are configured.

### 6. Build Health Report
A summary showing the top 10 slowest tasks, cache hit rate, configuration vs execution time split, and overall build health score.

### 7. Optimization Suggestions (Pro)
Specific, actionable recommendations per task with before/after time projections. Know exactly how much time each optimization will save.

### 8. Build Time Trends (Pro)
30-day trend graphs showing build time over time with automatic regression detection. Get alerted when a build gets slower.

---

## How It Works

1. **Run a profiled build** — Execute `gradle build --profile` or use the plugin's built-in action.
2. **Analyze the profile** — The plugin parses the report and identifies hotspots, cache misses, and non-incremental tasks.
3. **Review the dashboard** — See a ranked list of slow tasks with severity levels and build health metrics.
4. **Apply optimizations** — Follow the optimization suggestions to reduce build time with measurable results.

---

## Pricing

### Free
- Build profile parsing and analysis
- Hotspot detection with severity levels
- Performance dashboard with task breakdown
- Inline performance hints (top 10 hotspots)
- Build health report
- Last 5 build profiles stored

### Pro — $3.90/month
- Optimization suggestions with before/after projections
- 30-day build time trend graphs with regression detection
- Unlimited build history
- All inline hints (not just top 10)
- CSV and JSON data export

---

## Testimonials

> *"We found a single task that was adding 45 seconds to every build. Fixed it in 10 minutes."*
> — [Your name here], Android Developer

> *"The cache miss analysis showed us we had misconfigured outputs. Build time dropped by 30%."*
> — [Your name here], Build Engineer

> *"The trend graph caught a regression the same day it was introduced."*
> — [Your name here], Tech Lead

---

## FAQ

### Which build systems are supported?
Currently Gradle only. The plugin parses Gradle `--profile` HTML reports.

### Do I need to change my build configuration?
No. Just run your build with `--profile` and the plugin analyzes the generated report. No build script changes needed.

### How many builds can the free tier track?
The free tier stores the last 5 build profiles. Upgrade to Pro for unlimited build history.

### Does it work with multi-module projects?
Yes. The plugin shows per-module task breakdowns and identifies which modules contribute the most to total build time.

### Can I export the data?
Pro users can export build analysis results as CSV or JSON for sharing with the team or importing into other tools.

---

## Get Started

Install Slow Build Hotspots from the [JetBrains Marketplace](https://plugins.jetbrains.com), run `gradle build --profile`, and see exactly where your build time goes. Free features are available immediately.
