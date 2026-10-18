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
- ✅ Create, rename, and delete playlists
- ✅ Add tracks to playlists
- ✅ Open and play playlists
- ✅ Export playlists as M3U
- ✅ Import M3U playlists and match tracks to the device library
- ✅ Play queue screen
- ✅ Queue playback, removal, move up, move down, and clear queue
- ✅ Save and load multiple named queues
- ✅ Add to queue and play next from track long-press menu
- ✅ Favorites with toggle from track long-press menu
- ✅ Recently played history
- ✅ Favorites and Recently Played smart views
- ✅ Recently Added smart view
- ✅ Empty-state messaging for missing music and search results
- ✅ Library refresh action

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

**Phase 2 complete.** PeusicPlus now includes live search, sorting, genres, playlists with M3U import/export, a manageable play queue, multiple saved queues, favorites, recently played, recently added, and empty-state handling. The next milestone is Phase 3: advanced playback controls and audio enhancement.