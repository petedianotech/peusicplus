# PeusicPlus Feature Plan

This roadmap guides development of PeusicPlus as a powerful, offline-first Android music player inspired by Poweramp and Musicolet. Completed work is marked with ✅.

Poweramp contributes the advanced audio-engine, equalizer/DSP, hi-res, visualization, and deep-customization direction; Musicolet contributes the lightweight, offline, multi-queue, folder/library-navigation direction. [web:1][web:2]

## Development principles

- Offline-first: the core player must work without an account or network connection.
- Respect the user: no ads, no tracking, and local data stays on the device.
- Build incrementally: every merged step should produce a runnable app.
- Favor Android platform APIs: MediaStore, MediaSession, MediaCodec, and foreground services.

## Phase 1 — Native Android foundation

- ✅ Native Kotlin Android application project
- ✅ Gradle Kotlin DSL build configuration
- ✅ Material 3 dark user interface
- ✅ Audio playback with play/pause, next, previous, seek, and completion handling
- ✅ Basic bundled-track playlist interface
- ✅ Device audio library scanning through MediaStore
- ✅ Runtime audio permission request
- ✅ Play tracks directly from device storage using content URIs
- ✅ Track list showing title, artist, album, and duration
- ✅ Project feature roadmap and development plan

## Phase 2 — Library and navigation

- [ ] Songs, albums, artists, genres, folders, and playlists tabs
- [ ] Album art loading from MediaStore
- [ ] Artist and album detail screens
- [ ] Hierarchical and linear folder browsing
- [ ] Search across songs, albums, artists, and folders
- [ ] Sort options: title, artist, album, duration, date added, and file path
- [ ] Library refresh and storage-location handling
- [ ] Empty, loading, and permission-denied states

## Phase 3 — Playback engine

- [ ] Foreground `MediaSessionService` for background playback
- [ ] Media notification with play/pause, next, previous, and seek actions
- [ ] Lock-screen and Bluetooth/headset controls
- [ ] Audio-focus handling and pause-on-call behavior
- [ ] Becoming-noisy handling for headphone disconnection
- [ ] Persistent queue across app restarts
- [ ] Shuffle and repeat: off, repeat one, repeat all
- [ ] Gapless playback
- [ ] Crossfade with configurable duration
- [ ] Playback speed and pitch controls
- [ ] Sleep timer
- [ ] Bookmarks and resume positions
- [ ] Wide format support: MP3, AAC, FLAC, ALAC, WAV, OGG, Opus, and more where platform codecs permit

## Phase 4 — Queue and playlists

- [ ] Dynamic play queue
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

- [ ] Now Playing screen with large artwork
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

**Phase 1 complete.** The app can request audio permission, scan the device MediaStore, list local tracks, and play selected tracks through Android's media playback APIs. [web:1][web:2]