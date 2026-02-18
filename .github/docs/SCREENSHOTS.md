# Slow Build Hotspots — Screenshot Plan

## Screenshots

### 1. Performance Dashboard
**Title:** See where your build time goes
**Capture:** The Build Hotspots tool window showing a ranked list of tasks sorted by duration with colored severity indicators (red for critical, orange for slow, yellow for notable).

### 2. Build Health Report
**Title:** Build health at a glance
**Capture:** The health report panel showing top 10 slowest tasks, overall cache hit rate percentage, configuration vs execution time pie chart, and build health score.

### 3. Inline Performance Hints
**Title:** Hotspots highlighted in your build file
**Capture:** A build.gradle.kts file with inline annotations showing task durations and severity next to the task configurations (e.g., "compileKotlin — 34s 🔴").

### 4. Cache Miss Analysis
**Title:** Find tasks that should be cached
**Capture:** The cache analysis view showing tasks with cache status (HIT/MISS), cache hit rate metric, and tasks flagged as "cacheable but not cached" highlighted.

### 5. Hotspot Detection
**Title:** Critical, slow, and notable tasks identified
**Capture:** A detailed task view showing a critical task (>30s) with its duration, cache status, incremental status, and module information.

### 6. Build Time Trends
**Title:** Track build performance over time
**Capture:** The trend graph showing build time over 30 days with a visible regression point highlighted and an alert indicator for the detected slowdown.

### 7. Optimization Suggestions
**Title:** Actionable fixes with projected savings
**Capture:** An optimization suggestion panel for a specific task showing the problem description, recommended fix, and before/after build time projection.

---

## Video Demo Outline (40 seconds)

| Time | Scene | Description |
|------|-------|-------------|
| 0-5s | Intro | Show plugin name with a slow build progress bar animation |
| 5-12s | Analyze | Run "Analyze Build Profile" from Tools menu, show the profile being parsed |
| 12-22s | Dashboard | Walk through the performance dashboard: slowest tasks, severity levels, cache hit rate |
| 22-30s | Inline | Open build.gradle.kts and show inline performance hints highlighting slow tasks |
| 30-37s | Optimize | Show an optimization suggestion with before/after time projection |
| 37-40s | CTA | "Install Slow Build Hotspots — free on JetBrains Marketplace" |
