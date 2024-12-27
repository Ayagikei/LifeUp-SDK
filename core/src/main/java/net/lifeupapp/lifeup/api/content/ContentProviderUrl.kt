package net.lifeupapp.lifeup.api.content

internal object ContentProviderUrl {

    private const val PREFIX = "content://net.sarasarasa.lifeup.provider.api"

    const val TASK = "$PREFIX/tasks"

    const val HISTORY = "$PREFIX/history"

    const val ITEMS = "$PREFIX/items"

    const val SKILLS = "$PREFIX/skills"

    const val INFO = "$PREFIX/info"

    const val FEELINGS = "$PREFIX/feelings"

    const val ACHIEVEMENT_CATEGORIES =
        "$PREFIX/achievement_categories"

    const val TASKS_CATEGORIES =
        "$PREFIX/tasks_categories"

    const val SHOP_CATEGORIES =
        "$PREFIX/items_categories"

    const val ACHIEVEMENTS = "$PREFIX/achievements"

    const val SYNTHESIS = "$PREFIX/synthesis"

    const val SYNTHESIS_CATEGORIES =
        "$PREFIX/synthesis_categories"

    const val POMODORO_RECORDS = "$PREFIX/pomodoro_records"
}
