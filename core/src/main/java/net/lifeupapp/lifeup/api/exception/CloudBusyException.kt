package net.lifeupapp.lifeup.api.exception

/**
 * Admission rejected the attempt before ContentProvider work started.
 * The HTTP bridge maps this to status 503 and [CloudException.ERROR_CLOUD_BUSY].
 */
class CloudBusyException : CloudException(
    CloudException.ERROR_CLOUD_BUSY,
    MESSAGE,
) {
    companion object {
        const val MESSAGE =
            "LifeUp Cloud is busy; the request was not started. Retry later."
    }
}
