# PeusicPlus Feature Plan

This roadmap guides development of PeusicPlus as a powerful, offline-first Android music player inspired by Poweramp and Musicolet. Completed work is marked with ✅.

## Development principles

- Offline-first: the core player must work without an account or network connection.
- Respect the user: no ads, no tracking, and local data stays on the device.
- Build incrementally: every merged step should produce a runnable app.
- Favor Android platform APIs: MediaStore, MediaSession, ExoPlayer, and foreground services.
- Keep CI green: every push must produce an installable debug APK.

## Phase 1 — Foundation, playback, and library

- ✅ Native Kotlin Android application project
- ✅ Gradle Kotlin DSL build configuration
- ✅ Material 3 user interface
- ✅ Device audio library scanning through MediaStore
- ✅ Runtime audio permission request
- ✅ Media3 ExoPlayer playback engine
- ✅ Foreground `MediaSessionService` for background playback
- ✅ System media notification and playback controls
- ✅ Queue-based playback using Media3 media items
- ✅ Audio-focus handling and pause-on-call behavior
- ✅ Becoming-noisy handling for headphone disconnection
- ✅ Persistent queue and playback position across app restarts
- ✅ Shuffle and repeat modes
- ✅ Songs, albums, artists, and folders library tabs
- ✅ Album artwork loading from MediaStore
- ✅ Adaptive launcher icon resources
- ✅ GitHub Actions workflow that builds an installable debug APK on every push

## Phase 2 — Library, queue, and playlists

- ✅ Live search across song title, artist, and album
- ✅ Sorting: title, artist, album, duration, date added, and folder
- ✅ Genres, playlists, favorites, recently played, and recently added
- ✅ Playlist creation and M3U import/export
- ✅ Queue screen with playback, removal, reordering, clearing, and saved queues
- ✅ Add to queue and play next
- ✅ Empty-state messaging and library refresh

## Phase 3 — Advanced playback and sound

- ✅ Gapless playback setting
- ✅ Configurable crossfade
- ✅ Playback speed and pitch controls
- ✅ Sleep timer
- ✅ Bookmarks
- ✅ Equalizer presets and custom bands
- ✅ Bass boost, stereo widening, and reverb
- ✅ ReplayGain-style preamp
- ✅ Output profiles
- ✅ Live audio effects through ExoPlayer’s audio session

## Phase 4 — Lyrics, metadata, and Now Playing

- ✅ Full Now Playing screen with large artwork
- ✅ Swipe gestures for track changes
- ✅ Embedded lyrics display
- ✅ Synchronized `.lrc` lyrics parsing and highlighting
- ✅ Lyrics editor
- ✅ Tag editor and batch album metadata
- ✅ Album-art override with the photo picker
- ✅ Audio-format and codec information viewer
- ✅ Metadata overrides applied to playback metadata

## Phase 5 — Interface, integrations, quality, and release

- ✅ Dark, AMOLED, and light themes
- ✅ Accent-color preference used by the visualizer
- ✅ Waveform visualizer attached to active playback
- ✅ Visualizer enable/disable setting
- ✅ RECORD_AUDIO runtime permission request for visualizer operation
- ✅ Home-screen widget with one-tap access to the player
- ✅ Android Auto compatibility through the foreground Media3 media session
- ✅ Unit tests for synchronized-lyrics parsing and M3U parsing
- ✅ Accessibility content descriptions for player controls
- ✅ Privacy policy documenting local-only data handling
- ✅ Settings backup and restore via Android file picker
- ✅ Manual GitHub Actions workflow for an unsigned release APK
- ✅ Debug APK workflow retained for installable builds on every push

## Build reliability fixes

- ✅ Removed invalid getter-only `var equalizerEnabled` property that prevented Kotlin compilation
- ✅ Added missing adaptive launcher icon resources required by `@mipmap/ic_launcher`
- ✅ Added explicit Guava dependency for Media3 `ListenableFuture` APIs
- ✅ Fixed `PickVisualMediaRequest` construction for the Android photo picker
- ✅ Registered the settings broadcast receiver with `RECEIVER_NOT_EXPORTED` for Android 14 compatibility
- ✅ Enabled default-value stubs for local unit tests
- ✅ Added safe handling for unavailable media duration during crossfade

## Release readiness

- ✅ Installable debug APK generated automatically on every push
- ✅ Manual release APK build workflow
- ✅ Privacy policy
- ✅ Core unit tests
- [ ] Signed release workflow using repository secrets
- [ ] Full UI-test suite
- [ ] Performance and battery profiling report
- [ ] Play Store listing assets

## Current milestone

**Build blockers fixed and Phase 5 complete.** The project now includes launcher icons, working visualizer permission flow, Android 14-safe receiver registration, valid photo-picker integration, Guava support for Media3 callbacks, and stable debug/release CI workflows.