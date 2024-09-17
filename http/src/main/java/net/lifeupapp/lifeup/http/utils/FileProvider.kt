package net.lifeupapp.lifeup.http.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import net.lifeupapp.lifeup.api.Val
import net.lifeupapp.lifeup.http.base.appCtx
import java.io.File

fun File.getUriForFile(
    context: Context = appCtx,
    authority: String = "${context.packageName}.fileprovider",
    targetPackage: String? = Val.LIFEUP_PACKAGE_NAME,
    writePermission: Boolean = false
): Uri {
    val contentUri = FileProvider.getUriForFile(context, authority, this)

    targetPackage?.let { packageName ->
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                (if (writePermission) Intent.FLAG_GRANT_WRITE_URI_PERMISSION else 0)

        context.grantUriPermission(packageName, contentUri, flags)
    }

    return contentUri
}