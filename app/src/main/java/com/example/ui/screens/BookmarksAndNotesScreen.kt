package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Note
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.SaptahaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksAndNotesScreen(
    viewModel: SaptahaViewModel,
    onNavigateToTopic: (String) -> Unit
) {
    val bookmarks by viewModel.bookmarks.collectAsState()
    val notes by viewModel.notes.collectAsState()

    val context = LocalContext.current
    var currentSubTab by remember { mutableStateOf(0) } // 0: Bookmarks, 1: Personal Notes

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tab Row
        TabRow(
            selectedTabIndex = currentSubTab,
            containerColor = WarmCream,
            contentColor = Saffron,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[currentSubTab]),
                    color = Saffron
                )
            }
        ) {
            Tab(
                selected = currentSubTab == 0,
                onClick = { currentSubTab = 0 },
                text = { Text("मनपर्ने सूची (Bookmarks)", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("bookmarks_tab")
            )
            Tab(
                selected = currentSubTab == 1,
                onClick = { currentSubTab = 1 },
                text = { Text("मेरो टिपोटहरू (Notes)", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("notes_tab")
            )
        }

        when (currentSubTab) {
            0 -> {
                // Bookmarks List
                if (bookmarks.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Favorite, contentDescription = "Empty", tint = Saffron.copy(alpha = 0.3f), modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("तपाईंले कुनै पनि अध्याय मनपर्नेमा थप्नुभएको छैन।", color = MutedText, fontSize = 15.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(bookmarks) { bookmark ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectTopic(bookmark.itemId)
                                        onNavigateToTopic(bookmark.itemId)
                                    }
                                    .testTag("bookmark_item_${bookmark.itemId}"),
                                colors = CardDefaults.cardColors(containerColor = WarmCream),
                                border = BorderStroke(1.dp, Gold.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Book, contentDescription = "Topic", tint = Saffron)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(text = bookmark.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DeepText)
                                            Text(text = bookmark.subtitle, fontSize = 13.sp, color = MutedText)
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            viewModel.toggleBookmark(bookmark.itemType, bookmark.itemId, bookmark.title, bookmark.subtitle)
                                            Toast.makeText(context, "हटाउनु भयो", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.testTag("remove_bookmark_${bookmark.itemId}")
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // Personal Notes List
                if (notes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Note, contentDescription = "Empty", tint = Gold.copy(alpha = 0.3f), modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("पढ्ने मोड स्क्रिनबाट टिपोट थप्नुहोस्।", color = MutedText, fontSize = 15.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(notes) { note ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectTopic(note.topicId)
                                        onNavigateToTopic(note.topicId)
                                    },
                                colors = CardDefaults.cardColors(containerColor = WarmCream),
                                border = BorderStroke(1.dp, Gold.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = note.topicTitle, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SaffronDark)
                                        IconButton(
                                            onClick = { viewModel.deleteNote(note.id) },
                                            modifier = Modifier.testTag("delete_note_${note.id}")
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = note.noteText, fontSize = 15.sp, color = DeepText)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
