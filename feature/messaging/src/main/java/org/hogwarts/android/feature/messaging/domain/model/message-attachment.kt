package org.hogwarts.android.feature.messaging.domain.model

data class MessageAttachment(
    val id: String,
    val messageId: String,
    val fileName: String,
    val fileUrl: String,
    val mimeType: String,
    val fileSizeBytes: Long,
    val thumbnail: String? = null,
    val width: Int? = null,
    val height: Int? = null,
) {
    val isImage: Boolean get() = mimeType.startsWith("image/")
    val isVideo: Boolean get() = mimeType.startsWith("video/")
    val isAudio: Boolean get() = mimeType.startsWith("audio/")
    val isPdf: Boolean get() = mimeType == "application/pdf"
    val isDocument: Boolean get() = !isImage && !isVideo && !isAudio

    val formattedSize: String
        get() = when {
            fileSizeBytes < 1024 -> "$fileSizeBytes B"
            fileSizeBytes < 1024 * 1024 -> "${fileSizeBytes / 1024} KB"
            else -> "${fileSizeBytes / (1024 * 1024)} MB"
        }
}
