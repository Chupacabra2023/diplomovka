# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug

# Run unit tests
./gradlew testDebugUnitTest

# Run a single unit test class
./gradlew testDebugUnitTest --tests "com.example.diplomovka_kotlin.ExampleUnitTest"

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Run lint
./gradlew lint

# Clean build
./gradlew clean
```

## Architecture

Android app (Kotlin, minSdk 26, targetSdk/compileSdk 36) for event discovery and management with location-based services.

**Pattern:** MVVM + Clean Architecture (no Hilt/Dagger — manual dependency wiring)

**Source root:** `app/src/main/java/com/example/diplomovka_kotlin/`

**Package structure:**
- `ui/auth/` — auth fragments + `AuthActivity` (host)
- `ui/map/` — `MapActivity`, `PlaceAutocompleteView`
- `ui/events/` — event creation, detail, and list screens
- `ui/settings/` — settings, profile, logout, help
- `viewmodel/` — ViewModels + factories
- `domain/` — use cases (`LoginUserUseCase`, `RegisterUserUseCase`, `ResetPasswordUseCase`)
- `data/models/` — `Event`, `UserCredentials`
- `data/services/` — `AuthService`, `EventFilterService`, `FilterCriteria`
- `adapters/` — `EventAdapter`, `FaqAdapter`, `InvitationAdapter`

**Auth flow:**
- `MainActivity` checks `FirebaseAuth.currentUser` and routes to `AuthActivity` (unauthenticated) or `MapActivity` (authenticated), then calls `finish()`.
- `AuthActivity` hosts a NavGraph (`nav_auth.xml`) with four fragments: `LandingFragment` (start) → `LoginFragment` → `RegisterFragment` / `ResetPasswordFragment`. Navigation Component handles back-stack.
- After successful login, fragments launch `MapActivity` with `FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK`.

**Map / event flow:**
- `MapActivity` is the main screen — shows events as Google Maps markers centered on Bratislava.
- Event creation: user taps "Vytvoriť udalosť" FAB → moves map pin → taps OK → `EventCreationInformationActivity` opens via `ActivityResultContracts.StartActivityForResult`. On `RESULT_OK`, the returned `Event` is added as a map marker.
- Marker click → `EventDetailActivity`. Edit in detail view → re-opens `EventCreationInformationActivity` and updates in-memory state.
- Filter sheet (`bottom_sheet_filter.xml`) applies `EventFilterService(FilterCriteria(...)).filter(...)` to toggle marker visibility — events are **not yet persisted to Firestore** (in-memory only).

**Event model (`data/models/Event.kt`):**
- `Event` implements `Serializable` and is passed between activities via `Intent.putExtra("event", event)`.
- `toMap()` / `fromMap()` / `fromFirestore()` handle Firestore serialization (dates stored as `Long` epoch millis).
- `toMarkerOptions()` produces a `MarkerOptions` for Google Maps.

**ViewModel patterns (no DI framework):**
- `LoginViewModel(context: Context)` — requires a factory (`LoginViewModelFactory`). Instantiated with `by viewModels { LoginViewModelFactory(requireContext().applicationContext) }`.
- `RegisterViewModel` and `ResetPasswordViewModel` extend `AndroidViewModel(application)` — no custom factory needed.
- When adding a ViewModel that needs a `Context`, use the factory pattern. If it only needs `Application`, extend `AndroidViewModel`.

**Stub screens:** `MyCreatedEventsActivity`, `MyVisitedEventsActivity`, `RecommendedEventsActivity`, and `MyInvitationsActivity` contain hardcoded/simulated data and `// TODO` comments — they are placeholders pending Firestore integration.

## Key Dependencies

- **Firebase Auth** — email/password (with email verification enforcement) + Google Sign-In
- **Firestore** — event persistence (`firebase-firestore-ktx`)
- **Google Maps SDK** (`play-services-maps`) + **Places API** (`places:4.1.0`) — map display and address autocomplete restricted to Slovakia (`setCountries("SK")`)
- **Navigation Component** (`navigation-fragment-ktx`) — auth fragment back-stack
- **Coroutines** — Firebase Tasks wrapped with `kotlinx-coroutines-play-services` (`.await()` extension)
- **ViewBinding** — enabled; all UI accessed via generated binding classes, not `findViewById`
- **Glide + CircleImageView** — profile image loading

## Noteworthy Gotchas

- **Duplicate files (legacy vs. current):** `events/EventFilterService.kt` and `events/EventAdapter.kt` at the root package are old versions — the canonical ones are in `data/services/EventFilterService.kt` and `adapters/EventAdapter.kt`.
- **Category list is hardcoded in two places:** `EventCreationInformationActivity` and `MapActivity`'s filter sheet — keep them in sync when modifying categories.
- **`Event` as `Serializable`:** passed via `Intent` extras. Use `getSerializableExtra("event") as Event` (or `as? Event` for nullable). `fromFirestore()` adds the Firestore document ID to the map before deserializing.
- **`PlaceAutocompleteView`** is a custom `LinearLayout` that self-inflates from `fragment_place_autocomplete.xml`. Add it programmatically (`binding.flAutocomplete.addView(...)`) and set `onPredictionSelected` callback.
