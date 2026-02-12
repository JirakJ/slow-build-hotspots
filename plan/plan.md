# Slow Build Hotspots — Implementační plán

**Verze:** 2026.2.1
**Status:** Připraveno k implementaci

---

## Fáze 0: Plugin Ikona & Branding

- [ ] Navrhnout plugin ikonu (SVG + PNG)
  - [ ] Koncept: 🔥 Kladivo s hotspot plamenem
  - [ ] Hlavní ikona: 40×40 SVG pro Marketplace listing
  - [ ] Toolbar ikona: 16×16 + 13×13 SVG (light + dark varianta)
  - [ ] Tool window ikona: 13×13 SVG
  - [ ] `src/main/resources/META-INF/pluginIcon.svg` (40×40, light theme)
  - [ ] `src/main/resources/META-INF/pluginIcon_dark.svg` (40×40, dark theme)
  - [ ] Action ikony pro toolbar/menu (16×16 SVG, light + dark)
- [ ] Barevná paleta konzistentní s Build Performance brandem
- [ ] Marketplace banner obrázek (1280×800)
- [ ] Screenshoty pro Marketplace listing (min 3, 1280×800 PNG)

## Fáze 1: Projekt scaffolding

- [ ] Inicializovat IntelliJ Platform Plugin projekt (Gradle Kotlin DSL)
  - [ ] `build.gradle.kts` — IntelliJ Platform 2024.3+, JVM 17
  - [ ] `settings.gradle.kts`, `gradle.properties` (verze `2026.2.1`)
  - [ ] Gradle wrapper (8.x)
  - [ ] Dependency: JSoup (HTML parsing pro `--profile` output)
- [ ] `src/main/resources/META-INF/plugin.xml`
  - [ ] Plugin ID: `com.jakubjirak.buildhotspots`
  - [ ] Závislosti: `com.intellij.modules.platform`, `com.intellij.gradle` (optional)
- [ ] Balíčková struktura: `com.jakubjirak.buildhotspots.*`
  - [ ] `.model` — datové třídy
  - [ ] `.parser` — build profile/log parsery
  - [ ] `.analysis` — hotspot analýza
  - [ ] `.suggestions` — optimalizační návrhy
  - [ ] `.ui` — inlay hints, tool window
  - [ ] `.storage` — lokální úložiště build dat
  - [ ] `.settings` — konfigurace
- [ ] `src/main/resources/messages/BuildHotspotsBundle.properties`
- [ ] `.gitignore` (build/, .idea/, .gradle/, .kotlin/, out/)
- [ ] `gradle/libs.versions.toml` — version catalog
- [ ] `LICENSE` (Apache-2.0)
- [ ] `.editorconfig` (Kotlin style conventions)
- [ ] Detekt konfigurace (`detekt.yml`)
- [ ] GitHub Actions CI workflow (`.github/workflows/build.yml`)
- [ ] Ověřit `./gradlew build`

## Fáze 2: Datový model

- [ ] `BuildProfile.kt`
  - [ ] `timestamp: Instant`, `totalTime: Duration`
  - [ ] `configurationTime: Duration`, `executionTime: Duration`
  - [ ] `tasks: List<TaskProfile>`, `cacheHitRate: Double`
  - [ ] `gradleVersion: String`, `projectPath: String`
- [ ] `TaskProfile.kt`
  - [ ] `path: String` (`:app:compileKotlin`), `type: String`
  - [ ] `duration: Duration`, `outcome: TaskOutcome`
  - [ ] `isIncremental: Boolean`, `cacheable: Boolean`
- [ ] `TaskOutcome.kt` — enum: EXECUTED, UP_TO_DATE, FROM_CACHE, SKIPPED, NO_SOURCE
- [ ] `BuildHotspot.kt` — identifikovaný problém
  - [ ] `task: TaskProfile`, `severity: HotspotSeverity`, `description: String`
  - [ ] `suggestion: String?`, `estimatedSaving: Duration?`
- [ ] `HotspotSeverity.kt` — enum: CRITICAL (>30s), SLOW (10–30s), NOTABLE (5–10s)
- [ ] Unit testy

## Fáze 3: Gradle Profile Parser

- [ ] `GradleProfileParser.kt`
  - [ ] `fun parseHtml(profileHtml: File): BuildProfile`
  - [ ] Parsování Gradle `--profile` HTML output:
    - [ ] Summary table: total, configuration, dependency resolution
    - [ ] Task execution table: task path, duration, outcome
  - [ ] JSoup pro HTML parsing
- [ ] `GradleProfileJsonParser.kt`
  - [ ] Parsování Gradle 8+ JSON profile (pokud dostupný)
- [ ] `ProfileLocator.kt`
  - [ ] Hledání profile souborů v `build/reports/profile/`
  - [ ] Seřazení dle timestamp (nejnovější první)
  - [ ] File watcher pro nové profily
- [ ] Unit testy s fixture HTML/JSON soubory

## Fáze 4: Build Log Parser

- [ ] `BuildLogParser.kt`
  - [ ] Parsování Gradle console output (build tool window)
  - [ ] Extrakce task timing z `--scan` summary nebo `--info` output
  - [ ] Detekce task outcomes (UP-TO-DATE, FROM-CACHE, EXECUTED)
- [ ] Integrace s IntelliJ Build Tool Window API (pokud dostupné)
- [ ] Unit testy

## Fáze 5: Hotspot Analyzer

- [ ] `HotspotAnalyzer.kt`
  - [ ] Input: `List<BuildProfile>` (historie buildů)
  - [ ] Analýzy:
    - [ ] Top N nejpomalejších tasků (across builds)
    - [ ] Tasky nikdy UP-TO-DATE (vždy EXECUTED) → non-incremental
    - [ ] Tasky nikdy FROM-CACHE → cache miss pattern
    - [ ] Configuration time vs. execution time ratio
    - [ ] Závislosti, které nejvíce invalidují cache (korelace)
  - [ ] Output: `List<BuildHotspot>` seřazené dle severity
- [ ] `TrendAnalyzer.kt`
  - [ ] Build time trend přes čas
  - [ ] Detekce regresí: „Build zpomalil o X% od commitu Y"
- [ ] Unit testy (fixture build profiles, edge cases)

## Fáze 6: Inlay Hints

- [ ] `BuildPerformanceInlayProvider.kt` implements `InlayHintsProvider`
  - [ ] Registrace v `plugin.xml` pro `*.gradle.kts` a `*.gradle` soubory
  - [ ] Hinty:
    - [ ] U `dependencies {}` bloku: celkový dependency resolution time
    - [ ] U konkrétní dependency: „⚠ Slow resolution: 2.3s" / „❌ Invalidates cache"
    - [ ] U `tasks.register {}` / task deklarace: průměrný execution time
    - [ ] U `plugins {}` bloku: configuration overhead
  - [ ] Barvy dle severity
- [ ] Mapování: task path ↔ build.gradle element (heuristický)
- [ ] Lazy loading: data z cache, aktualizace po novém buildu

## Fáze 7: Build Health Tool Window

- [ ] `BuildHealthToolWindow.kt` implements `ToolWindowFactory`
  - [ ] Registrace v `plugin.xml`
  - [ ] Tabs:
    - [ ] **Overview:** total time, config/exec split, cache hit-rate
    - [ ] **Hotspots:** tabulka top problémů se severity ikonami
    - [ ] **Tasks:** kompletní task list sortitelná dle duration
    - [ ] **History:** posledních N buildů (tabulka)
  - [ ] Refresh action (re-parse profiles)
  - [ ] Double-click na task → navigace do build.gradle
- [ ] Grafy: cache hit-rate donut chart, build time bar chart (pokud JFreeChart/kotlin-plot)

## Fáze 8: Build Data Storage

- [ ] `BuildDataStorage.kt`
  - [ ] Ukládání parsovaných BuildProfile do `.idea/build-hotspots/`
  - [ ] JSON formát, jeden soubor per build
  - [ ] Retence: posledních N buildů (konfigurovatelné, default 20)
  - [ ] Auto-cleanup starých dat
- [ ] `.gitignore` template pro `.idea/build-hotspots/`
- [ ] Unit testy

## Fáze 9: Konfigurace

- [ ] `BuildHotspotsSettings.kt` implements `PersistentStateComponent`
  - [ ] Severity thresholds (sekundy pro CRITICAL/SLOW/NOTABLE)
  - [ ] Max buildů v historii
  - [ ] Enabled inlay hints (toggle per typ)
  - [ ] Profile auto-detection (on/off)
- [ ] `BuildHotspotsConfigurable.kt` — Settings → Tools → Build Hotspots
  - [ ] Threshold slidery, history limit, hint toggles
- [ ] Unit testy

## Fáze 10: Auto-Profile Integration

- [ ] `AutoProfileTrigger.kt`
  - [ ] Detekce Gradle buildu přes IDE (ExternalSystemTaskNotificationListener)
  - [ ] Automatické přidání `--profile` k buildu (konfigurovatelné)
  - [ ] Nebo: parsování build output z IDE Build tool window
- [ ] Notifikace: „New build profile available" → „Analyze" action

## Fáze 11: Freemium & Licensing Infrastructure

- [ ] Integrace JetBrains Marketplace Licensing API
  - [ ] `LicenseChecker.kt` — ověření licence přes `com.intellij.ide.plugins.PluginManager` a Marketplace API
  - [ ] `LicenseStatus.kt` — enum: FREE, TRIAL, PAID, EXPIRED
  - [ ] `FreemiumGate.kt` — centrální guard pro paid features
    - [ ] `fun isPaidFeatureAvailable(featureId: String): Boolean`
    - [ ] Graceful degradation — paid UI elementy skryté/disabled ve free tieru
  - [ ] Trial period: 30 dní plný přístup
- [ ] `plugin.xml` — `<product-descriptor>` element pro Marketplace licensing
  - [ ] `code` atribut (unique product code)
  - [ ] `release-date`, `release-version`
- [ ] Nag screen / upgrade prompt
  - [ ] Non-intrusive balloon notification při pokusu o paid feature
  - [ ] „Upgrade to Pro" action → Marketplace stránka
  - [ ] Max 1× denně (ne spam)
- [ ] Feature flags
  - [ ] `FeatureRegistry.kt` — definice free vs. paid features
  - [ ] Každý paid feature má `@PaidFeature("feature.id")` anotaci nebo guard check
- [ ] Unit testy pro licensing logic (mock Marketplace API)

## Fáze 12: Production-Grade Quality

- [ ] Error Handling & Resilience
  - [ ] Globální exception handler (`ErrorReportSubmitter` implementace)
  - [ ] Graceful degradation — plugin nesmí crashnout IDE
  - [ ] Try-catch na všech EP entry pointech
  - [ ] Timeout na všech I/O operacích (network, git, file system)
  - [ ] Rate limiting na API volání
- [ ] Logging
  - [ ] `Logger` instance v každé třídě (IntelliJ `Logger.getInstance()`)
  - [ ] Structured logging: DEBUG pro vývoj, INFO pro operace, WARN/ERROR pro problémy
  - [ ] Žádné citlivé údaje v logech
- [ ] Telemetrie (opt-in, anonymní)
  - [ ] `PluginUsageTracker.kt` — FUS (Feature Usage Statistics) integrace
  - [ ] Metriky: aktivace featury, error rate, latence
  - [ ] Registrace FUS group v `plugin.xml`
- [ ] Performance
  - [ ] Profiling na velkých projektech (10k+ souborů)
  - [ ] Background thread pro všechny I/O operace (nesmí blokovat EDT)
  - [ ] Read/write action správně (ReadAction, WriteAction, smart mode check)
  - [ ] Disposal pattern: `Disposable` pro všechny listeners a services
  - [ ] Memory leak check (WeakReference kde potřeba)
- [ ] Testing — production úroveň
  - [ ] Unit testy: ≥80 % coverage core logiky
  - [ ] Integrační testy: `BasePlatformTestCase` pro PSI, inspections, EP
  - [ ] UI testy: `HeavyPlatformTestCase` pro tool windows, dialogy
  - [ ] Performance testy: benchmark na velkých souborech
- [ ] CI/CD Pipeline
  - [ ] GitHub Actions workflow:
    - [ ] `./gradlew check` — kompilace + testy
    - [ ] `./gradlew verifyPlugin` — plugin strukturální validace
    - [ ] `./gradlew verifyPluginProjectConfiguration` — metadata validace
    - [ ] `./gradlew runPluginVerifier` — binární kompatibilita s target IDE verzemi
    - [ ] Qodana scan (JetBrains code quality)
  - [ ] Release workflow:
    - [ ] Tag-based release (push tag → build → publish)
    - [ ] `./gradlew publishPlugin` — automatický upload na Marketplace
    - [ ] Changelog generace z git commitů
  - [ ] Dependabot / Renovate pro dependency updates
- [ ] Kódová kvalita
  - [ ] Kotlin coding conventions (official JetBrains style)
  - [ ] Detekt nebo ktlint pro statickou analýzu
  - [ ] Žádné deprecated API (IntelliJ Platform compatibility)
  - [ ] `@ApiStatus.Experimental` / `@ApiStatus.Internal` anotace
- [ ] Dokumentace
  - [ ] KDoc pro všechny public API
  - [ ] README.md: instalace, konfigurace, screenshoty, FAQ
  - [ ] CHANGELOG.md (Keep a Changelog format)
  - [ ] CONTRIBUTING.md (pokud open source)

## Fáze 13: Polish & Release

- [ ] README.md
- [ ] `./gradlew verifyPlugin`, `./gradlew buildPlugin`
- [ ] Manuální testování na Gradle KTS projektech
- [ ] Testování na Android Studio (Android projekty)
- [ ] Marketplace listing
- [ ] Git tag `v2026.2.1`

---

## Budoucí fáze (Paid)

- [ ] Optimization suggestions s quick-fix (add gradle.properties flags)
- [ ] Trend analýza (30 dní, regrese detekce)
- [ ] Neomezená historie (free: 5 buildů)
- [ ] Všechny inlay hints (free: top 10)
- [ ] Export dat (CSV/JSON)
- [ ] Team build metriky (anonymizované CI data)
