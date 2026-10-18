# PeusicPlus Feature Plan

## Build reliability fixes

- ✅ Removed unresolved `R.id.settingsButton` reference; Settings is now opened by long-pressing the PeusicPlus title
- ✅ Fixed `GestureDetector.SimpleOnGestureListener.onFling` override for the non-null `MotionEvent` signature used by compileSdk 35
- ✅ Retained all prior build fixes: launcher icons, Guava dependency, photo-picker API, Android 14 receiver registration, and visualizer permission flow

## Current milestone

**Compile-blocking references fixed.** The next CI run should compile `MainActivity` and `NowPlayingActivity` successfully and produce the installable debug APK artifact.