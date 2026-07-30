package com.example.audio

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AudioExportHelper {

    fun createTempAudioFile(context: Context, prefix: String = "hindi_voiceover"): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "${prefix}_${timeStamp}.mp3"
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC) 
            ?: context.filesDir
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        return File(storageDir, fileName)
    }

    fun exportToPublicDownloads(context: Context, audioFile: File, displayTitle: String): Uri? {
        if (!audioFile.exists() || audioFile.length() == 0L) return null

        val cleanTitle = displayTitle.replace(Regex("[^a-zA-Z0-9_ -]"), "_").take(30)
            .ifEmpty { "Hindi_Voiceover" }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val outputFileName = "${cleanTitle}_$timeStamp.mp3"

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Audio.Media.DISPLAY_NAME, outputFileName)
                put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg")
                put(MediaStore.Audio.Media.TITLE, displayTitle)
                put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/HindiVoiceStudio")
                put(MediaStore.Audio.Media.IS_PENDING, 1)
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)

            if (uri != null) {
                try {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        FileInputStream(audioFile).use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.Audio.Media.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                    uri
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            } else null
        } else {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val customFolder = File(downloadsDir, "HindiVoiceStudio")
            if (!customFolder.exists()) {
                customFolder.mkdirs()
            }
            val targetFile = File(customFolder, outputFileName)
            try {
                FileInputStream(audioFile).use { input ->
                    FileOutputStream(targetFile).use { output ->
                        input.copyTo(output)
                    }
                }
                Uri.fromFile(targetFile)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    fun shareAudioFile(context: Context, audioFile: File, title: String) {
        if (!audioFile.exists()) {
            Toast.makeText(context, "Audio file not found", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val authority = "${context.packageName}.fileprovider"
            val contentUri = FileProvider.getUriForFile(context, authority, audioFile)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "audio/*"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "Hindi Voiceover: $title")
                putExtra(Intent.EXTRA_TEXT, "Listen to this Hindi voiceover created with Hindi Voice Studio.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Hindi Voiceover MP3")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Could not share audio file: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 KB"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        return if (mb >= 1.0) {
            String.format(Locale.getDefault(), "%.1f MB", mb)
        } else {
            String.format(Locale.getDefault(), "%.0f KB", kb)
        }
    }

    fun estimateDurationMs(textLength: Int, speechRate: Float): Long {
        if (textLength <= 0) return 0L
        // Average Hindi speech pace ~ 12 characters per second at 1.0x rate
        val charsPerSecond = 12.0 * speechRate
        val totalSeconds = textLength / charsPerSecond
        return (totalSeconds * 1000).toLong()
    }

    fun formatDuration(durationMs: Long): String {
        val totalSeconds = (durationMs / 1000).toInt()
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    private var mediaPlayer: MediaPlayer? = null

    fun playAudioFile(context: Context, audioFile: File, onCompletion: () -> Unit) {
        stopAudioPlayback()
        if (!audioFile.exists()) return

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioFile.absolutePath)
                prepare()
                setOnCompletionListener {
                    onCompletion()
                    stopAudioPlayback()
                }
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onCompletion()
        }
    }

    fun stopAudioPlayback() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaPlayer = null
        }
    }
}
