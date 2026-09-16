# DuoFold architecture (clean-room)

Original Kotlin/Compose Home app. Package `com.duofold.launcher`.

| Area | Code |
|---|---|
| Entry | `DuoFoldApp`, `MainActivity` |
| Data | `data/AppCatalog`, `FeaturePrefs` |
| Model | `model/HomeLayout`, `LaunchApp`, `HomeDrag`, `LayoutEditing`, `WidgetPlacement` |
| UI | `ui/HomeRoot`, `WidgetSurface`, `AssistChrome`, `StandbySuite`, `DuoFoldTheme` |
| Fold | `fold/PanelKind`, `FoldSensors` |
| Widgets | `widgets/WidgetController` |
| Assist | `assist/AssistService` (optional shade + soft-nav global actions) |
| Lock | `lock/LockActivity`, `UnlockReceiver`, `LockWallpaper` |

## Panel rules

`classifyPanel` uses max-window pixel area / smallest width dp. Expanded dual pane requires non-cover panel and width ≥ 650dp.

## LockSurface

Optional. After `USER_PRESENT` and Keyguard dismiss, may show `LockActivity` with biometric/credential unlock, then returns to Home.

## Assist

Optional AccessibilityService that observes no events and only performs user-triggered global actions (notifications, quick settings, back/home/recents). No Wireless ADB stack.
