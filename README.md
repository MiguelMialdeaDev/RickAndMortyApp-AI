# Rick & Morty App — AI

Code challenge: lista de personajes de Rick & Morty con paginación, detalle con episodios y animaciones.

## Stack

- Kotlin 2.1 + Jetpack Compose (Material 3)
- Arquitectura modular: `:app` (presentation + DI) + `:domain` (Kotlin puro) + `:data` (Android library)
- MVVM con `StateFlow`
- Hilt (DI) — KSP
- Retrofit + OkHttp + Kotlinx Serialization
- Paging 3 (Compose)
- Coil para imágenes
- Navigation Compose con **shared element transition** lista → detalle
- Tests: JUnit4, MockK, Turbine, Truth, paging-testing

## Módulos

```
:domain   → modelos, repository interface, use cases. Puro Kotlin/JVM, sin Android.
:data     → Retrofit API, DTOs, mappers, PagingSource, repo impl, módulos Hilt.
:app      → UI Compose, ViewModels, navegación, theme, DI Application + MainActivity.
```

## Decisión clave: paginación de 10 items

La API de Rick & Morty (`https://rickandmortyapi.com/api/character`) **devuelve 20 items por página de forma fija** — el parámetro `?count=` no es soportado por el backend.

**Solución implementada**: el `CharacterPagingSource` realiza una llamada por página de API y entrega los resultados a Paging 3 con `pageSize = 10`. Internamente Paging 3 emite a la UI en bloques de 10, manteniendo el contrato del requisito sin lógica de troceo manual en el repositorio.

Resultado: **1 llamada de red cada 2 páginas visibles**, sin estado intermedio en memoria.

Alternativa descartada: trocear los 20 items en el repositorio en dos bloques de 10 con estado interno. Añade complejidad, edge cases en el último bloque, y no aporta ahorro de red real.

## Animaciones

- **Shared element transition** entre item de la lista y header del detalle (Compose 1.7+)
- **Stagger entry** en los items de la lista (entrada escalonada con fade + slide)
- **Animated status color** (alive/dead/unknown) con `animateColorAsState`
- **AnimatedContent** en transiciones de estado (loading → success → error)

## Cómo correr

```bash
./gradlew :app:assembleDebug
./gradlew :app:installDebug
```

Tests:

```bash
./gradlew test
```

## Lo que falta / mejoras

- Caché offline con Room (Mediator de Paging 3)
- Búsqueda por nombre y filtro por status
- Tests instrumentados de Compose
- Baseline Profiles
