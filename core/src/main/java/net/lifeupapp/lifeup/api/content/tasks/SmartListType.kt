package net.lifeupapp.lifeup.api.content.tasks

enum class SmartListType(val pathSegment: String) {
    DAILY("daily"),
    WEEKLY("weekly"),
    MONTHLY("monthly");

    companion object {
        fun fromPathSegment(segment: String?): SmartListType? {
            if (segment.isNullOrEmpty()) {
                return null
            }
            return entries.firstOrNull { it.pathSegment == segment }
        }
    }
}
