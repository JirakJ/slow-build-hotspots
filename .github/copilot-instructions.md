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
- Balíčky: `com.jakubjirak.buildhotspots.*`
- Build data ukládána v `.idea/build-hotspots/` (gitignored)
- Severity: CRITICAL (>30s), SLOW (10–30s), NOTABLE (5–10s)
- Parsování na background threadech
- Texty v `messages/BuildHotspotsBundle.properties`
- Verzování: `2026.2.X`

## Production-grade požadavky
- Všechny I/O operace na background threadech (`Task.Backgroundable`, coroutines)
- Read/Write actions správně (`ReadAction.run {}`, `WriteAction.run {}`)
- `Disposable` pattern pro listeners a services
- Exception handling: žádné unhandled exceptions, graceful degradation
- Logging: `Logger.getInstance(MyClass::class.java)` v každé třídě
- EDT nesmí být blokován — žádné síťové/git/file operace na EDT
- Smart mode check (`DumbService.isDumb()`) kde potřeba

## Freemium architektura
- `FreemiumGate` centrální guard: `FreemiumGate.isPaidFeatureAvailable("feature.id")`
- JetBrains Marketplace Licensing API přes `<product-descriptor>` v `plugin.xml`
- Paid UI elementy: skryté nebo disabled ve free tieru (ne crash/error)
- Trial: 30 dní plný přístup, pak downgrade na free tier
- Upgrade prompt: max 1× denně, non-intrusive balloon

## CI/CD
- GitHub Actions: `./gradlew check`, `verifyPlugin`, `runPluginVerifier`
- Detekt pro statickou analýzu Kotlin kódu
- Tag-based release → `publishPlugin` na Marketplace
- Kotlin coding conventions (JetBrains official style)

## Ikona
- `src/main/resources/META-INF/pluginIcon.svg` (40×40, light)
- `src/main/resources/META-INF/pluginIcon_dark.svg` (40×40, dark)
- Action ikony: 16×16 + 13×13 SVG (light + dark varianty)

## Freemium hranice
- Free: top 10 hotspotů, 5 buildů historie, basic report
- Paid: neomezeno, suggestions, trend, regrese, export
