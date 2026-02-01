package com.zhd.socionet.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.zhd.socionet.data.model.UserRole
import com.zhd.socionet.ui.admin.approval.AdminApprovalScreen
import com.zhd.socionet.ui.admin.complaints.AdminComplaintScreen
import com.zhd.socionet.ui.admin.dashboard.AdminDashboardScreen
import com.zhd.socionet.ui.admin.masterdata.AdminSetupScreen
import com.zhd.socionet.ui.admin.notices.CreateNoticeScreen
import com.zhd.socionet.ui.admin.staff.StaffRegistryScreen
import com.zhd.socionet.ui.auth.LoginScreen
import com.zhd.socionet.ui.auth.PendingApprovalScreen
import com.zhd.socionet.ui.auth.RegisterScreen
import com.zhd.socionet.ui.billing.BillingScreen
import com.zhd.socionet.ui.common.AdaptiveAppShell
import com.zhd.socionet.ui.common.NavItem
import com.zhd.socionet.ui.profile.ProfileScreen
import com.zhd.socionet.ui.resident.complaints.ResidentComplaintScreen
import com.zhd.socionet.ui.resident.gatepass.VisitorManagementScreen
import com.zhd.socionet.ui.resident.home.ResidentHomeScreen
import com.zhd.socionet.ui.resident.notices.NoticeBoardScreen
import com.zhd.socionet.ui.resident.payments.ResidentPaymentsScreen
import com.zhd.socionet.ui.resident.staff.ResidentStaffScreen
import com.zhd.socionet.ui.security.entries.ManualEntryScreen
import com.zhd.socionet.ui.security.scanner.SecurityScannerScreen
import com.zhd.socionet.ui.security.staff.SecurityStaffScreen
import com.zhd.socionet.ui.settings.SettingsScreen
import com.zhd.socionet.ui.splash.SplashScreen

@Composable
fun SocioNetNavGraph(
    navController: NavHostController,
    startDestination: String = Routes.LOGIN
) {
    NavHost(navController = navController, startDestination = startDestination) {

        // --- SPLASH & AUTH ---
        composable(Routes.SPLASH) { SplashScreen(onNavigate = { dest -> navController.navigate(dest) { popUpTo(Routes.SPLASH) { inclusive = true } } }) }
        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                onLoginSuccess = { profile ->
                    val dest = when {
                        profile.roles.contains(UserRole.SUPER_ADMIN) -> Routes.ADMIN_DASHBOARD
                        profile.roles.contains(UserRole.SECURITY_GUARD) -> Routes.SECURITY_DASHBOARD
                        !profile.isVerified -> Routes.PENDING_APPROVAL
                        else -> "${Routes.RESIDENT_HOME}/${profile.assignedFlatId ?: "0"}/${profile.id}"
                    }
                    navController.navigate(dest) { popUpTo(Routes.LOGIN) { inclusive = true } }
                }
            )
        }
        composable(Routes.REGISTER) { RegisterScreen(onNavigateToLogin = { navController.navigateUp() }) }
        composable(Routes.PENDING_APPROVAL) { PendingApprovalScreen(onBackToLogin = { navController.navigate(Routes.LOGIN) { popUpTo(0) } }) }

        // --- HELPER: RESIDENT SHELL ---
        fun residentShell(content: @Composable () -> Unit) = @Composable {
            val backStack = navController.currentBackStackEntry
            val flatId = backStack?.arguments?.getString(NavArgs.FLAT_ID) ?: ""
            val userId = backStack?.arguments?.getString("userId") ?: FirebaseAuth.getInstance().currentUser?.uid ?: ""

            val residentNavItems = listOf(
                NavItem("Home", "${Routes.RESIDENT_HOME}/$flatId/$userId", Icons.Default.Home, selectedRoutePrefix = Routes.RESIDENT_HOME),
                NavItem("Gate Pass", "${Routes.RESIDENT_CREATE_PASS}/$flatId/$userId", Icons.Default.QrCode, selectedRoutePrefix = Routes.RESIDENT_CREATE_PASS),
                NavItem("Notices", Routes.RESIDENT_NOTICES, Icons.Default.Campaign, selectedRoutePrefix = Routes.RESIDENT_NOTICES),
                NavItem("Profile", "${Routes.PROFILE_SCREEN}/$userId?type=resident", Icons.Default.Person, selectedRoutePrefix = Routes.PROFILE_SCREEN)
            )
            AdaptiveAppShell(navController, residentNavItems) { content() }
        }

        // --- HELPER: ADMIN SHELL ---
        fun adminShell(content: @Composable () -> Unit) = @Composable {
            val adminId = FirebaseAuth.getInstance().currentUser?.uid ?: "admin"
            val adminNavItems = listOf(
                NavItem("Dashboard", Routes.ADMIN_DASHBOARD, Icons.Default.Dashboard, selectedRoutePrefix = Routes.ADMIN_DASHBOARD),
                NavItem("Complaints", Routes.ADMIN_COMPLAINTS, Icons.Default.Report, selectedRoutePrefix = Routes.ADMIN_COMPLAINTS),
                NavItem("Notices", Routes.ADMIN_POST_NOTICE, Icons.Default.Campaign, selectedRoutePrefix = Routes.ADMIN_POST_NOTICE),
                NavItem("Profile", "${Routes.PROFILE_SCREEN}/$adminId?type=admin", Icons.Default.Person, selectedRoutePrefix = Routes.PROFILE_SCREEN)
            )
            AdaptiveAppShell(navController, adminNavItems) { content() }
        }

        // --- RESIDENT SCREENS ---
        composable(
            route = "${Routes.RESIDENT_HOME}/{${NavArgs.FLAT_ID}}/{userId}",
            arguments = listOf(navArgument(NavArgs.FLAT_ID) { type = NavType.StringType }, navArgument("userId") { type = NavType.StringType })
        ) { backStack ->
            val flatId = backStack.arguments?.getString(NavArgs.FLAT_ID) ?: ""
            val userId = backStack.arguments?.getString("userId") ?: ""
            residentShell { ResidentHomeScreen(navController, flatId, userId) }()
        }

        composable(
            route = "${Routes.RESIDENT_CREATE_PASS}/{${NavArgs.FLAT_ID}}/{userId}",
            arguments = listOf(navArgument(NavArgs.FLAT_ID) { type = NavType.StringType }, navArgument("userId") { type = NavType.StringType })
        ) { backStack ->
            val flatId = backStack.arguments?.getString(NavArgs.FLAT_ID) ?: ""
            val userId = backStack.arguments?.getString("userId") ?: ""
            residentShell { VisitorManagementScreen(hostFlatId = flatId, hostUserId = userId, onBackClick = { navController.popBackStack() }) }()
        }

        composable(Routes.RESIDENT_NOTICES) {
            residentShell { NoticeBoardScreen(userWing = "A", userRoles = listOf(UserRole.OWNER), onBack = { navController.popBackStack() }) }()
        }

        composable("${Routes.RESIDENT_COMPLAINTS}/{${NavArgs.FLAT_ID}}/{userId}",
            arguments = listOf(navArgument(NavArgs.FLAT_ID) { type = NavType.StringType }, navArgument("userId") { type = NavType.StringType })
        ) { backStack ->
            val flatId = backStack.arguments?.getString(NavArgs.FLAT_ID) ?: ""
            val userId = backStack.arguments?.getString("userId") ?: ""
            residentShell { ResidentComplaintScreen(flatId = flatId, userId = userId, onBack = { navController.popBackStack() }) }()
        }

        composable(
            route = "${Routes.RESIDENT_STAFF}/{${NavArgs.FLAT_ID}}",
            arguments = listOf(navArgument(NavArgs.FLAT_ID) { type = NavType.StringType })
        ) { backStack ->
            val flatId = backStack.arguments?.getString(NavArgs.FLAT_ID) ?: ""
            residentShell { ResidentStaffScreen(flatId = flatId, onBack = { navController.popBackStack() }) }()
        }

        composable(
            route = "${Routes.RESIDENT_PAYMENTS}/{${NavArgs.FLAT_ID}}/{userId}",
            arguments = listOf(navArgument(NavArgs.FLAT_ID) { type = NavType.StringType }, navArgument("userId") { type = NavType.StringType })
        ) { backStack ->
            val flatId = backStack.arguments?.getString(NavArgs.FLAT_ID) ?: ""
            val userId = backStack.arguments?.getString("userId") ?: ""
            residentShell { ResidentPaymentsScreen(flatId = flatId, userId = userId, onBack = { navController.popBackStack() }) }()
        }

        // --- ADMIN SCREENS ---
        composable(Routes.ADMIN_DASHBOARD) { adminShell { AdminDashboardScreen(navController) }() }
        composable(Routes.ADMIN_COMPLAINTS) { adminShell { AdminComplaintScreen(onBack = { navController.popBackStack() }) }() }
        composable(Routes.ADMIN_POST_NOTICE) { adminShell { CreateNoticeScreen(adminId = FirebaseAuth.getInstance().currentUser?.uid ?: "", onBack = { navController.popBackStack() }) }() }
        composable(Routes.ADMIN_MANAGE_FLATS) { adminShell { AdminSetupScreen(onSetupComplete = { navController.popBackStack() }, onBack = { navController.popBackStack() }) }() }
        composable(Routes.ADMIN_APPROVALS) { adminShell { AdminApprovalScreen(onBack = { navController.popBackStack() }) }() }
        composable(Routes.ADMIN_BILLING) { adminShell { BillingScreen(userId = FirebaseAuth.getInstance().currentUser?.uid ?: "", flatId = null, isAdmin = true, societyId = "WbZ6qVoyNlMi1Quch8o3", onBack = { navController.popBackStack() }) }() }
        composable(Routes.ADMIN_STAFF_REGISTRY) { adminShell { StaffRegistryScreen(onBack = { navController.popBackStack() }) }() }

        // --- PROFILE & SETTINGS ---
        composable(
            route = "${Routes.PROFILE_SCREEN}/{userId}?type={type}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType }, navArgument("type") { type = NavType.StringType; defaultValue = "resident" })
        ) { backStack ->
            val userId = backStack.arguments?.getString("userId") ?: ""
            val type = backStack.arguments?.getString("type") ?: "resident"
            val content = @Composable {
                ProfileScreen(userId = userId, onBack = { navController.popBackStack() }, onLogout = { navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } } })
            }
            if (type == "admin") adminShell { content() }() else residentShell { content() }()
        }

        composable(Routes.SETTINGS_SCREEN) { SettingsScreen(onBack = { navController.popBackStack() }) }

        // --- SECURITY ---
        composable(Routes.SECURITY_DASHBOARD) {
            SecurityScannerScreen(
                onNavigateToManualEntry = { navController.navigate(Routes.SECURITY_MANUAL_ENTRY) },
                onNavigateToStaffAttendance = { navController.navigate(Routes.SECURITY_STAFF) },
                onLogout = { navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } } }
            )
        }
        composable(Routes.SECURITY_MANUAL_ENTRY) { ManualEntryScreen(onEntryLogged = { navController.popBackStack() }, onBack = { navController.popBackStack() }) }
        composable(Routes.SECURITY_STAFF) { SecurityStaffScreen(onBack = { navController.popBackStack() }) }
    }
}