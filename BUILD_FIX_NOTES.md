# Build Fix: Guava ListenableFuture conflict

The previous failure was caused by duplicate `com.google.common.util.concurrent.ListenableFuture` classes:

- `com.google.guava:guava:33.3.1-android` provides the full Guava class set, including `Futures`.
- Media3’s transitive `androidx.media:media` dependency pulls in `com.google.guava:listenablefuture:1.0`, which contains another copy of `ListenableFuture`.

The build now excludes the standalone `listenablefuture` artifact globally:

```kotlin
configurations.all {
    exclude(group = "com.google.guava", module = "listenablefuture")
}
```

Full Guava remains available for `Futures.immediateFuture(...)` in `PlaybackService`, while Media3 uses the same Guava-provided `ListenableFuture` class.