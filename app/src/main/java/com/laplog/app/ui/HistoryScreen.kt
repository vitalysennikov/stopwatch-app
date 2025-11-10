package com.laplog.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ScreenRotation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.laplog.app.R
import com.laplog.app.data.PreferencesManager
import com.laplog.app.data.database.dao.SessionDao
import com.laplog.app.data.database.entity.SessionEntity
import com.laplog.app.model.SessionWithLaps
import com.laplog.app.viewmodel.HistoryViewModel
import com.laplog.app.viewmodel.HistoryViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    preferencesManager: PreferencesManager,
    sessionDao: SessionDao,
    onExportCsv: (List<SessionWithLaps>) -> Unit,
    onExportJson: (List<SessionWithLaps>) -> Unit
) {
    val viewModel: HistoryViewModel = viewModel(
        factory = HistoryViewModelFactory(preferencesManager, sessionDao)
    )

    val sessions by viewModel.sessions.collectAsState()
    val usedComments by viewModel.usedComments.collectAsState()
    val expandAll by viewModel.expandAll.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var showExportMenu by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.history)) },
                actions = {
                    // Expand/Collapse all toggle
                    IconButton(onClick = { viewModel.toggleExpandAll() }) {
                        Icon(
                            imageVector = if (expandAll) Icons.Default.UnfoldLess else Icons.Default.UnfoldMore,
                            contentDescription = if (expandAll) "Collapse All" else "Expand All"
                        )
                    }
                    IconButton(onClick = { showAboutDialog = true }) {
                        Icon(Icons.Default.Info, contentDescription = "About")
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.export_history)) },
                            onClick = {
                                showMenu = false
                                showExportMenu = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.delete_all_sessions)) },
                            onClick = {
                                showMenu = false
                                showDeleteAllDialog = true
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (sessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_sessions),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(sessions) { sessionWithLaps ->
                    SessionItem(
                        sessionWithLaps = sessionWithLaps,
                        usedComments = usedComments.toList(),
                        onUpdateComment = { comment ->
                            viewModel.updateSessionComment(sessionWithLaps.session.id, comment)
                        },
                        onDelete = { viewModel.deleteSession(sessionWithLaps.session) },
                        onDeleteBefore = { viewModel.deleteSessionsBefore(sessionWithLaps.session.startTime) },
                        formatTime = viewModel::formatTime,
                        totalSessionCount = sessions.size,
                        sessionIndex = sessions.indexOf(sessionWithLaps),
                        expandAll = expandAll
                    )
                    Divider()
                }
            }
        }

        // Delete all dialog
        if (showDeleteAllDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteAllDialog = false },
                title = { Text(stringResource(R.string.delete_confirm_title)) },
                text = { Text(stringResource(R.string.delete_all_sessions_message, sessions.size)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteAllSessions()
                            showDeleteAllDialog = false
                        }
                    ) {
                        Text(stringResource(R.string.delete))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteAllDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }

        // Export menu
        if (showExportMenu) {
            AlertDialog(
                onDismissRequest = { showExportMenu = false },
                title = { Text(stringResource(R.string.export_history)) },
                text = {
                    Column {
                        TextButton(
                            onClick = {
                                onExportCsv(sessions)
                                showExportMenu = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.export_csv))
                        }
                        TextButton(
                            onClick = {
                                onExportJson(sessions)
                                showExportMenu = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.export_json))
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showExportMenu = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }

        // About dialog
        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                title = { Text(stringResource(R.string.app_name)) },
                text = {
                    Column {
                        Text("Version: ${stringResource(R.string.version_name)}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("© 2025 Vitaly Sennikov")
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.about_description),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Toggle buttons:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.AccessTime,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Show/hide milliseconds",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Smartphone,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Keep screen on while running",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.ScreenLockRotation,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Lock screen orientation",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAboutDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionItem(
    sessionWithLaps: SessionWithLaps,
    usedComments: List<String>,
    onUpdateComment: (String) -> Unit,
    onDelete: () -> Unit,
    onDeleteBefore: () -> Unit,
    formatTime: (Long, Boolean) -> String,
    totalSessionCount: Int,
    sessionIndex: Int,
    expandAll: Boolean
) {
    var expanded by remember { mutableStateOf(expandAll) }

    // Sync with global expandAll state
    LaunchedEffect(expandAll) {
        expanded = expandAll
    }
    var showCommentDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDeleteBeforeDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val session = sessionWithLaps.session
    val laps = sessionWithLaps.laps

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()) }
    val dateStr = dateFormat.format(Date(session.startTime))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        onClick = { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (!session.comment.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = session.comment,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.session_duration, formatTime(session.totalDuration, false)),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (laps.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.session_laps, laps.size),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options")
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (session.comment.isNullOrBlank())
                                    stringResource(R.string.add_comment)
                                else
                                    stringResource(R.string.edit_comment)
                            )
                        },
                        onClick = {
                            showMenu = false
                            showCommentDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.delete_session)) },
                        onClick = {
                            showMenu = false
                            showDeleteDialog = true
                        }
                    )
                    if (sessionIndex < totalSessionCount - 1) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.delete_sessions_before)) },
                            onClick = {
                                showMenu = false
                                showDeleteBeforeDialog = true
                            }
                        )
                    }
                }
            }

            // Expanded content with laps
            if (expanded && laps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))

                // Show statistics if there are at least 2 laps
                if (laps.size >= 2) {
                    val lapDurations = laps.map { it.lapDuration }
                    val avgDuration = lapDurations.average().toLong()
                    val minDuration = lapDurations.minOrNull() ?: 0L
                    val maxDuration = lapDurations.maxOrNull() ?: 0L
                    val medianDuration = (minDuration + maxDuration) / 2

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "AVG",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = formatTime(avgDuration, false),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "MEDIAN",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = formatTime(medianDuration, false),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }

                laps.forEachIndexed { index, lap ->
                    // Calculate difference from previous lap
                    val previousLap = if (index > 0) laps[index - 1] else null
                    val difference = if (previousLap != null) {
                        lap.lapDuration - previousLap.lapDuration
                    } else null

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Lap number (left)
                        Text(
                            text = stringResource(R.string.lap_number, lap.lapNumber),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.width(60.dp)
                        )

                        // Lap duration (center, larger) with difference indicator
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = formatTime(lap.lapDuration, false),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                // Difference from previous lap - next to duration
                                if (difference != null) {
                                    val diffSeconds = difference / 1000.0
                                    val sign = if (diffSeconds > 0) "+" else ""
                                    Text(
                                        text = "$sign%.1f s".format(diffSeconds),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (diffSeconds < 0) Color(0xFF4CAF50) // Green for faster laps
                                               else MaterialTheme.colorScheme.error // Red for slower laps
                                    )
                                }
                            }
                        }

                        // Total time (right)
                        Text(
                            text = formatTime(lap.totalTime, false),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.width(80.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.End
                        )
                    }
                }
            }
        }
    }

    // Comment dialog
    if (showCommentDialog) {
        CommentDialog(
            currentComment = session.comment ?: "",
            usedComments = usedComments,
            onDismiss = { showCommentDialog = false },
            onSave = { comment ->
                onUpdateComment(comment)
                showCommentDialog = false
            }
        )
    }

    // Delete dialogs
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_confirm_title)) },
            text = { Text(stringResource(R.string.delete_session_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showDeleteBeforeDialog) {
        val sessionsToDelete = totalSessionCount - sessionIndex
        AlertDialog(
            onDismissRequest = { showDeleteBeforeDialog = false },
            title = { Text(stringResource(R.string.delete_confirm_title)) },
            text = { Text(stringResource(R.string.delete_sessions_before_message, sessionsToDelete)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteBefore()
                        showDeleteBeforeDialog = false
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteBeforeDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentDialog(
    currentComment: String,
    usedComments: List<String>,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var comment by remember { mutableStateOf(currentComment) }
    var showSuggestions by remember { mutableStateOf(false) }

    val filteredSuggestions = remember(comment, usedComments) {
        if (comment.isBlank()) {
            usedComments
        } else {
            usedComments.filter { it.contains(comment, ignoreCase = true) }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (currentComment.isBlank())
                    stringResource(R.string.add_comment)
                else
                    stringResource(R.string.edit_comment)
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = comment,
                    onValueChange = {
                        comment = it
                        showSuggestions = it.isNotBlank() && filteredSuggestions.isNotEmpty()
                    },
                    label = { Text(stringResource(R.string.comment_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (showSuggestions && filteredSuggestions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Suggestions:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    filteredSuggestions.take(3).forEach { suggestion ->
                        TextButton(
                            onClick = {
                                comment = suggestion
                                showSuggestions = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(suggestion, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(comment) }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
