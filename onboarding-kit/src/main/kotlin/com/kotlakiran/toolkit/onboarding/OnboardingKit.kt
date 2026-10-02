package com.kotlakiran.toolkit.onboarding

import android.content.Context

data class GoalOption(val id: String, val label: String, val emoji: String = "")

/**
 * Persists the user's picked goals so recommendation logic and later
 * personalization survive reinstall-less restarts and cold starts.
 */
object OnboardingPrefs {
    private const val FILE = "toolkit_onboarding"
    private const val KEY_GOALS = "goals"
    private const val KEY_DONE = "done_at"

    private fun prefs(context: Context) = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun saveGoals(context: Context, goals: Set<String>) {
        prefs(context).edit()
            .putStringSet(KEY_GOALS, goals)
            .putLong(KEY_DONE, System.currentTimeMillis())
            .apply()
    }

    fun goals(context: Context): Set<String> =
        prefs(context).getStringSet(KEY_GOALS, emptySet()) ?: emptySet()

    fun isDone(context: Context): Boolean = prefs(context).getLong(KEY_DONE, 0L) > 0L

    fun reset(context: Context) {
        prefs(context).edit().clear().apply()
    }
}

/**
 * Maps the user's picked goals to a ranked list of in-app items
 * (journeys, loan types, preset habits — whatever the app passes in).
 */
class GoalRecommender<T>(private val mapping: Map<String, List<T>>) {

    fun recommend(goals: Set<String>): List<T> {
        val seen = LinkedHashSet<T>()
        for (goal in goals) {
            mapping[goal]?.let(seen::addAll)
        }
        return seen.toList()
    }

    fun primary(goals: Set<String>): T? = recommend(goals).firstOrNull()
}
