package com.example.myottapp.ui.utils

// ─────────────────────────────────────────────────────────────────────
//  RuntimeUtils.kt
//  Place at: ui/utils/RuntimeUtils.kt
//  Converts TMDB runtime (minutes) → human readable string
// ─────────────────────────────────────────────────────────────────────

/**
 * Converts runtime in minutes to "Xh Ym" format.
 *
 * Examples:
 *   181 → "3h 1m"
 *   90  → "1h 30m"
 *   45  → "45m"
 *   0   → ""  (hidden)
 *   null→ ""  (hidden)
 */
fun formatRuntime(minutes: Int?): String {
    if (minutes == null || minutes <= 0) return ""
    val hours = minutes / 60
    val mins  = minutes % 60
    return when {
        hours > 0 && mins > 0 -> "${hours}h ${mins}m"
        hours > 0             -> "${hours}h"
        else                  -> "${mins}m"
    }
}