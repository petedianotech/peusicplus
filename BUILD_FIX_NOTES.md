# Build Fix Notes

## Fix: AndroidX not enabled

The `:app:checkDebugAarMetadata` failure occurred because the project used AndroidX dependencies without enabling AndroidX in Gradle.

Added a root `gradle.properties` file with:

```properties
android.useAndroidX=true
```

This is the required setting for all `androidx.*` dependencies, including Media3, AppCompat, Material, and Lifecycle. The file also includes standard Gradle memory/encoding settings and does not conflict with any existing properties because no root `gradle.properties` existed previously.