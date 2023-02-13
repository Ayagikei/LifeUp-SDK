package net.lifeupapp.lifeup.api.exception

class NullCursorException :
    RuntimeException("Cursor is null, check if LifeUp is installed and running and you have granted the permission")
