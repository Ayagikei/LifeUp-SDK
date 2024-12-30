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

        fun <T> error(message: String, code: Int = ERROR): HttpResponse<T?> {
            return HttpResponse(code, message, null)
        }

        fun <T> error(throwable: Throwable): HttpResponse<T?> {
            return when {
                throwable is CloudException -> HttpResponse(
                    throwable.errorCode,
                    throwable.message ?: "Unknown error",
                    null
                )

                throwable is IllegalArgumentException &&
                        throwable.message?.contains("Unknown authority net.sarasarasa.lifeup.provider.api") == true -> HttpResponse(
                    CloudException.ERROR_NULL_CURSOR,
                    "Unknown authority net.sarasarasa.lifeup.provider.api, check if LifeUp is installed and running and you have granted the permission",
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
