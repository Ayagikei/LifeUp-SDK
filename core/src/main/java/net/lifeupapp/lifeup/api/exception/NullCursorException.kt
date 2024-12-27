package net.lifeupapp.lifeup.api.exception

class NullCursorException : CloudException(
    CloudException.ERROR_NULL_CURSOR,
    "Cursor is null, check if LifeUp is installed and running and you have granted the permission"
)
