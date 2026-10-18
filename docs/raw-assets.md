# Raw audio assets

Place MP3, AAC, or OGG files in `app/src/main/res/raw/`.

Android resource filenames must contain only lowercase letters, numbers, and underscores, so use names such as:

- `sample_one.mp3`
- `sample_two.mp3`

Then register each track in `app/src/main/java/com/peusicplus/player/TrackRepository.kt` if you are using bundled sample tracks. The main library player loads music from the device MediaStore automatically.