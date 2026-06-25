# 🏠 InteriorAI 3D — Android App

A full-featured interior design Android app built with Kotlin + Jetpack Compose.

## Features

- **3D Room Designer** — Canvas-based 3D room visualization with perspective, top-down, and isometric views
- **AR Furniture Placement** — ARCore-powered augmented reality placement
- **Furniture Catalog** — 18+ furniture items across 10 categories with search & filter
- **Room Measurement** — AR scan or manual dimension entry
- **Color & Material Picker** — Wall paint colors, floor materials, curated palettes
- **AI Design Suggestions** — 5 curated design styles (Japandi, Industrial, Coastal, Mid-Century, Bohemian)
- **Save & Share Designs** — Room project persistence with Room database

## Tech Stack

| Layer | Technology |
|-------|-----------|
| UI | Jetpack Compose + Material 3 |
| 3D Engine | Filament (Google) |
| AR | ARCore |
| Navigation | Compose Navigation |
| Database | Room (SQLite) |
| Architecture | MVVM + StateFlow |
| Camera | CameraX |
| Images | Coil |
| Persistence | DataStore |

## Project Structure

```
app/src/main/java/com/interiordesign3d/
├── MainActivity.kt              # Entry point
├── data/
│   ├── models/Models.kt         # All data models
│   └── repository/
│       ├── Database.kt          # Room DB + DAOs
│       └── FurnitureRepository.kt  # Catalog + color palettes
├── viewmodel/ViewModels.kt      # Home, Catalog, RoomDesigner, Measurement VMs
└── ui/
    ├── Navigation.kt            # NavHost + bottom nav
    ├── theme/Theme.kt           # Colors, typography, shapes
    └── screens/
        ├── HomeScreen.kt        # Dashboard + recent rooms
        ├── CatalogScreen.kt     # Furniture browsing grid
        ├── RoomDesignerScreen.kt # 3D canvas designer
        ├── MeasurementScreen.kt  # AR + manual measurement
        └── OtherScreens.kt      # Color picker, AR view, AI, Saved, Detail
```

## Setup Instructions

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17+
- Android SDK API 26+

### Steps
1. Clone / extract this project
2. Open in Android Studio
3. Update `local.properties` with your SDK path:
   ```
   sdk.dir=/Users/yourname/Library/Android/sdk
   ```
4. Sync Gradle (`File → Sync Project with Gradle Files`)
5. Run on device or emulator (API 26+)

### For AR Features
- Test on a physical device with ARCore support
- Enable camera permission when prompted

## Screens Overview

| Screen | Description |
|--------|-------------|
| 🏠 Home | Dashboard with hero banner, quick actions, AI style picks, room list |
| 🛋️ Catalog | Searchable furniture grid with category filters |
| 📐 Measurement | AR scan or manual room dimension entry |
| 🎨 Room Designer | Interactive 3D room with drag/rotate/scale furniture |
| 📷 AR View | ARCore furniture placement in real space |
| ✨ AI Suggestions | Style cards with one-tap apply |
| 💾 Saved Designs | Project library with share options |
| 🎨 Color Picker | Wall paint, floor materials, curated palettes |
| 🔍 Furniture Detail | Full item view with color options + AR preview |

## License
MIT — free for personal and commercial use.
