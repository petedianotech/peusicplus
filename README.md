# PeusicPlus

PeusicPlus is a native Android music player built with Kotlin.

## Features

- Play and pause local audio files
- Previous and next track controls
- Seekable playback progress
- Volume and mute controls
- Playlist-style track list
- Modern Material 3 interface

## Requirements

- Android Studio Ladybug or newer
- Android SDK 35
- Minimum Android 8.0 (API 26)

## Build and run

1. Open the project in Android Studio.
2. Let Gradle sync.
3. Run the `app` configuration on an emulator or device.

## Adding music

Place audio files in `app/src/main/res/raw/`, then add matching entries to `TrackRepository.kt`.

## Project structure

- `app/src/main/java/com/peusicplus/player/` — Kotlin source code
- `app/src/main/res/layout/` — XML layouts
- `app/src/main/res/raw/` — bundled audio files
- `app/src/main/AndroidManifest.xml` — app manifest