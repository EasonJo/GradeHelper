package com.eason.grade.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.v4.content.FileProvider
import com.eason.grade.BuildConfig

import java.io.File

object FileProviderUtils {
    fun getUriForFile(mContext: Context, file: File): Uri? {
        var fileUri: Uri? = null
        if (Build.VERSION.SDK_INT >= 24) {
            fileUri = getUriForFile24(mContext, file)
        } else {
            fileUri = Uri.fromFile(file)
        }
        return fileUri
    }

    fun getUriForFile24(mContext: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            mContext,
            BuildConfig.APPLICATION_ID + ".provider",
            file
        )
    }

    fun setIntentDataAndType(
        mContext: Context,
        intent: Intent,
        type: String,
        file: File,
        writeAble: Boolean
    ) {
        if (Build.VERSION.SDK_INT >= 24) {
            intent.setDataAndType(getUriForFile(mContext, file), type)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (writeAble) {
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
        } else {
            intent.setDataAndType(Uri.fromFile(file), type)
        }
    }
}
