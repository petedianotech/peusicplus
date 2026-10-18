# Build Fix Notes

- `PresetReverb` now uses `setPreset(...)` directly instead of a Kotlin synthetic property assignment.
- Guava is pinned to `33.2.1-android`, a verified release, while the conflicting standalone `listenablefuture` artifact remains excluded.