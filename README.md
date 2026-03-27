# Busca Preços — SearchPrice

**Compare retail prices in Maceió/AL directly from the government database.**

Busca Preços is a **Kotlin Multiplatform** price-comparison app for consumers in the state of Alagoas, Brazil. It queries the [SEFAZ-AL](https://www.sefaz.al.gov.br/) official retail price registry in real time and displays results sorted by price or distance from the user's location — all from a single shared codebase that runs natively on Android, iOS, Desktop, and the Web.

![Platform](https://img.shields.io/badge/platform-Android%20%7C%20iOS%20%7C%20Desktop%20%7C%20Web%20%7C%20Android%20Auto-blue)
![Kotlin](https://img.shields.io/badge/Kotlin-2.3.0-7F52FF?logo=kotlin&logoColor=white)
![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.10.0-4285F4?logo=jetpackcompose&logoColor=white)
![License](https://img.shields.io/badge/license-MIT-green)
![CI](https://img.shields.io/github/actions/workflow/status/YOUR_USERNAME/SearchPrice/deploy-pages.yml?label=web%20deploy)

---

## Table of Contents

- [Features](#features)
- [Platforms](#platforms)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Building for Each Platform](#building-for-each-platform)
- [Android Auto](#android-auto)
- [Configuration](#configuration)
- [Security](#security)
- [Internationalization](#internationalization)
- [CI/CD](#cicd)
- [Release & Signing](#release--signing)
- [Contributing](#contributing)
- [License](#license)

---

## Features

- **Real-time price search** — queries the SEFAZ-AL government retail price API with a 10 km radius and a 3-day record window
- **Sort by price** — surface the cheapest option instantly
- **Sort by distance** — find the nearest store using the Haversine formula on real GPS coordinates
- **Location-aware** — Android and iOS request runtime GPS permission; other platforms default to the centre of Maceió
- **Open in Maps** — tap any result to open the store location in the system map app
- **Offline-friendly error handling** — descriptive error state with one-tap retry
- **Material Design 3** — dynamic colour, adaptive typography, and dark-mode-ready components
- **Adaptive app icon** — custom vector icon with monochrome support for Android 13+ themed icons
- **Bilingual UI** — Portuguese (default) and English localisations
- **Android Auto** — in-car price search via the Car App Library; uses `SearchTemplate` + `PlaceListMapTemplate` (POI category) so drivers can search products and browse results hands-free on the head unit

---

## Platforms

| Platform | Target | Minimum |
|----------|--------|---------|
| Android  | API 36 | API 24 (Android 7.0) |
| iOS      | arm64 device + simulator | iOS 16 |
| Desktop  | JVM (macOS / Windows / Linux) | JDK 17 |
| Web      | WebAssembly (preferred) | Modern browser with Wasm GC support |
| Web      | JavaScript (fallback) | Any modern browser |
| Android Auto | Car App Library (template-based) | Android 6.0 (API 23) + Auto head unit |

---

## Tech Stack

### Core

| Library | Version | Role |
|---------|---------|------|
| [Kotlin](https://kotlinlang.org/) | 2.3.0 | Language |
| [Kotlin Multiplatform](https://www.jetbrains.com/kotlin-multiplatform/) | 2.3.0 | Single codebase targeting all platforms |
| [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/) | 1.10.0 | Declarative UI shared across all platforms |
| [Compose Material 3](https://m3.material.io/) | 1.10.0-alpha05 | Design system & component library |

### Networking

| Library | Version | Role |
|---------|---------|------|
| [Ktor Client](https://ktor.io/docs/client-create-multiplatform-application.html) | 3.1.1 | Multiplatform async HTTP client |
| [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) | 1.8.0 | JSON serialisation / deserialisation |

Ktor uses a different engine per platform for optimal native performance:

| Platform | Engine |
|----------|--------|
| Android, Desktop (JVM) | `ktor-client-okhttp` |
| iOS | `ktor-client-darwin` |
| Web (JS & WasmJS) | `ktor-client-js` |

### Dependency Injection

| Library | Version | Role |
|---------|---------|------|
| [Koin](https://insert-koin.io/) | 4.1.0 | Lightweight multiplatform DI |

Koin is configured entirely in `commonMain` so the same module graph is shared across all targets. `koin-compose` and `koin-compose-viewmodel` provide first-class integration with Compose composables and ViewModels without any Android-specific plumbing.

### Lifecycle & State Management

| Library | Version | Role |
|---------|---------|------|
| [AndroidX Lifecycle (KMP)](https://developer.android.com/jetpack/androidx/releases/lifecycle) | 2.9.6 | Multiplatform `ViewModel`, `collectAsStateWithLifecycle` |
| kotlinx.coroutines | 1.10.2 | Structured concurrency, `StateFlow`, `Channel` |

### Android Auto

| Library | Version | Role |
|---------|---------|------|
| [Car App Library](https://developer.android.com/training/cars/apps) | 1.7.0 | Android Auto screen templates (POI category) |

### Build & Tooling

| Tool | Version | Role |
|------|---------|------|
| Android Gradle Plugin | 8.11.2 | Android build system |
| [BuildConfig](https://github.com/gmazzo/gradle-buildconfig-plugin) | 5.3.5 | Type-safe build constants (injects API token) |
| [Compose Hot Reload](https://github.com/JetBrains/compose-hot-reload) | 1.0.0 | Live UI changes without a full recompile |
| R8 / ProGuard | bundled | Code shrinking & obfuscation for release builds |

---

## Architecture

The project follows **Clean Architecture** with an **MVI (Model-View-Intent)** presentation layer. All business logic lives in `commonMain` and is 100% shared; only thin platform adapters (HTTP engine selection, location API, app entry point) live in platform-specific source sets.

```
┌──────────────────────────────────────────────────────────────────┐
│                         Presentation                             │
│                                                                  │
│   SearchPriceScreen  ──intent──►  SearchViewModel                │
│         │  ▲                           │                         │
│      state │ effect               handleIntent()                 │
│         ▼  │                           │                         │
│   SearchBar                   SearchProductsUseCase              │
│   SortFilterChips             GetLocationUseCase                 │
│   ProductList                                                    │
│   ProductItemCard                                                │
├──────────────────────────────────────────────────────────────────┤
│                            Domain                                │
│                                                                  │
│   Product (clean entity)     PriceRepository (interface)         │
│   SortOption (enum)          LocationUtils (Haversine formula)   │
├──────────────────────────────────────────────────────────────────┤
│                             Data                                 │
│                                                                  │
│   RemotePriceDataSource      PriceSearchRequest/Response DTOs    │
│   (Ktor HTTP)                PriceMapper (.toProduct())          │
│                              PriceRepositoryImpl                 │
└──────────────────────────────────────────────────────────────────┘
```

### MVI Contract (`SearchContract.kt`)

```
State    query · isLoading · products · sortOption · error · hasSearched
Intent   UpdateQuery · UpdateLocation · ChangeSortOption · PerformSearch · RetrySearch
Effect   ShowSnackbar · NavigateBack   (one-shot events via Channel)
```

The ViewModel caches the raw unsorted result list. Changing the sort option re-sorts in memory without issuing a new network request, keeping the UI responsive with zero redundant API calls.

### Full Data Flow

```
UI dispatches Intent
  └─► SearchViewModel.handleIntent()
        └─► SearchProductsUseCase(query, lat, lon, sortOption)
              └─► PriceRepositoryImpl.searchProducts(request)
                    └─► RemotePriceDataSource  (Ktor HTTP POST)
                          └─► SEFAZ-AL REST API
                    ◄── List<PriceSearchResponse>  (DTOs)
              ◄── PriceMapper.toProduct()  →  List<Product>
              ◄── sorted by price or distance
        ◄── new State emitted via StateFlow
  ◄── Compose recomposes only the affected subtree
```

### Location — `expect` / `actual`

`RequestLocationEffect` is an `expect` composable in `commonMain` with platform `actual` implementations:

| Platform | Behaviour |
|----------|-----------|
| Android | Requests `ACCESS_FINE_LOCATION` at runtime; reads GPS via `LocationManager` |
| iOS | Uses `CoreLocation` with `NSLocationWhenInUseUsageDescription` privacy string |
| JVM / JS / WasmJS | No-op; defaults to Maceió city centre coordinates |

---

## Project Structure

```
SearchPrice/
├── composeApp/
│   ├── proguard-rules.pro            # R8 rules for Ktor, Serialization, Koin, models
│   └── src/
│       ├── commonMain/               # 100% shared application code
│       │   ├── domain/
│       │   │   ├── model/            # Product — clean entity, no nullable fields
│       │   │   ├── repository/       # PriceRepository interface
│       │   │   ├── usecase/          # SearchProductsUseCase, GetLocationUseCase
│       │   │   └── util/             # SortOption enum, LocationUtils (Haversine)
│       │   ├── data/
│       │   │   ├── model/            # PriceSearchRequest / PriceSearchResponse DTOs
│       │   │   ├── mapper/           # PriceMapper — toProduct() extension
│       │   │   ├── source/           # RemotePriceDataSource (Ktor)
│       │   │   └── repository/       # PriceRepositoryImpl
│       │   ├── presentation/
│       │   │   ├── contract/         # SearchContract (State · Intent · Effect)
│       │   │   ├── viewmodel/        # SearchViewModel
│       │   │   └── ui/               # SearchPriceScreen, SearchBar, FilterChips,
│       │   │                         # ProductList, ProductItemCard
│       │   ├── location/             # expect RequestLocationEffect
│       │   └── composeResources/
│       │       ├── values/           # strings.xml  (Portuguese — default)
│       │       └── values-en/        # strings.xml  (English)
│       ├── androidMain/              # OkHttp engine · LocationManager · MainActivity
│       │   └── auto/                 # Android Auto: SearchPriceCarAppService,
│       │                             # SearchPriceSession, SearchInputScreen,
│       │                             # ProductSearchScreen
│       ├── iosMain/                  # Darwin engine · CoreLocation
│       ├── jvmMain/                  # Desktop entry point (main.kt)
│       ├── jsMain/                   # JS browser entry point
│       └── wasmJsMain/               # WasmJS browser entry point
├── iosApp/                           # Xcode project
├── .github/
│   └── workflows/
│       └── deploy-pages.yml          # Build WasmJS → deploy to GitHub Pages
├── gradle/
│   └── libs.versions.toml            # Centralised version catalog
├── keystore.properties.template      # Signing config guide (gitignored when filled in)
└── README.md
```

---

## Getting Started

### Prerequisites

| Requirement | Minimum version | Notes |
|-------------|----------------|-------|
| JDK | 17 | 17 or 21 recommended |
| Android Studio | Meerkat (2024.3) | Includes KMP support out of the box |
| Xcode | 15 | Required for iOS targets; macOS only |
| KMP plugin | Latest stable | Install via Android Studio → Plugins |

### Clone

```bash
git clone https://github.com/YOUR_USERNAME/SearchPrice.git
cd SearchPrice
```

### API Key Setup

The app uses a **public** SEFAZ-AL government API token. Add it to `local.properties` (created automatically by Android Studio, or create it manually in the project root):

```properties
# local.properties
sdk.dir=/path/to/your/Android/sdk
APP_TOKEN=YOUR_SEFAZ_API_TOKEN
```

The token is injected at compile time via `BuildConfig.APP_TOKEN`. `local.properties` is gitignored and will never be committed.

---

## Building for Each Platform

### Android

```bash
# Debug APK
./gradlew :composeApp:assembleDebug

# Install on a connected device / emulator
./gradlew :composeApp:installDebug

# Release APK (requires keystore — see Release & Signing below)
./gradlew :composeApp:assembleRelease

# Release AAB — recommended for Google Play
./gradlew :composeApp:bundleRelease
```

### iOS

Open the Xcode project and run on a simulator or physical device:

```bash
open iosApp/iosApp.xcodeproj
```

You can also launch the iOS simulator directly from Android Studio using the KMP plugin run configuration.

### Desktop (JVM)

```bash
# Run in development mode
./gradlew :composeApp:run

# Package as a native installer
./gradlew :composeApp:packageDmg   # macOS  → .dmg
./gradlew :composeApp:packageMsi   # Windows → .msi
./gradlew :composeApp:packageDeb   # Linux  → .deb
```

### Web — WebAssembly (preferred)

Requires a browser with WebAssembly Garbage Collection support (Chrome 119+, Firefox 120+, Safari 17.4+).

```bash
# Development server with hot reload
./gradlew :composeApp:wasmJsBrowserDevelopmentRun

# Production build
# Output: composeApp/build/dist/wasmJs/productionExecutable/
./gradlew :composeApp:wasmJsBrowserDistribution
```

### Web — JavaScript (fallback)

```bash
# Development server
./gradlew :composeApp:jsBrowserDevelopmentRun

# Production build
./gradlew :composeApp:jsBrowserDistribution
```

### Tests

```bash
./gradlew test
```

---

## Android Auto

The app includes an **Android Auto** module built with the [Car App Library](https://developer.android.com/training/cars/apps). It uses the **POI (Points of Interest)** category, giving access to `SearchTemplate` and `PlaceListMapTemplate` without requiring the Navigation category.

### How it works

```
Android Auto connects
  └─► SearchPriceCarAppService  (CarAppService entry point)
        └─► SearchPriceSession.onCreateScreen()
              └─► SearchInputScreen  (driver enters product name)
                    └─► ProductSearchScreen  (results on PlaceListMapTemplate)
```

Turn-by-turn navigation is intentionally delegated to the head unit's installed navigation app — the app only shows store locations on the map.

### Testing with the Desktop Head Unit (DHU)

1. Install the DHU via **Android Studio → SDK Manager → Extras → Android Auto Desktop Head Unit Emulator**.
2. Connect an Android phone via USB with USB Debugging enabled.
3. On the phone, open **Android Auto → Settings**, tap the **Version** label 10 times to enable developer mode, then go to **Developer settings** and enable **Unknown sources** and **Head unit server**.
4. Run the helper script:

```bash
./start-dhu.sh
```

The script forwards port 5277 and launches the DHU automatically.

> **Note on host validation:** `HostValidator.ALLOW_ALL_HOSTS_VALIDATOR` is used for development. Replace it with a certificate-based allowlist before publishing to the Google Play Store.

---

## Configuration

### Build Properties

| Property | File | Description |
|----------|------|-------------|
| `APP_TOKEN` | `local.properties` | SEFAZ-AL API bearer token |
| `sdk.dir` | `local.properties` | Android SDK path (managed by Android Studio) |

### SEFAZ-AL API

The app communicates with:

```
POST https://api.sefaz.al.gov.br/sfz-economiza-alagoas-api/api/public/produto/pesquisar
Authorization: Token YOUR_SEFAZ_API_TOKEN
```

Default search parameters (defined in `RemotePriceDataSource`):

| Parameter | Default | Description |
|-----------|---------|-------------|
| `raio` | `10` | Search radius in kilometres |
| `dias` | `3` | Maximum age of price records in days |
| `latitude` | `-9.6476` | Fallback latitude (centre of Maceió) |
| `longitude` | `-35.7339` | Fallback longitude (centre of Maceió) |

---

## Security

### Network Security

Android's Network Security Config (`res/xml/network_security_config.xml`) replaces the broad `android:usesCleartextTraffic="true"` flag with a targeted allowlist:

```xml
<network-security-config>
    <base-config cleartextTrafficPermitted="false" />
    <domain-config cleartextTrafficPermitted="true">
        <!-- Government API endpoint — does not yet support HTTPS -->
        <domain includeSubdomains="false">api.sefaz.al.gov.br</domain>
    </domain-config>
</network-security-config>
```

All traffic outside this domain must use HTTPS.

### Release Build Hardening

- **R8 minification** (`isMinifyEnabled = true`) and **resource shrinking** (`isShrinkResources = true`) are enabled for release builds.
- A custom `proguard-rules.pro` preserves rules for Ktor, Kotlin Serialization, Koin, and domain model classes while aggressively shrinking everything else.

### Secrets Management

- The API token is stored in `local.properties` (gitignored) and compiled into `BuildConfig` — it is never hard-coded in source files.
- Release keystore credentials are stored in `keystore.properties` (gitignored). A `keystore.properties.template` is committed to guide setup.

---

## Internationalization

All user-visible strings are managed by Compose Multiplatform's resource system, located in `composeApp/src/commonMain/composeResources/`:

| Locale | Directory | Status |
|--------|-----------|--------|
| Portuguese | `values/strings.xml` | Complete (default) |
| English | `values-en/strings.xml` | Complete |

The system selects the correct locale automatically based on device/browser language settings. To add a new language, create `values-<BCP-47-tag>/strings.xml` — no Kotlin changes are needed.

---

## CI/CD

A GitHub Actions workflow (`.github/workflows/deploy-pages.yml`) automatically builds and deploys the **WebAssembly production distribution** to **GitHub Pages** on every push to `master`.

```
push → master
  └─► actions/checkout
  └─► setup-java (Temurin 17)
  └─► gradle/actions/setup-gradle
  └─► ./gradlew composeApp:wasmJsBrowserDistribution
  └─► actions/deploy-pages
        └─► https://YOUR_USERNAME.github.io/SearchPrice/
```

Manual deployments can be triggered from the **Actions** tab using `workflow_dispatch`.

---

## Release & Signing

### 1. Generate a keystore (one-time)

```bash
keytool -genkey -v \
  -keystore searchprice-release.jks \
  -alias searchprice \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

Keep the `.jks` file outside of version control (the root directory is safe — `*.jks` is gitignored).

### 2. Create `keystore.properties`

```bash
cp keystore.properties.template keystore.properties
# Edit the file and fill in your passwords
```

```properties
storeFile=../searchprice-release.jks
storePassword=YOUR_STORE_PASSWORD
keyAlias=searchprice
keyPassword=YOUR_KEY_PASSWORD
```

`keystore.properties` is gitignored and will never be committed. The build script reads it at configuration time and skips signing silently when the file is absent (e.g., on CI for unsigned builds).

### 3. Build a signed release

```bash
# AAB for Google Play (preferred)
./gradlew :composeApp:bundleRelease

# APK for direct distribution / sideloading
./gradlew :composeApp:assembleRelease
```

### Versioning Checklist

Before each release, update the following in `composeApp/build.gradle.kts`:

```kotlin
versionCode = 2          // increment by 1 for every Play Store submission
versionName = "1.1.0"    // semantic version shown to users
```

> **Note on legacy launcher icons:** The mipmap PNG files (mdpi–xxxhdpi) are placeholders used only on Android < 8.0. On Android 8.0+ (API 26+), the vector adaptive icon is used automatically. To regenerate the PNGs, open **Android Studio → Resource Manager → + → Image Asset** and point it at `drawable-v24/ic_launcher_foreground.xml`.

---

## Contributing

Contributions are welcome!

1. Fork the repository and create your branch from `main`.
2. Keep new features in `commonMain` unless they require platform-specific APIs.
3. Write tests in `commonTest` for any new use-case or utility logic.
4. Run `./gradlew test` and ensure all tests pass before opening a PR.
5. Open a pull request with a clear description of what changed and why.

### Code Style

- Kotlin official style (`kotlin.code.style=official` in `gradle.properties`)
- No wildcard imports
- `internal` visibility for anything not exposed across architectural boundaries

---

## License

```
MIT License

Copyright (c) 2024 YOUR_NAME

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

> Built with [Kotlin Multiplatform](https://www.jetbrains.com/kotlin-multiplatform/) and [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/).
> Price data provided by the [SEFAZ-AL Economiza Alagoas](https://www.sefaz.al.gov.br/) public API.
