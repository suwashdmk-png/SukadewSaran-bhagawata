package com.example.ui.viewmodel

data class AudioPlayerState(
    val isPlaying: Boolean = false,
    val currentDayId: Int = 1,
    val currentDayName: String = "प्रथम दिन",
    val currentTopicId: String = "day1_topic1",
    val currentTopicTitle: String = "मंगलाचरण",
    val currentSegment: AudioSegment = AudioSegment.KATHA,
    val progress: Float = 0.0f,
    val currentTimeSec: Int = 0,
    val totalTimeSec: Int = 240, // 4 mins default for demo katha
    val playbackSpeed: Float = 1.0f,
    val sleepTimerMinutesLeft: Int? = null,
    val isTanpuraBgEnabled: Boolean = true,
    val isFluteBgEnabled: Boolean = false,
    val showMiniPlayer: Boolean = false
)

enum class AudioSegment(val label: String, val durationSec: Int) {
    MANTRA("मंगलाचरण मन्त्र (Mantra)", 60),
    KATHA("कथा वाचन (Daily Katha)", 240),
    SHLOKA("श्लोक पाठ (Shloka Recitation)", 90),
    PRAYER("आरती र प्रार्थना (Closing Prayer)", 120)
}
