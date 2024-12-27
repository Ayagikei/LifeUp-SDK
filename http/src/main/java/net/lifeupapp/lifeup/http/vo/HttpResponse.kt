package net.lifeupapp.lifeup.http.vo

import kotlinx.serialization.Serializable
import net.lifeupapp.lifeup.api.exception.CloudException

@Serializable
data class HttpResponse<T>(
    val code: Int,
    val message: String,
    val data: T?
) {
    companion object {
        const val SUCCESS = 200
        const val ERROR = 500

        fun <T> success(data: T?): HttpResponse<T?> {
            return HttpResponse(SUCCESS, "success", data)
        }

        fun <T> error(message: String): HttpResponse<T?> {
            return HttpResponse(ERROR, message, null)
        }

        fun <T> error(throwable: Throwable): HttpResponse<T?> {
            return when (throwable) {
                is CloudException -> HttpResponse(
                    throwable.errorCode,
                    throwable.message ?: "Unknown error",
                    null
                )

                else -> HttpResponse(
                    ERROR,
                    "${throwable.message}:\n\n${throwable.stackTraceToString()}",
                    null
                )
            }
        }
    }
}

inline fun <reified T : Any> T.wrapAsResponse(): HttpResponse<T?> {
    return HttpResponse.success(this)
}
