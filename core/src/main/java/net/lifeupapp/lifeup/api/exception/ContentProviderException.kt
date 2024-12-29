package net.lifeupapp.lifeup.api.exception

class ContentProviderException(
    errorMessage: String
) : CloudException(
    ERROR_CONTENT_PROVIDER,
    errorMessage
)
