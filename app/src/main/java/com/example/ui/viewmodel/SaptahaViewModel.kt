package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.BhagawatRepository
import com.example.data.repository.SearchResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SaptahaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BhagawatRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = BhagawatRepository(
            bookmarkDao = database.bookmarkDao(),
            noteDao = database.noteDao(),
            japCounterDao = database.japCounterDao(),
            sessionTrackerDao = database.sessionTrackerDao()
        )
    }

    // Static content
    val saptahaDays = repository.saptahaDays

    // UI Configuration / Elderly Accessibility
    private val _readingFontScale = MutableStateFlow(1.2f) // Defaults to 1.2x larger for elder friendliness
    val readingFontScale: StateFlow<Float> = _readingFontScale.asStateFlow()

    private val _readingTheme = MutableStateFlow("light_ivory") // "light_ivory", "warm_saffron", "night_mode"
    val readingTheme: StateFlow<String> = _readingTheme.asStateFlow()

    fun updateFontScale(scale: Float) {
        _readingFontScale.value = scale
    }

    fun updateReadingTheme(theme: String) {
        _readingTheme.value = theme
    }

    // Navigation State holding current selected day / topic
    private val _selectedDay = MutableStateFlow<SaptahaDay>(saptahaDays.first())
    val selectedDay: StateFlow<SaptahaDay> = _selectedDay.asStateFlow()

    private val _selectedTopic = MutableStateFlow<KathaTopic?>(null)
    val selectedTopic: StateFlow<KathaTopic?> = _selectedTopic.asStateFlow()

    fun selectDay(dayId: Int) {
        saptahaDays.find { it.id == dayId }?.let {
            _selectedDay.value = it
        }
    }

    fun selectTopic(topicId: String) {
        val topic = repository.getTopicById(topicId)
        _selectedTopic.value = topic
        if (topic != null) {
            // Auto update audio player track when user clicks a topic
            setAudioTrack(topic.dayId, topic.id, topic.title)
        }
    }

    // Room DB State Flows
    val bookmarks: StateFlow<List<BookmarkEntity>> = repository.allBookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<NoteEntity>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val japCounter: StateFlow<JapCounterEntity> = repository.japCounter
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), JapCounterEntity())

    val completedSessions: StateFlow<List<SessionTrackerEntity>> = repository.allCompletedSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Check bookmark state for currently selected topic/shloka dynamically
    fun isTopicBookmarked(topicId: String): Flow<Boolean> {
        return repository.isBookmarked("topic", topicId)
    }

    fun isShlokaBookmarked(shlokaId: String): Flow<Boolean> {
        return repository.isBookmarked("shloka", shlokaId)
    }

    fun toggleBookmark(itemType: String, itemId: String, title: String, subtitle: String) {
        viewModelScope.launch {
            val isBookmarkedFlow = repository.isBookmarked(itemType, itemId).first()
            if (isBookmarkedFlow) {
                repository.removeBookmark(itemType, itemId)
            } else {
                repository.addBookmark(itemType, itemId, title, subtitle)
            }
        }
    }

    // Personal Notes
    fun saveNote(topicId: String, topicTitle: String, noteText: String) {
        viewModelScope.launch {
            repository.saveNote(topicId, topicTitle, noteText)
        }
    }

    fun deleteNote(noteId: Int) {
        viewModelScope.launch {
            repository.deleteNote(noteId)
        }
    }

    // Session Tracker (Morning, Afternoon, Evening schedule)
    fun toggleSessionProgress(dayId: Int, topicId: String, sessionType: String, complete: Boolean) {
        viewModelScope.launch {
            repository.toggleSessionProgress(dayId, topicId, sessionType, complete)
        }
    }

    // Search Engine
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchResults: StateFlow<List<SearchResult>> = _searchResults.asStateFlow()

    fun search(query: String) {
        _searchQuery.value = query
        _searchResults.value = repository.searchContent(query)
    }

    // --- Jap Counter Logic (with target matching and mala completion) ---
    fun incrementJap() {
        viewModelScope.launch {
            val current = japCounter.value
            var nextCount = current.currentCount + 1
            var nextMalas = current.completedMalas

            if (nextCount >= current.targetCount) {
                nextCount = 0
                nextMalas += 1 // Complete one Mala (108 counts)
            }

            repository.updateJapCount(nextCount, nextMalas, current.mantraText)
        }
    }

    fun resetJap() {
        viewModelScope.launch {
            repository.resetJapCounter()
        }
    }

    fun setMantra(mantra: String) {
        viewModelScope.launch {
            val current = japCounter.value
            repository.updateJapCount(current.currentCount, current.completedMalas, mantra)
        }
    }

    // --- Simulated Shankha & Bell Effects ---
    private val _shankhaAnimationPlaying = MutableStateFlow(false)
    val shankhaAnimationPlaying: StateFlow<Boolean> = _shankhaAnimationPlaying.asStateFlow()

    private val _bellAnimationPlaying = MutableStateFlow(false)
    val bellAnimationPlaying: StateFlow<Boolean> = _bellAnimationPlaying.asStateFlow()

    fun playVirtualShankha() {
        viewModelScope.launch {
            _shankhaAnimationPlaying.value = true
            delay(3000) // Shankha sound duration 3s
            _shankhaAnimationPlaying.value = false
        }
    }

    fun playVirtualBell() {
        viewModelScope.launch {
            _bellAnimationPlaying.value = true
            delay(1000) // Bell sound duration 1s
            _bellAnimationPlaying.value = false
        }
    }

    // --- Audio Player Simulation Engine ---
    private val _audioState = MutableStateFlow(AudioPlayerState())
    val audioState: StateFlow<AudioPlayerState> = _audioState.asStateFlow()

    private var audioJob: Job? = null
    private var sleepTimerJob: Job? = null

    private fun setAudioTrack(dayId: Int, topicId: String, topicTitle: String) {
        val dayName = saptahaDays.find { it.id == dayId }?.name ?: "प्रथम दिन"
        _audioState.update {
            it.copy(
                currentDayId = dayId,
                currentDayName = dayName,
                currentTopicId = topicId,
                currentTopicTitle = topicTitle,
                currentTimeSec = 0,
                progress = 0.0f,
                totalTimeSec = it.currentSegment.durationSec,
                showMiniPlayer = true
            )
        }
    }

    fun togglePlayPause() {
        val isPlayingNow = _audioState.value.isPlaying
        if (isPlayingNow) {
            pauseAudio()
        } else {
            playAudio()
        }
    }

    fun playAudio() {
        _audioState.update { it.copy(isPlaying = true, showMiniPlayer = true) }
        startAudioTicker()
    }

    fun pauseAudio() {
        _audioState.update { it.copy(isPlaying = false) }
        stopAudioTicker()
    }

    fun selectAudioSegment(segment: AudioSegment) {
        _audioState.update {
            it.copy(
                currentSegment = segment,
                currentTimeSec = 0,
                progress = 0.0f,
                totalTimeSec = segment.durationSec
            )
        }
    }

    fun changePlaybackSpeed(speed: Float) {
        _audioState.update { it.copy(playbackSpeed = speed) }
    }

    fun toggleTanpura(enabled: Boolean) {
        _audioState.update { it.copy(isTanpuraBgEnabled = enabled) }
    }

    fun toggleFlute(enabled: Boolean) {
        _audioState.update { it.copy(isFluteBgEnabled = enabled) }
    }

    fun seekTo(progress: Float) {
        val total = _audioState.value.totalTimeSec
        val targetSec = (total * progress).toInt()
        _audioState.update {
            it.copy(
                progress = progress,
                currentTimeSec = targetSec
            )
        }
    }

    // Sleep Timer Support
    fun setSleepTimer(minutes: Int?) {
        _audioState.update { it.copy(sleepTimerMinutesLeft = minutes) }
        if (minutes != null) {
            startSleepTimerTicker()
        } else {
            stopSleepTimerTicker()
        }
    }

    private fun startAudioTicker() {
        audioJob?.cancel()
        audioJob = viewModelScope.launch {
            while (_audioState.value.isPlaying) {
                // Adjust delay according to playback speed
                val delayTime = (1000 / _audioState.value.playbackSpeed).toLong()
                delay(delayTime)

                _audioState.update { state ->
                    val nextSec = state.currentTimeSec + 1
                    val nextProgress = nextSec.toFloat() / state.totalTimeSec.toFloat()

                    if (nextSec >= state.totalTimeSec) {
                        // Switch to next segment automatically
                        val segments = AudioSegment.values()
                        val currentIdx = segments.indexOf(state.currentSegment)
                        if (currentIdx < segments.size - 1) {
                            val nextSegment = segments[currentIdx + 1]
                            state.copy(
                                currentSegment = nextSegment,
                                currentTimeSec = 0,
                                progress = 0.0f,
                                totalTimeSec = nextSegment.durationSec
                            )
                        } else {
                            // Finished all segments
                            state.copy(
                                isPlaying = false,
                                currentTimeSec = 0,
                                progress = 0.0f
                            )
                        }
                    } else {
                        state.copy(
                            currentTimeSec = nextSec,
                            progress = nextProgress
                        )
                    }
                }

                // If playback stopped, break loop
                if (!_audioState.value.isPlaying) {
                    break
                }
            }
        }
    }

    private fun stopAudioTicker() {
        audioJob?.cancel()
        audioJob = null
    }

    private fun startSleepTimerTicker() {
        sleepTimerJob?.cancel()
        sleepTimerJob = viewModelScope.launch {
            while (_audioState.value.sleepTimerMinutesLeft != null) {
                // Ticks down every 60 seconds of real-time wait
                delay(60000)
                _audioState.update { state ->
                    val left = state.sleepTimerMinutesLeft
                    if (left != null) {
                        if (left <= 1) {
                            // Timer triggered, pause audio
                            pauseAudio()
                            state.copy(sleepTimerMinutesLeft = null)
                        } else {
                            state.copy(sleepTimerMinutesLeft = left - 1)
                        }
                    } else {
                        state
                    }
                }
            }
        }
    }

    private fun stopSleepTimerTicker() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopAudioTicker()
        stopSleepTimerTicker()
    }
}
