package com.zhd.socionet.ui.navigation

import com.zhd.socionet.data.model.UserProfile
import com.zhd.socionet.data.model.UserRole

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
    const val ADMIN_COMPLAINTS = "admin_complaints"

    // --- Resident Module ---
    const val RESIDENT_HOME = "resident_home"
    const val RESIDENT_CREATE_PASS = "resident_create_pass"
    const val RESIDENT_NOTICES = "resident_notices"
    const val RESIDENT_PAYMENTS = "resident_payments"
    const val RESIDENT_COMPLAINTS = "resident_complaints"

    // --- Directory Module ---
    const val SOCIETY_DIRECTORY = "society_directory"

    // --- Security Module ---
    const val SECURITY_DASHBOARD = "security_dashboard"
    const val SECURITY_SCANNER = "security_qr_scanner"
    const val SECURITY_MANUAL_ENTRY = "security_manual_entry"
    const val SECURITY_STAFF = "security_staff_attendance"

    // --- Profile ---
    const val PROFILE_SCREEN = "profile_screen"

    // --- Settings ---
    const val SETTINGS_SCREEN = "settings_screen"

    // --- ROUTE BUILDERS (Safe Navigation) ---

    fun buildResidentHome(flatId: String, userId: String) = "$RESIDENT_HOME/$flatId/$userId"

    fun buildResidentCreatePass(flatId: String, userId: String, tab: Int = 0) = "$RESIDENT_CREATE_PASS/$flatId/$userId?tab=$tab"

    fun buildResidentNotices(flatId: String, userId: String, societyId: String = "") = "$RESIDENT_NOTICES/$flatId/$userId?${NavArgs.SOCIETY_ID}=$societyId"

    fun buildResidentComplaints(flatId: String, userId: String) = "$RESIDENT_COMPLAINTS/$flatId/$userId"

    fun buildResidentPayments(flatId: String, userId: String) = "$RESIDENT_PAYMENTS/$flatId/$userId"

    fun buildAdminApprovals(societyId: String) = "$ADMIN_APPROVALS/$societyId"

    fun buildAdminBilling(societyId: String) = "$ADMIN_BILLING/$societyId"

    fun buildAdminPostNotice(societyId: String) = "$ADMIN_POST_NOTICE?${NavArgs.SOCIETY_ID}=$societyId"

    fun buildSecurityStaff(societyId: String) = "$SECURITY_STAFF/$societyId"

    fun buildPendingApproval(wing: String? = null, flat: String? = null) = 
        "$PENDING_APPROVAL?wing=${wing ?: ""}&flat=${flat ?: ""}"

    fun buildDirectory(isAdmin: Boolean, societyId: String, flatId: String? = null, userId: String? = null): String {
        return "$SOCIETY_DIRECTORY/$isAdmin/$societyId?flatId=${flatId ?: ""}&userId=${userId ?: ""}"
    }

    fun buildProfile(userId: String, type: String = "resident", flatId: String? = null): String {
        return "$PROFILE_SCREEN/$userId?type=$type&flatId=${flatId ?: ""}"
    }
}

object NavArgs {
    const val FLAT_ID = "flatId"
    const val VISITOR_ID = "visitorId"
    const val NOTICE_ID = "noticeId"
    const val IS_ADMIN = "isAdmin"
    const val SOCIETY_ID = "societyId"
    const val USER_ID = "userId"
}

// --- NAVIGATION LOGIC ---
object NavigationRouter {
    fun getHomeDestination(profile: UserProfile): String {
        return when {
            profile.roles.contains(UserRole.SUPER_ADMIN) -> Routes.ADMIN_DASHBOARD
            profile.roles.contains(UserRole.SECURITY_GUARD) -> Routes.SECURITY_DASHBOARD
            !profile.isVerified -> Routes.buildPendingApproval(profile.wingId, profile.flatNumber)
            else -> Routes.buildResidentHome(profile.flatId ?: profile.societyId ?: "0", profile.id)
        }
    }
}
