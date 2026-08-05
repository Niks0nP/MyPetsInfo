# MyPetsInfo

Kotlin Multiplatform application with shared Compose Multiplatform UI for Android and iOS.

## Modules

- `app` — Android application and `MainActivity` entry point.
- `shared` — shared UI and logic for Android and iOS.
- `iosApp` — SwiftUI/Xcode application that hosts the shared Compose UI.

## Build

On Windows:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
```

On macOS, open `iosApp/iosApp.xcodeproj` in Xcode. Its build phase invokes
`:shared:embedAndSignAppleFrameworkForXcode` automatically.
