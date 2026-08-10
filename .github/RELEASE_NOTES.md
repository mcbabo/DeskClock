## What's new in 1.0.1

> [!IMPORTANT]
> **Early Release / Potential Instability**
>
> This build is provided as a release for testing and early adoption, but it is not yet fully
> stable. You may encounter bugs or performance issues.

### Features

* **Popup Styles**: Added a third interaction model ("Tertiary") featuring a bidirectional swipe
  slider for both alarms and timers.
* **Slider Actions**:
    * **Alarms**: Swipe left to snooze, right to dismiss.
    * **Timers**: Swipe left to add 1 minute, right to stop.
* **"Next Alarm" Display**: The Alarm screen now shows the time remaining until the next scheduled
  alarm (e.g., "Next alarm in 8h 15m").
* **Tactile Feedback**: Added haptic feedback across the app, including swipe actions, the timer
  keypad.
* **Timer Improvements**: Added a quick "1 min" extension button to all timer popup styles.
* **Localization**: Full German support for new styles and interactions.

### Maintenance

* Refactored popup architecture into shared base components to ensure UI consistency.
* Standardized popup buttons and slider logic across the app.
