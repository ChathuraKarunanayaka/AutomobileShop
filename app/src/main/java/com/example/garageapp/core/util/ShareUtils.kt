package com.example.garageapp.core.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object ShareUtils {
    fun shareText(context: Context, text: String, phoneNumber: String? = null) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            if (!phoneNumber.isNullOrBlank()) {
                val cleanPhone = phoneNumber.replace("+", "").replace(" ", "")
                putExtra("jid", "$cleanPhone@s.whatsapp.net")
            }
            `package` = "com.whatsapp"
        }
        
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // If WhatsApp is not installed, use generic share
            val genericIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            context.startActivity(Intent.createChooser(genericIntent, "Share via"))
        }
    }

    fun shareFile(context: Context, file: File, phoneNumber: String? = null) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            if (!phoneNumber.isNullOrBlank()) {
                val cleanPhone = phoneNumber.replace("+", "").replace(" ", "")
                putExtra("jid", "$cleanPhone@s.whatsapp.net")
            }
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            `package` = "com.whatsapp"
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val genericIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(genericIntent, "Share PDF"))
        }
    }
}
