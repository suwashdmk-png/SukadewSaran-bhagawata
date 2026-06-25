package com.example.data.repository

import com.example.data.local.BookmarkDao
import com.example.data.local.JapCounterDao
import com.example.data.local.NoteDao
import com.example.data.local.SessionTrackerDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BhagawatRepository(
    private val bookmarkDao: BookmarkDao,
    private val noteDao: NoteDao,
    private val japCounterDao: JapCounterDao,
    private val sessionTrackerDao: SessionTrackerDao
) {
    // Static Bhagawat Katha content
    val saptahaDays: List<SaptahaDay> = BhagawatData.saptahaDays

    fun getDayById(dayId: Int): SaptahaDay? {
        return saptahaDays.find { it.id == dayId }
    }

    fun getTopicById(topicId: String): KathaTopic? {
        for (day in saptahaDays) {
            val topic = day.topics.find { it.id == topicId }
            if (topic != null) return topic
        }
        return null
    }

    // Search function for Shlokas, Day, or Topics
    fun searchContent(query: String): List<SearchResult> {
        val results = mutableListOf<SearchResult>()
        if (query.isBlank()) return results

        for (day in saptahaDays) {
            if (day.name.contains(query, ignoreCase = true) || day.description.contains(query, ignoreCase = true)) {
                results.add(SearchResult.DayResult(day))
            }
            for (topic in day.topics) {
                if (topic.title.contains(query, ignoreCase = true) || 
                    topic.description.contains(query, ignoreCase = true) ||
                    topic.explanation.contains(query, ignoreCase = true)) {
                    results.add(SearchResult.TopicResult(day.id, topic))
                }
                for (shloka in topic.shlokas) {
                    if (shloka.originalSanskrit.contains(query, ignoreCase = true) ||
                        shloka.nepaliTranslation.contains(query, ignoreCase = true) ||
                        shloka.nepaliTransliteration.contains(query, ignoreCase = true)) {
                        results.add(SearchResult.ShlokaResult(day.id, topic.id, topic.title, shloka))
                    }
                }
            }
        }
        return results
    }

    // Bookmarks
    val allBookmarks: Flow<List<BookmarkEntity>> = bookmarkDao.getAllBookmarks()

    suspend fun addBookmark(itemType: String, itemId: String, title: String, subtitle: String) {
        bookmarkDao.insertBookmark(
            BookmarkEntity(
                itemType = itemType,
                itemId = itemId,
                title = title,
                subtitle = subtitle
            )
        )
    }

    suspend fun removeBookmark(itemType: String, itemId: String) {
        bookmarkDao.deleteBookmark(itemType, itemId)
    }

    fun isBookmarked(itemType: String, itemId: String): Flow<Boolean> {
        return bookmarkDao.isBookmarked(itemType, itemId)
    }

    // Notes
    val allNotes: Flow<List<NoteEntity>> = noteDao.getAllNotes()

    fun getNotesForTopic(topicId: String): Flow<List<NoteEntity>> = noteDao.getNotesForTopic(topicId)

    suspend fun saveNote(topicId: String, topicTitle: String, noteText: String) {
        noteDao.insertNote(
            NoteEntity(
                topicId = topicId,
                topicTitle = topicTitle,
                noteText = noteText
            )
        )
    }

    suspend fun deleteNote(id: Int) {
        noteDao.deleteNoteById(id)
    }

    // Jap Counter
    val japCounter: Flow<JapCounterEntity> = japCounterDao.getCounterFlow().map { 
        it ?: JapCounterEntity() // Return default if empty
    }

    suspend fun updateJapCount(currentCount: Int, completedMalas: Int, mantraText: String) {
        val existing = japCounterDao.getCounterDirect() ?: JapCounterEntity()
        japCounterDao.insertOrUpdateCounter(
            existing.copy(
                currentCount = currentCount,
                completedMalas = completedMalas,
                mantraText = mantraText,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun resetJapCounter() {
        val existing = japCounterDao.getCounterDirect() ?: JapCounterEntity()
        japCounterDao.insertOrUpdateCounter(
            existing.copy(
                currentCount = 0,
                completedMalas = 0,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    // Session Tracker
    val allCompletedSessions: Flow<List<SessionTrackerEntity>> = sessionTrackerDao.getAllSessions()

    fun getSessionsForDay(dayId: Int): Flow<List<SessionTrackerEntity>> = sessionTrackerDao.getSessionsForDay(dayId)

    suspend fun toggleSessionProgress(dayId: Int, topicId: String, sessionType: String, complete: Boolean) {
        if (complete) {
            sessionTrackerDao.insertSession(
                SessionTrackerEntity(
                    dayId = dayId,
                    topicId = topicId,
                    sessionType = sessionType
                )
            )
        } else {
            sessionTrackerDao.deleteSession(dayId, topicId, sessionType)
        }
    }
}

sealed class SearchResult {
    data class DayResult(val day: SaptahaDay) : SearchResult()
    data class TopicResult(val dayId: Int, val topic: KathaTopic) : SearchResult()
    data class ShlokaResult(val dayId: Int, val topicId: String, val topicTitle: String, val shloka: ShlokaItem) : SearchResult()
}
