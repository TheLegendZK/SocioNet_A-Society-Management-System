package com.zhd.socionet.ui.navigation

object Routes {
    // --- Authentication ---
    const val LOGIN = "login_screen"
    const val REGISTER = "register_screen"
    const val SPLASH = "splash_screen"
    const val PENDING_APPROVAL = "pending_approval"

    // --- Admin Module ---
    const val ADMIN_DASHBOARD = "admin_dashboard"
    const val ADMIN_MANAGE_FLATS = "admin_manage_flats"
    const val ADMIN_APPROVALS = "admin_approvals"
    const val ADMIN_POST_NOTICE = "admin_post_notice"
    const val ADMIN_BILLING = "admin_billing"
    const val ADMIN_STAFF_REGISTRY = "admin_staff_registry"
    const val ADMIN_COMPLAINTS = "admin_complaints"

    // --- Resident Module ---
    const val RESIDENT_HOME = "resident_home"
    const val RESIDENT_CREATE_PASS = "resident_create_pass"
    const val RESIDENT_NOTICES = "resident_notices"
    const val RESIDENT_PAYMENTS = "resident_payments"
    const val RESIDENT_COMPLAINTS = "resident_complaints"
    const val RESIDENT_STAFF = "resident_staff"

    // --- Directory Module (New) ---
    const val SOCIETY_DIRECTORY = "society_directory"

    // --- Security Module ---
    const val SECURITY_DASHBOARD = "security_dashboard"
    const val SECURITY_SCANNER = "security_qr_scanner"
    const val SECURITY_MANUAL_ENTRY = "security_manual_entry" // New
    const val SECURITY_STAFF = "security_staff_attendance"

    // --- Profile ---
    const val PROFILE_SCREEN = "profile_screen"

    // --- Settings ---
    const val SETTINGS_SCREEN = "settings_screen"
}

object NavArgs {
    const val FLAT_ID = "flatId"
    const val VISITOR_ID = "visitorId"
    const val NOTICE_ID = "noticeId"
    const val IS_ADMIN = "isAdmin"
    const val SOCIETY_ID = "societyId"
}