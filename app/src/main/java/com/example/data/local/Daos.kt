package com.example.data.local

import androidx.room.*
import com.example.data.model.BookmarkEntity
import com.example.data.model.JapCounterEntity
import com.example.data.model.NoteEntity
import com.example.data.model.SessionTrackerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE itemType = :itemType AND itemId = :itemId")
    suspend fun deleteBookmark(itemType: String, itemId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE itemType = :itemType AND itemId = :itemId)")
    fun isBookmarked(itemType: String, itemId: String): Flow<Boolean>
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE topicId = :topicId ORDER BY timestamp DESC")
    fun getNotesForTopic(topicId: String): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: Int)
}

@Dao
interface JapCounterDao {
    @Query("SELECT * FROM jap_counters WHERE id = 1")
    fun getCounterFlow(): Flow<JapCounterEntity?>

    @Query("SELECT * FROM jap_counters WHERE id = 1")
    suspend fun getCounterDirect(): JapCounterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCounter(counter: JapCounterEntity)
}

@Dao
interface SessionTrackerDao {
    @Query("SELECT * FROM session_trackers ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<SessionTrackerEntity>>

    @Query("SELECT * FROM session_trackers WHERE dayId = :dayId")
    fun getSessionsForDay(dayId: Int): Flow<List<SessionTrackerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionTrackerEntity)

    @Query("DELETE FROM session_trackers WHERE dayId = :dayId AND topicId = :topicId AND sessionType = :sessionType")
    suspend fun deleteSession(dayId: Int, topicId: String, sessionType: String)
}
