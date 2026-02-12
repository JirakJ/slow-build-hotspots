# Slow Build Hotspots — Software Requirements Specification (SRS)

**Verze:** 2026.2.1
**Autor:** Jakub Jirák
**Datum:** 12. února 2026
**Model:** Freemium JetBrains Plugin

---

## 1. Úvod

### 1.1 Účel
Slow Build Hotspots parsuje Gradle build logy a profily a zobrazuje performance hinty přímo v build.gradle(.kts) — „tahle závislost mění cache hit-rate", „task je non-incremental", „tento modul je bottleneck".

### 1.2 Rozsah
Plugin analyzuje lokální Gradle build scany / `--profile` output a vizualizuje build performance data jako inlay hints a anotace v build souborech.

---

## 2. Celkový popis

### 2.1 Perspektiva produktu
Žádný IDE plugin neposkytuje inline build performance hinty. Gradle Enterprise (Develocity) to řeší na serveru (web dashboard), ale nikdo to netahá zpět do editoru.

### 2.2 Uživatelské skupiny
- **Android vývojáři** — velké buildy, kritická optimalizace
- **Backend vývojáři** — multi-module Gradle projekty
- **Build engineers** — systematická optimalizace build pipeline

---

## 3. Funkční požadavky

### FR-1: Build Profile Parsing
- **FR-1.1:** Parsování Gradle `--profile` HTML/JSON output
- **FR-1.2:** Parsování build scan summary (lokální `.gradle/build-scan-data/`)
- **FR-1.3:** Extrakce metrik:
  - Task execution time
  - Task outcome (UP-TO-DATE, FROM-CACHE, EXECUTED)
  - Configuration time per project
  - Dependency resolution time

### FR-2: Inline Performance Hints
- **FR-2.1:** Inlay hint u `dependencies {}` bloku: celkový resolution time
- **FR-2.2:** Inlay hint u konkrétní dependency: „Frequently invalidates cache" / „Slow resolution"
- **FR-2.3:** Inlay hint u custom task: execution time, incremental status
- **FR-2.4:** Inlay hint u `plugins {}`: configuration overhead

### FR-3: Build Health Report
- **FR-3.1:** Tool window s přehledem posledních N buildů
- **FR-3.2:** Top 10 nejpomalejších tasků
- **FR-3.3:** Cache hit-rate trend
- **FR-3.4:** Configuration vs. execution time split
- **FR-3.5:** Doporučení: „Enable configuration cache", „Use parallel execution"

### FR-4: Hotspot Detection
- **FR-4.1:** Identifikace tasků, které nikdy nejsou UP-TO-DATE
- **FR-4.2:** Závislosti, které nejvíce invalidují cache
- **FR-4.3:** Moduly s nejdelším configuration time
- **FR-4.4:** Severity: 🔴 Critical (>30s), 🟠 Slow (10-30s), 🟡 Notable (5-10s)

### FR-5: Optimization Suggestions (Paid)
- **FR-5.1:** Automatické návrhy optimalizací s quick-fix:
  - „Enable build cache" → přidej `org.gradle.caching=true`
  - „Enable parallel execution" → přidej `org.gradle.parallel=true`
  - „Enable configuration cache" → přidej `org.gradle.configuration-cache=true`
  - „Use implementation instead of api" → quick-fix
- **FR-5.2:** Before/after projekce: „Estimated time saving: 15s"

### FR-6: Trend Analysis (Paid)
- **FR-6.1:** Build time trend graf (posledních 30 dní)
- **FR-6.2:** Detekce regresí: „Build time increased 20% after commit X"
- **FR-6.3:** Export dat (CSV/JSON)

---

## 4. Nefunkční požadavky

### NFR-1: Výkon
- Profile parsing < 2s pro build s 200 tasky
- Hints se zobrazují lazy (při scrollu do view)

### NFR-2: Kompatibilita
- Gradle 7.0+ (profile format), Gradle 8.0+ (build scan local data)
- IntelliJ IDEA 2024.3+, Android Studio

### NFR-3: Data
- Všechna data lokální — žádné odesílání na server
- Profile data ukládána v `.idea/` (git-ignored)

---

## 5. Datový model

### 5.1 BuildProfile
```
BuildProfile {
  timestamp: Instant
  totalTime: Duration
  configurationTime: Duration
  executionTime: Duration
  tasks: List<TaskProfile>
  cacheHitRate: Double
  gradleVersion: String
}
```

### 5.2 TaskProfile
```
TaskProfile {
  path: String (e.g., ":app:compileKotlin")
  type: String
  duration: Duration
  outcome: Enum (EXECUTED, UP_TO_DATE, FROM_CACHE, SKIPPED, NO_SOURCE)
  isIncremental: Boolean
  cacheable: Boolean
}
```

---

## 6. Freemium model

### Free tier
- Build profile parsing (posledních 5 buildů)
- Inline hints pro top 10 hotspotů
- Build health report (basic)

### Paid tier
- Neomezená historie
- Optimization suggestions s quick-fix
- Trend analýza (30 dní)
- Regrese detection
- Export
- Všechny inline hints
