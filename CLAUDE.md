# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**SearchPrice** is a Kotlin Multiplatform (KMP) price comparison app for Brazilian consumers in Alagoas state. It queries the government SEFAZ retail price database and displays results sorted by price or distance. Targets: Android, iOS, Web (WASM/JS), and Desktop (JVM).

## Build Commands

```bash
# Android
./gradlew :composeApp:assembleDebug

# Desktop (JVM)
./gradlew :composeApp:run

# Web (WebAssembly - preferred)
./gradlew :composeApp:wasmJsBrowserDevelopmentRun

# Web (JavaScript fallback)
./gradlew :composeApp:jsBrowserDevelopmentRun

# Run tests
./gradlew test

# iOS - open iosApp/ in Xcode
```

## Architecture

Clean Architecture with MVI (Model-View-Intent) pattern. All shared code lives in `composeApp/src/commonMain/`.

**Data flow**: UI sends `Intent` → `SearchViewModel.handleIntent()` → `SearchProductsUseCase` → `PriceRepositoryImpl` → `RemotePriceDataSource` (Ktor) → mapped to `Product` entities → sorted → `State` emitted → UI recomposes.

### Layers

**Domain** (`domain/`) — pure Kotlin, no framework dependencies:
- `model/Product.kt` — clean entity (no nullable fields)
- `repository/PriceRepository.kt` — interface
- `usecase/SearchProductsUseCase.kt` — fetches + sorts results
- `usecase/GetLocationUseCase.kt` — provides default Maceio coordinates
- `util/LocationUtils.kt` — Haversine formula, `DEFAULT_LATITUDE/LONGITUDE`
- `util/SortOption.kt` — enum: PRICE, DISTANCE

**Data** (`data/`) — infrastructure:
- `model/` — `PriceSearchRequest` / `PriceSearchResponse` DTOs (22 nullable fields from SEFAZ API)
- `mapper/PriceMapper.kt` — `PriceSearchResponse.toProduct()` extension
- `source/RemotePriceDataSource.kt` — Ktor HTTP client, SEFAZ API call
- `repository/PriceRepositoryImpl.kt` — implements `PriceRepository`

**Presentation** (`presentation/`):
- `contract/SearchContract.kt` — MVI contract:
  - `State`: query, isLoading, products, sortOption, error, hasSearched
  - `Intent`: UpdateQuery, UpdateLocation, ChangeSortOption, PerformSearch, RetrySearch
  - `Effect`: ShowSnackbar, NavigateBack
- `viewmodel/SearchViewModel.kt` — `handleIntent()` entry point, `StateFlow<State>`, `Channel<Effect>`; caches unsorted results for re-sort without re-fetch
- `ui/SearchPriceScreen.kt` — thin orchestrator: collects state/effects, dispatches intents, hosts `RequestLocationEffect`
- `ui/SearchBar.kt` (`PriceSearchBar`) — search input + button
- `ui/FilterChips.kt` (`SortFilterChips`) — sort option chips + result count
- `ui/ProductList.kt` — switches between Loading / Error / Empty / Initial / Results states
- `ui/ProductItemCard.kt` — individual product card; color tokens defined as `internal` vals here

**Location** (`location/RequestLocationEffect.kt`): `expect`/`actual` composable — Android uses LocationManager + permission; iOS uses CoreLocation; JVM/JS/WasmJS use default coordinates.

### Platform-specific HTTP engines
- Android + JVM: `ktor-client-okhttp`
- iOS: `ktor-client-darwin`
- Web (JS/Wasm): `ktor-client-js`

## Key Constants

- **API token**: `95da6fd760888ae09160bfdf1d8cab5acc307716` (public government API)
- **Default search radius**: 10 km (`raio`)
- **Default search window**: 3 days (`dias`)
- **Min Android SDK**: 24, Target: 36

## CI/CD

GitHub Actions (`.github/workflows/deploy-pages.yml`) builds the WASM distribution and deploys to GitHub Pages on push to `master`.