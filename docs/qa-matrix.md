# QA matrix — DuoFold 0.5.0

| # | Scenario | Expected |
|---|---|---|
| 1 | Fresh install release APK | Installs; cert is DuoFold Release not Android Debug |
| 2 | Set as Home | Responds to Home |
| 3 | Cover Home | Single grid + right dock |
| 4 | Unfold inner wide | Leading pane + home |
| 5 | Fold animation on/off | Wipe effect follows hinge / disabled |
| 6 | StandBy on + cover landscape | Large clock |
| 7 | LockSurface on | After unlock, optional lock UI + biometric |
| 8 | Launch app from grid/dock/all apps | Starts activity |

```sh
./scripts/gradle.sh :app:testDebugUnitTest
```
