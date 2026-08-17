## What's new in 1.1.0

> [!IMPORTANT]
> **Early Release / Potential Instability**
>
> This build is provided as a release for testing and early adoption. You may encounter bugs or
> performance issues.

### Features

#### Gradual Volume Increase

* Gradual Volume Increase: Added a new setting to slowly ramp up the alarm and timer volume when
  ringing, providing a gentler wake-up experience.
* Configurable Duration: Choose the duration for the volume ramp-up, ranging from 5 to 60
  seconds.

#### Direct Boot Mode support

* Alarms are rescheduled after a reboot even when the device is still locked.
* Migrated Preferences and Database: Preferences (except ringtones) can still be used even when the
  device is locked.
* Fallback Ringtones: Fallback ringtones were added which can be used even when `content://` is
  locked after reboot.

### Maintenance

* **Refined Settings UI**: Implemented a new unified drawer for volume settings, keeping the
  main settings screen clean and consistent.
* **UI Stability**: Disabled settings now remain visible but grayed out, ensuring a more stable
  layout while clearly showing available options.
* **Localization**: Full German support for all new volume-related settings.
* Enhanced core UI primitives (`GroupRow`, `GroupItem`) with built-in support for disabled states
  and consistent Material 3 styling.
* Refactored `AudioPlayer` core to support dynamic volume animation via `ValueAnimator`.
