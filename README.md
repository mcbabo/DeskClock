# DeskClock

A modern rewrite of the Android clock utility, following the **Material 3 Expressive** design
system. This project aims to replace the aging AOSP `com.android.deskclock` with a clean, stable,
and privacy-focused alternative.

#### Current issue disclaimer

> [!IMPORTANT]  
> AlarmPopUpScreen showed up much later than the ringtone and vibration triggers. (should be fixed
> by
> now)

## Features

- **Alarms:** Full scheduling, custom ringtones, and vibration patterns. Uses a secure full-screen
  intent for alerts.
- **World Clock:** Track time across multiple cities with an integrated timezone search.
- **Timer:** Reliable background countdowns with "negative count-up" after completion.
- **Stopwatch:** High-precision lap timing with battery-efficient status bar updates.
- **Material You:** Full support for Dynamic Colors and Edge-to-Edge layouts.

## Technical Details

This app is built with modern Android development in mind:

- **UI:** 100% Jetpack Compose.
- **Architecture:** MVI-style pattern with clean separation between UI and Data layers.
- **Navigation:** Adopts the latest **Navigation 3** library.
- **Lifecycle:** Services are Android 14+ compliant, using specific foreground service types for
  reliability.
- **DI:** **Koin** instead of Hilt (why not).

## Screenshots

*(Coming soon - The app uses the latest Material 3 Expressive components)*

## Building

The project is ready to build with Android Studio Ladybug (or newer).

```bash
./gradlew assembleDebug
```

## Contributing

Contributions are welcomed!
Open bug reports, feature requests, questions, ideas to improve :)

> If you want to contribute to the codebase create a pull request and make sure you follow the
> guidelines

See: [CONTRIBUTING](CONTRIBUTING.md)

This project is currently a one-man job, but to become a fully functional replacement for the stock
AOSP clock, it needs a team.

If you like **GrapheneOS** and want to give something back by building a high-quality system
utility, this is a great place to start. Contributions in the form of code, design, localization, or
bug reports are all welcome.

## Authors

- [**mcbabo**](https://github.com/mcbabo)

## License

MIT License - see the [LICENSE](LICENSE) file for details.
