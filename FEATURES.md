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
- ✅ Material 3 dark user interface
- ✅ Device audio library scanning through MediaStore
- ✅ Runtime audio permission request
- ✅ Play tracks directly from device storage using content URIs
- ✅ Media3 ExoPlayer playback engine
- ✅ Foreground `MediaSessionService` for background playback
- ✅ System media notification and playback controls
- ✅ Queue-based playback using Media3 media items
- ✅ Audio-focus handling and pause-on-call behavior
- ✅ Becoming-noisy handling for headphone disconnection
- ✅ Persistent queue and playback position across app restarts
- ✅ Shuffle toggle
- ✅ Repeat modes: off, repeat all, and repeat one
- ✅ Songs, albums, artists, and folders library tabs
- ✅ Album artwork loading from MediaStore
- ✅ Album, artist, and folder drill-down views
- ✅ GitHub Actions workflow that builds an installable debug APK on every push

## Phase 2 — Library, queue, and playlists

- ✅ Dedicated live search across song title, artist, and album
- ✅ Sorting for songs: title, artist, album, duration, date added, and folder
- ✅ Genres tab and genre drill-down
- ✅ Playlists tab
- ✅ Create, rename, delete, open, and play playlists
- ✅ Export playlists as M3U
- ✅ Import M3U playlists and match tracks to the device library
- ✅ Play queue screen with playback, removal, reordering, and clear queue
- ✅ Save and load multiple named queues
- ✅ Add to queue and play next from track long-press menu
- ✅ Favorites, Recently Played, and Recently Added smart views
- ✅ Empty-state messaging and library refresh

## Phase 3 — Advanced playback and sound

- ✅ Gapless playback setting using continuous ExoPlayer queue playback
- ✅ Crossfade with configurable fade duration
- ✅ Playback speed control
- ✅ Pitch control
- ✅ Sleep timer: 15, 30, and 60 minutes plus cancel
- ✅ Bookmarks: save current track position and jump back later
- ✅ Equalizer enable/disable
- ✅ Equalizer presets
- ✅ Custom graphic equalizer band adjustment
- ✅ Bass boost
- ✅ Stereo widening / virtualizer
- ✅ Reverb effects: none, room, hall, and plate presets
- ✅ ReplayGain-style preamp and volume normalization control
- ✅ Balance-aware playback volume control
- ✅ Per-output profile setting: speaker, headphones, Bluetooth, and USB
- ✅ Advanced audio settings screen
- ✅ Audio effects attached to the active ExoPlayer audio session
- ✅ Settings changes applied live to the foreground playback service
- ✅ Direct volume control investigation: implemented as a safe ReplayGain/preamp volume path; bit-perfect USB DAC exclusive access requires device-specific USB audio work

## Phase 4 — Lyrics, metadata, and Now Playing

- [ ] Full Now Playing screen with large artwork
- [ ] Swipe gestures for track changes
- [ ] Embedded lyrics display
- [ ] Plain-text `.lrc` lyrics support
- [ ] Synchronized lyrics with timing
- [ ] Lyrics editor
- [ ] Built-in tag editor
- [ ] Batch tag editing
- [ ] Album-art editor and embedded artwork support
- [ ] Detailed audio-format and codec information

## Phase 5 — Interface, integrations, and release

- [ ] Customizable home layout
- [ ] Light, dark, AMOLED, and custom themes
- [ ] Accent-color customization
- [ ] Configurable buttons and controls
- [ ] Visualizer: spectrum and waveform
- [ ] Widgets
- [ ] Android Auto support
- [ ] Android TV / large-screen layout
- [ ] Unit tests for repository, queue, and playback logic
- [ ] UI tests for core player flows
- [ ] Accessibility audit and TalkBack support
- [ ] Performance profiling and battery optimization
- [ ] User-facing settings backup and restore
- [ ] Signed release workflow
- [ ] Play Store listing assets and privacy policy

## Current milestone

**Phase 3 complete.** PeusicPlus now includes gapless playback, configurable crossfade, speed and pitch, a sleep timer, bookmarks, equalizer presets and custom bands, bass boost, stereo widening, reverb, ReplayGain-style preamp, output profiles, and live-applicable audio settings. The next milestone is Phase 4: full Now Playing, lyrics, and metadata/tag editing.