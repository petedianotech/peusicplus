package com.peusicplus.player

object TrackRepository {

    // Add audio files to app/src/main/res/raw/ and register them here.
    val tracks = listOf(
        Track(
            title = "Sample Track One",
            artist = "PeusicPlus",
            resourceName = "sample_one"
        ),
        Track(
            title = "Sample Track Two",
            artist = "PeusicPlus",
            resourceName = "sample_two"
        )
    )
}