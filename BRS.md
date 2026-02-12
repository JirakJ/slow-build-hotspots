# Slow Build Hotspots — Business Requirements Specification (BRS)

**Verze:** 2026.2.1
**Autor:** Jakub Jirák
**Datum:** 12. února 2026
**Model:** Freemium JetBrains Plugin

---

## 1. Executive Summary

Slow Build Hotspots přináší Gradle build performance data přímo do editoru. Vývojář vidí, které tasky a závislosti zpomalují build, a dostane actionable optimalizační návrhy — vše bez opuštění IDE.

---

## 2. Obchodní cíle

### 2.1 KPI
| Metrika | Cíl (6 měsíců) | Cíl (12 měsíců) |
|---------|:---:|:---:|
| Celkové instalace | 5 000 | 22 000 |
| MAU | 2 000 | 9 000 |
| Paid konverze | 4 % | 6 % |
| Marketplace rating | ≥ 4.0 | ≥ 4.2 |

---

## 3. Cílový trh

- **Android vývojáři** (40 %) — build time je #1 bolest
- **Backend Gradle projekty** (35 %) — multi-module, spring boot
- **Build engineers** (25 %) — systematická optimalizace

---

## 4. Konkurenční prostředí

**Přímá konkurence neexistuje v IDE.** Develocity (Gradle Enterprise) je server-side SaaS. Savitar je Android-only a řeší code changes, ne build analysis.

---

## 5. Revenue model

| Tier | Cena |
|------|------|
| **Free** | $0 — top 10 hotspotů, 5 buildů historie |
| **Pro** | $3.90/měsíc nebo $39/rok — neomezeno, suggestions, trend |
| **Team** | $7.90/user/měsíc — Pro + team build metriky |

---

## 6. Go-to-Market

1. Android komunita: Reddit, Twitter, Android Weekly
2. Blog: „Your Gradle Build Is Slow — Here's Exactly Where"
3. Gradle community (forums, Slack)

---

## 7. Rizika

| Riziko | Mitigace |
|--------|---------|
| Gradle build scan API proprietární | Použít `--profile` output (public) |
| Android Studio specifické problémy | Testovat na AS + IntelliJ |
| Develocity přidá IDE plugin | Diferenciace: free, lightweight, instant |

---

## 8. Milníky

| Milestone | Popis |
|-----------|-------|
| **M1: MVP** | Profile parsing, inline hints, basic report |
| **M2: Suggestions** | Optimization quick-fixes |
| **M3: Trend** | Build time trend, regrese detection |
| **M4: Advanced** | Cache analysis, dependency impact |
| **M5: Team** | Team build metriky |

---

## 9. Success Criteria

1. 10 000+ instalací do 9 měsíců
2. Pozitivní feedback z Android community
3. ≥ 4 % paid konverze
4. Měřitelné build time savings u uživatelů
