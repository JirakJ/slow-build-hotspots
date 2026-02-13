# Slow Build Hotspots — Git Enhancement Specifikace

**Verze:** 2026.2.1
**Status:** Plánováno
**Závislost:** Git4Idea API, `com.intellij.git4idea`

---

## Přehled

Rozšíření pluginu o Git-based funkce pro sledování build time regresí per commit, predikci dopadu PR na build time a historickou analýzu build performance z Git history. Tyto funkce umožňují identifikovat „který commit zpomalil build" a predikovat dopad změn před mergem.

---

## FR-G1: Build Time Regression per Commit

**Popis:** Identifikace konkrétního commitu, který zpomalil build — korelace build time dat s git historií pro přesné určení regresního bodu.

### Akceptační kritéria

- [ ] AC-1: Plugin ukládá build profile s přidruženým commit SHA (aktuální HEAD při buildu)
- [ ] AC-2: Timeline view: graf build time vs. commit (x-osa: commity, y-osa: build time)
- [ ] AC-3: Automatická detekce regrese: build time vzrostl o >15 % oproti průměru posledních 5 buildů → notifikace
- [ ] AC-4: Regresní commit zvýrazněn v VCS Log (ikona 🔥 nebo barevný puntík)
- [ ] AC-5: Drill-down: kliknutí na regresní commit zobrazí diff task times (které tasky se zpomalily)
- [ ] AC-6: Blame action: „Show Build Regression Author" → identifikace autora regresního commitu

### Datový model — rozšíření

```kotlin
data class CommitBuildProfile(
    val buildProfile: BuildProfile,
    val commitSha: String,
    val commitMessage: String,
    val commitAuthor: String,
    val commitDate: Instant,
    val branch: String
)

data class BuildRegression(
    val regressionCommit: CommitBuildProfile,
    val baselineAvgTime: Duration,     // průměr posledních N buildů před regresí
    val regressionTime: Duration,       // build time regresního commitu
    val percentageIncrease: Double,     // procentuální nárůst
    val affectedTasks: List<TaskRegression> // které tasky se zpomalily
)

data class TaskRegression(
    val taskPath: String,
    val baselineAvgDuration: Duration,
    val regressionDuration: Duration,
    val percentageIncrease: Double
)
```

### Implementační poznámky

- Při každém buildu uložit `HEAD` SHA přes `GitRepositoryManager.getInstance().getRepositories()`
- Regrese detection: sliding window algoritmus (window size konfigurovatelný, default 5)
- VCS Log integrace: `VcsLogHighlighterFactory` pro highlight regresních commitů
- Graph rendering: JFreeChart nebo vlastní Swing canvas (lightweight)

---

## FR-G2: PR Build Impact Prediction

**Popis:** Predikce dopadu Pull Request na build time — analýza změněných souborů v PR a odhad vlivu na build performance na základě historických dat.

### Akceptační kritéria

- [ ] AC-1: Action „Predict Build Impact" dostupná při aktivním branch (ne main/master)
- [ ] AC-2: Analýza změněných souborů oproti base branch a korelace s historickými build daty
- [ ] AC-3: Predikce zobrazí: odhadovaný build time, změnu oproti baseline (±X%), confidence level
- [ ] AC-4: Per-task predikce: které tasky budou ovlivněny změnami (na základě file→task mapping)
- [ ] AC-5: Warning při predikci >20 % nárůstu build time
- [ ] AC-6: Report exportovatelný jako Markdown (pro vložení do PR description)

### Datový model — rozšíření

```kotlin
data class BuildImpactPrediction(
    val branch: String,
    val baseBranch: String,
    val changedFiles: List<String>,
    val predictedBuildTime: Duration,
    val baselineBuildTime: Duration,
    val predictedChange: Double,        // procentuální změna (+ nebo -)
    val confidence: Double,             // 0.0–1.0
    val affectedTasks: List<TaskImpactPrediction>,
    val riskLevel: PredictionRisk
)

data class TaskImpactPrediction(
    val taskPath: String,
    val currentAvgDuration: Duration,
    val predictedDuration: Duration,
    val reason: String                  // "Modified source files in this module"
)

enum class PredictionRisk {
    LOW,       // < 5 % nárůst
    MEDIUM,    // 5–20 % nárůst
    HIGH       // > 20 % nárůst
}
```

### Implementační poznámky

- File→task mapping: heuristika na základě module structure (`src/main/kotlin/com/...` → `:module:compileKotlin`)
- Historická korelace: regresní analýza mezi změněnými soubory a task duration
- `git diff --name-only HEAD...origin/main` pro seznam změněných souborů
- Confidence: f(počet historických dat, kvalita file→task mapping)

---

## FR-G3: Historical Build Performance

**Popis:** Timeline z Git history — build time trends per commit, vizualizace dlouhodobých trendů a identifikace vzorců zpomalení.

### Akceptační kritéria

- [ ] AC-1: Nový tab „Git History" v Build Health tool window
- [ ] AC-2: Tabulka: commit SHA (zkrácený), autor, datum, branch, total build time, delta oproti předchozímu
- [ ] AC-3: Graf: build time trend (line chart) s možností filtrování per branch
- [ ] AC-4: Heatmap: dny v týdnu × hodiny — kdy se buildí nejdéle (korelace s commit activity)
- [ ] AC-5: Statistiky: průměr, medián, P95, P99 build time per branch
- [ ] AC-6: Export: CSV/JSON export historických dat pro další analýzu

### Datový model — rozšíření

```kotlin
data class BuildHistoryTimeline(
    val entries: List<CommitBuildProfile>,
    val branch: String?,               // null = všechny branches
    val stats: BuildStatistics
)

data class BuildStatistics(
    val avgDuration: Duration,
    val medianDuration: Duration,
    val p95Duration: Duration,
    val p99Duration: Duration,
    val totalBuilds: Int,
    val regressionCount: Int,
    val trendDirection: TrendDirection  // IMPROVING, STABLE, DEGRADING
)

enum class TrendDirection {
    IMPROVING,   // build time klesá
    STABLE,      // build time stabilní (±5 %)
    DEGRADING    // build time roste
}
```

### Implementační poznámky

- Data z existujícího `BuildDataStorage` obohacená o commit metadata
- Line chart: JFreeChart `XYLineChart` nebo Swing custom rendering
- Branch filter: `ComboBoxAction` v toolbar tool window
- CSV export: standardní Java `CSVWriter` nebo Kotlin string building
- Retence: konfigurovatelná, default 100 buildů (rozšířeno z 20 pro historickou analýzu)

---

## Konfigurace — rozšíření `BuildHotspotsSettings`

```yaml
# .build-hotspots.yaml — nové sekce
git-enhancement:
  regression-detection:
    enabled: true
    window-size: 5                    # počet buildů pro baseline průměr
    regression-threshold-percent: 15  # % nárůst pro detekci regrese
    highlight-in-vcs-log: true
    notify-on-regression: true
  pr-impact-prediction:
    enabled: true
    base-branch: "main"              # auto-detect pokud neuvedeno
    high-risk-threshold-percent: 20
    export-format: "markdown"
  historical-performance:
    enabled: true
    max-history-entries: 100
    show-heatmap: true
    default-branch-filter: null      # null = všechny branches
    export-enabled: true
```
