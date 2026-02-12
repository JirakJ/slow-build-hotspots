# Copilot Instructions — Slow Build Hotspots

## Projekt
JetBrains IDE plugin — parsuje Gradle build profily a zobrazuje performance hinty přímo v build.gradle(.kts).

## Technický stack
- **Jazyk:** Kotlin (JVM 17+)
- **Build:** Gradle (Kotlin DSL) s `org.jetbrains.intellij.platform` plugin 2.x
- **Platform:** IntelliJ Platform SDK 2024.3+
- **Testování:** JUnit 5 + `BasePlatformTestCase`

## Build a testy
```bash
./gradlew build
./gradlew test
./gradlew test --tests "*.ProfileParserTest.testTaskParsing"
./gradlew runIde
./gradlew verifyPlugin
```

## Architektura
- **Parser:** `GradleProfileParser` — parsuje `--profile` HTML/JSON + build scan data
- **Analysis:** `HotspotAnalyzer` — identifikuje nejpomalejší tasky, cache-invalidating deps
- **Hints:** `BuildPerformanceInlayProvider` — inlay hints v build.gradle(.kts)
- **Report:** `BuildHealthToolWindow` — tool window s přehledem buildů
- **Suggestions:** `OptimizationSuggester` — quick-fix návrhy (Paid)
- **Trend:** `BuildTrendTracker` — ukládá historii buildů, detekuje regrese (Paid)

## Konvence
- Balíčky: `com.jirak.buildhotspots.*`
- Build data ukládána v `.idea/build-hotspots/` (gitignored)
- Severity: CRITICAL (>30s), SLOW (10–30s), NOTABLE (5–10s)
- Parsování na background threadech
- Texty v `messages/BuildHotspotsBundle.properties`
- Verzování: `2026.2.X`

## Freemium hranice
- Free: top 10 hotspotů, 5 buildů historie, basic report
- Paid: neomezeno, suggestions, trend, regrese, export
