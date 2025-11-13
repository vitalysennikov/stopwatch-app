package com.laplog.app.model

data class BackupData(
    val version: String,
    val timestamp: Long,
    val sessions: List<BackupSession>
)

data class BackupSession(
    val id: Long,
    val startTime: Long,
    val endTime: Long,
    val totalDuration: Long,
    val comment: String?,
    val laps: List<BackupLap>
)

data class BackupLap(
    val lapNumber: Int,
    val totalTime: Long,
    val lapDuration: Long
)

data class BackupFileInfo(
    val uri: android.net.Uri,
    val name: String,
    val timestamp: Long,
    val size: Long
)
