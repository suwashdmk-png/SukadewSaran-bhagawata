package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

// State representation for a katha topic
data class KathaTopic(
    val id: String,
    val dayId: Int,
    val title: String,
    val description: String,
    val shlokas: List<ShlokaItem>,
    val explanation: String,
    val deepMeaning: String,
    val drishtanta: String, // Relevant spiritual stories
    val poetry: String,    // Nepali Devotional Poetry (Gunaratnamalika Style)
    val reflection: String  // Closing reflection
) : Serializable

data class ShlokaItem(
    val id: String,
    val originalSanskrit: String,
    val nepaliTransliteration: String,
    val wordMeaning: String,
    val nepaliTranslation: String
) : Serializable

data class SaptahaDay(
    val id: Int,
    val name: String,
    val description: String,
    val topics: List<KathaTopic>
)

// Room Entities
@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val itemType: String, // "shloka" or "topic"
    val itemId: String,
    val title: String,
    val subtitle: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val topicId: String,
    val topicTitle: String,
    val noteText: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "jap_counters")
data class JapCounterEntity(
    @PrimaryKey val id: Int = 1,
    val currentCount: Int = 0,
    val targetCount: Int = 108,
    val completedMalas: Int = 0,
    val mantraText: String = "ॐ नमो भगवते वासुदेवाय",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "session_trackers")
data class SessionTrackerEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dayId: Int,
    val topicId: String,
    val sessionType: String, // "morning", "afternoon", "evening"
    val isCompleted: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)
