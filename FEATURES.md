# PeusicPlus Feature Plan

This roadmap guides development of PeusicPlus as a powerful, offline-first Android music player inspired by Poweramp and Musicolet. Completed work is marked with ✅.

Poweramp contributes the advanced audio-engine, equalizer/DSP, hi-res, visualization, and deep-customization direction; Musicolet contributes the lightweight, offline, multi-queue, folder/library-navigation direction. [web:1][web:2]

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
- ✅ Track list showing title, artist, album, and duration
- ✅ Media3 ExoPlayer playback engine
- ✅ Foreground `MediaSessionService` for background playback
- ✅ System media notification and playback controls
- ✅ Lock-screen and external media-controller integration
- ✅ Queue-based playback using Media3 media items
- ✅ Audio-focus handling and pause-on-call behavior
- ✅ Becoming-noisy handling for headphone disconnection
- ✅ Persistent queue and playback position across app restarts
- ✅ Shuffle toggle
- ✅ Repeat modes: off, repeat all, and repeat one
- ✅ Songs, albums, artists, and folders library tabs
- ✅ Album artwork loading from MediaStore
- ✅ Album, artist, and folder drill-down views
- ✅ Track artwork in song lists
- ✅ Back navigation from album, artist, and folder views
- ✅ Library search foundation across title, artist, and album
- ✅ GitHub Actions workflow that builds an installable debug APK on every push

## Phase 2 — Library, queue, and playlists

- [ ] Dedicated search screen with live results
- [ ] Sort options: title, artist, album, duration, date added, and file path
- [ ] Genres tab and genre drill-down
- [ ] Playlists tab
- [ ] Dynamic play queue screen
- [ ] Multiple independent queues
- [ ] Add to queue, play next, and move/remove queue items
- [ ] Queue reordering by drag and drop
- [ ] Create, rename, delete, and reorder playlists
- [ ] Import M3U, M3U8, PLS, and WPL playlists
- [ ] Export playlists
- [ ] Smart playlists based on rules
- [ ] Favorites and recently played
- [ ] Library refresh and storage-location handling
- [ ] Empty, loading, and permission-denied states

## Phase 3 — Advanced playback and sound

- [ ] Gapless playback configuration
- [ ] Crossfade with configurable duration
- [ ] Playback speed and pitch controls
- [ ] Sleep timer
- [ ] Bookmarks and resume positions
- [ ] Equalizer UI with presets
- [ ] Graphic equalizer bands
- [ ] Parametric equalizer mode
- [ ] Bass and treble controls
- [ ] Stereo widening and balance
- [ ] Reverb effects
- [ ] Tempo control
- [ ] ReplayGain support
- [ ] Per-output audio settings
- [ ] Direct volume control exploration
- [ ] Hi-res and USB DAC output investigation
- [ ] Wide format validation: MP3, AAC, FLAC, ALAC, WAV, OGG, and Opus

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

**CI and roadmap consolidation complete.** Every push to `main` now builds an installable debug APK with GitHub Actions and uploads it as a workflow artifact. The next development milestone is Phase 2: dedicated search, sorting, genres, playlists, and queue management.