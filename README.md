# Treningsvane app 🌱

En Android-applikasjon for å spore og visualisere sunne vaner.

## Teknologier
- Kotlin
- Jetpack Compose
- Room Database
- MVVM-arkitektur
- MPAndroidChart

## Mappestruktur
com.example.healthyhabits/
├── data/
│   └── local/
│       ├── Habit.kt
│       ├── HabitEntry.kt
│       ├── HabitWithEntries.kt
│       ├── HabitDao.kt
│       ├── HabitRepository.kt
│       ├── HabitViewModel.kt
│       └── AppDatabase.kt
└── ui/
    ├── screens/
    │   └── HabitChartScreen.kt
    └── theme/

## Arkitektur
Applikasjonen følger MVVM-arkitekturen:
UI (Compose) → ViewModel → Repository → Room Database

## Arbeidsfordeling
| Oppgave | Ansvarlig |
|---|---|
| UI (Compose) | Christian |
| Navigasjon | Christian |
| ViewModels | Christian |
| Android-prosjektoppsett | Siver |
| Room-database | Siver |
| Repository-lag | Siver |
| Grafbibliotek | Siver |

## Krav
- Android Studio
- Min SDK: 24
- Target SDK: 36
