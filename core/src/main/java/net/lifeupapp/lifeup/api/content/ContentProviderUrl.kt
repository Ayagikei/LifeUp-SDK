package net.lifeupapp.lifeup.api.content

internal object ContentProviderUrl {

    private const val PREFIX = "content://net.sarasarasa.lifeup.provider.api"

    const val TASK = "$PREFIX/tasks"

    const val ACHIEVEMENT_CATEGORIES =
        "$PREFIX/achievement_categories"

    const val TASKS_CATEGORIES =
        "$PREFIX/tasks_categories"

    const val ACHIEVEMENTS = "$PREFIX/achievements"
}