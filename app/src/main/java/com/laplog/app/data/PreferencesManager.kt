package com.laplog.app.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    var showMilliseconds: Boolean
        get() = prefs.getBoolean(KEY_SHOW_MILLISECONDS, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_MILLISECONDS, value).apply()

    var keepScreenOn: Boolean
        get() = prefs.getBoolean(KEY_KEEP_SCREEN_ON, true)
        set(value) = prefs.edit().putBoolean(KEY_KEEP_SCREEN_ON, value).apply()

    var usedComments: Set<String>
        get() = prefs.getStringSet(KEY_USED_COMMENTS, emptySet()) ?: emptySet()
        set(value) = prefs.edit().putStringSet(KEY_USED_COMMENTS, value).apply()

    var lockOrientation: Boolean
        get() = prefs.getBoolean(KEY_LOCK_ORIENTATION, false)
        set(value) = prefs.edit().putBoolean(KEY_LOCK_ORIENTATION, value).apply()

    var currentComment: String
        get() = prefs.getString(KEY_CURRENT_COMMENT, "") ?: ""
        set(value) = prefs.edit().putString(KEY_CURRENT_COMMENT, value).apply()

    var showMillisecondsInHistory: Boolean
        get() = prefs.getBoolean(KEY_SHOW_MILLISECONDS_IN_HISTORY, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_MILLISECONDS_IN_HISTORY, value).apply()

    var invertLapColors: Boolean
        get() = prefs.getBoolean(KEY_INVERT_LAP_COLORS, false)
        set(value) = prefs.edit().putBoolean(KEY_INVERT_LAP_COLORS, value).apply()

    companion object {
        private const val PREFS_NAME = "laplog_preferences"
        private const val KEY_SHOW_MILLISECONDS = "show_milliseconds"
        private const val KEY_KEEP_SCREEN_ON = "keep_screen_on"
        private const val KEY_USED_COMMENTS = "used_comments"
        private const val KEY_LOCK_ORIENTATION = "lock_orientation"
        private const val KEY_CURRENT_COMMENT = "current_comment"
        private const val KEY_SHOW_MILLISECONDS_IN_HISTORY = "show_milliseconds_in_history"
        private const val KEY_INVERT_LAP_COLORS = "invert_lap_colors"
    }
}
