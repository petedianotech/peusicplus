# PeusicPlus Feature Plan

This roadmap guides development of PeusicPlus as a powerful, offline-first Android music player inspired by Poweramp and Musicolet. Completed work is marked with ✅.

Poweramp contributes the advanced audio-engine, equalizer/DSP, hi-res, visualization, and deep-customization direction; Musicolet contributes the lightweight, offline, multi-queue, folder/library-navigation direction. [web:1][web:2]

## Development principles

- Offline-first: the core player must work without an account or network connection.
- Respect the user: no ads, no tracking, and local data stays on the device.
- Build incrementally: every merged step should produce a runnable app.
- Favor Android platform APIs: MediaStore, MediaSession, ExoPlayer, and foreground services.

## Phase 1 — Native Android foundation

- ✅ Native Kotlin Android application project
- ✅ Gradle Kotlin DSL build configuration
- ✅ Material 3 dark user interface
- ✅ Audio playback with play/pause, next, previous, seek, and completion handling
- ✅ Device audio library scanning through MediaStore
- ✅ Runtime audio permission request
- ✅ Play tracks directly from device storage using content URIs
- ✅ Track list showing title, artist, album, and duration
- ✅ Project feature roadmap and development plan

## Phase 2 — Production playback engine

- ✅ Media3 ExoPlayer playback engine
- ✅ Foreground `MediaSessionService` for background playback
- ✅ System media notification and playback controls
- ✅ Lock-screen and external media-controller integration
- ✅ Media session architecture for Bluetooth, headset, and Android Auto integration
- ✅ Persistent player owned by the service rather than the Activity
- ✅ Queue-based playback using Media3 media items
- ✅ Track transitions and now-playing state updates
- ✅ Audio-focus handling and pause-on-call behavior
- ✅ Becoming-noisy handling for headphone disconnection
- ✅ Persistent queue and playback position across app restarts
- ✅ Shuffle toggle
- ✅ Repeat modes: off, repeat all, and repeat one
- [ ] Gapless playback configuration
- [ ] Crossfade with configurable duration
- [ ] Playback speed and pitch controls
- [ ] Sleep timer
- [ ] Bookmarks and resume positions
- [ ] Wide format validation: MP3, AAC, FLAC, ALAC, WAV, OGG, and Opus

## Phase 3 — Library and navigation

- [ ] Songs, albums, artists, genres, folders, and playlists tabs
- [ ] Album art loading from MediaStore
- [ ] Artist and album detail screens
- [ ] Hierarchical and linear folder browsing
- [ ] Search across songs, albums, artists, and folders
- [ ] Sort options: title, artist, album, duration, date added, and file path
- [ ] Library refresh and storage-location handling
- [ ] Empty, loading, and permission-denied states

## Phase 4 — Queue and playlists

- [ ] Dynamic play queue screen
- [ ] Multiple independent queues
- [ ] Add to queue, play next, and move/remove queue items
- [ ] Queue reordering by drag and drop
- [ ] Create, rename, delete, and reorder playlists
- [ ] Import M3U, M3U8, PLS, and WPL playlists
- [ ] Export playlists
- [ ] Smart playlists based on rules
- [ ] Favorites and recently played

## Phase 5 — Audio enhancement

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

## Phase 6 — Lyrics and metadata

- [ ] Embedded lyrics display
- [ ] Plain-text `.lrc` lyrics support
- [ ] Synchronized lyrics with timing
- [ ] Lyrics editor
- [ ] Built-in tag editor
- [ ] Batch tag editing
- [ ] Album-art editor and embedded artwork support
- [ ] Detailed audio-format and codec information

## Phase 7 — Interface and customization

- [ ] Full Now Playing screen with large artwork
- [ ] Swipe gestures for track changes
- [ ] Customizable home layout
- [ ] Light, dark, AMOLED, and custom themes
- [ ] Accent-color customization
- [ ] Configurable buttons and controls
- [ ] Visualizer: spectrum and waveform
- [ ] Widgets
- [ ] Android Auto support
- [ ] Android TV / large-screen layout

## Phase 8 — Quality and release

- [ ] Unit tests for repository, queue, and playback logic
- [ ] UI tests for core player flows
- [ ] Crash-free release build configuration
- [ ] Accessibility audit and TalkBack support
- [ ] Performance profiling and battery optimization
- [ ] User-facing settings backup and restore
- [ ] Signed release workflow
- [ ] Play Store listing assets and privacy policy

## Current milestone

**Playback reliability complete.** PeusicPlus now handles audio focus, pauses when headphones disconnect, restores the last queue and position, and supports shuffle plus off/all/one repeat modes. The next milestone is library navigation: songs, albums, artists, folders, search, sorting, and album artwork.