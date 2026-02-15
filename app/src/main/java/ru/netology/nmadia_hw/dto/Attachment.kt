package ru.netology.nmadia_hw.dto

data class Attachment(
    val url: String,
    val description: String? = null,
    val type: AttachmentType,
)

enum class AttachmentType {
    IMAGE,
}
