package com.zhd.socionet.core.constants

object AppConstants {
    // Firestore Collection Names
    const val COLL_SOCIETIES = "societies"
    const val COLL_FLATS = "flats"
    const val COLL_USERS = "users"
    const val COLL_GATE_ENTRIES = "gate_entries"
    const val COLL_NOTICES = "notices"
    const val COLL_BILLS = "bills"
    const val COLL_SERVICE_STAFF = "service_staff"
    const val COLL_COMPLAINTS = "complaints"
    const val COLL_CONFIG = "config"
    const val COLL_EMERGENCY_ALERTS = "emergency_alerts"

    // Firestore Document Names
    const val DOC_SETTINGS = "settings"

    // Storage Paths
    const val PATH_PROFILE_PICS = "profile_photos/"
    const val PATH_NOTICE_ATTACHMENTS = "notice_attachments/"

    // Validation Limits
    const val OTP_LENGTH = 6
    const val MIN_PASSWORD_LENGTH = 6
}