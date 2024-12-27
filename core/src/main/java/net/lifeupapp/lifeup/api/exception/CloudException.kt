package net.lifeupapp.lifeup.api.exception

open class CloudException(
    val errorCode: Int,
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause) {
    companion object {
        const val ERROR_CODE_BASE = 10000

        const val ERROR_NULL_CURSOR = ERROR_CODE_BASE + 1
    }
}

