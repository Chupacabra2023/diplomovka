package com.example.diplomovka_kotlin.data.models

import java.util.Date

data class Invitation(
    val id: String = "",
    val eventId: String = "",
    val eventTitle: String = "",
    val eventPlace: String = "",
    val fromUserId: String = "",
    val fromUserName: String = "",
    val toUserId: String = "",
    val toEmail: String = "",
    val status: String = "pending",
    val createdAt: Date = Date()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "eventId"      to eventId,
        "eventTitle"   to eventTitle,
        "eventPlace"   to eventPlace,
        "fromUserId"   to fromUserId,
        "fromUserName" to fromUserName,
        "toUserId"     to toUserId,
        "toEmail"      to toEmail,
        "status"       to status,
        "createdAt"    to createdAt.time
    )

    companion object {
        fun fromFirestore(id: String, data: Map<String, Any?>): Invitation = Invitation(
            id          = id,
            eventId     = data["eventId"]      as? String ?: "",
            eventTitle  = data["eventTitle"]   as? String ?: "",
            eventPlace  = data["eventPlace"]   as? String ?: "",
            fromUserId  = data["fromUserId"]   as? String ?: "",
            fromUserName = data["fromUserName"] as? String ?: "",
            toUserId    = data["toUserId"]     as? String ?: "",
            toEmail     = data["toEmail"]      as? String ?: "",
            status      = data["status"]       as? String ?: "pending",
            createdAt   = (data["createdAt"]   as? Long)?.let { Date(it) } ?: Date()
        )
    }
}
