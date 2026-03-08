package com.zhd.socionet.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Report
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.zhd.socionet.data.model.UserRole
import com.zhd.socionet.ui.admin.approval.AdminApprovalScreen
import com.zhd.socionet.ui.admin.complaints.AdminComplaintScreen
import com.zhd.socionet.ui.admin.dashboard.AdminDashboardScreen
import com.zhd.socionet.ui.admin.masterdata.AdminSetupScreen
import com.zhd.socionet.ui.admin.notices.CreateNoticeScreen
import com.zhd.socionet.ui.auth.LoginScreen
import com.zhd.socionet.ui.auth.PendingApprovalScreen
import com.zhd.socionet.ui.auth.RegisterScreen
import com.zhd.socionet.ui.billing.BillingScreen
import com.zhd.socionet.ui.common.AdaptiveAppShell
import com.zhd.socionet.ui.common.NavItem
import com.zhd.socionet.ui.directory.SocietyDirectoryScreen
import com.zhd.socionet.ui.profile.ProfileScreen
import com.zhd.socionet.ui.profile.ProfileViewModel
import com.zhd.socionet.ui.resident.complaints.ResidentComplaintScreen
import com.zhd.socionet.ui.resident.gatepass.VisitorManagementScreen
import com.zhd.socionet.ui.resident.home.ResidentHomeScreen
import com.zhd.socionet.ui.resident.notices.NoticeBoardScreen
import com.zhd.socionet.ui.resident.payments.ResidentPaymentsScreen
import com.zhd.socionet.ui.security.entries.ManualEntryScreen
import com.zhd.socionet.ui.security.scanner.SecurityScannerScreen
import com.zhd.socionet.ui.security.staff.SecurityStaffScreen
import com.zhd.socionet.ui.settings.SettingsScreen
import com.zhd.socionet.ui.splash.SplashScreen

@Composable
fun SocioNetNavGraph(
    navController: NavHostController,
    startDestination: String = Routes.SPLASH,
    isDeepLinkPending: Boolean = false
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""

    val navItems = remember(currentRoute, navBackStackEntry) {
        when {
            currentRoute.startsWith(Routes.RESIDENT_HOME) || 
            currentRoute.startsWith(Routes.RESIDENT_CREATE_PASS) ||
            currentRoute.startsWith(Routes.RESIDENT_NOTICES) ||
            currentRoute.startsWith(Routes.RESIDENT_COMPLAINTS) ||
            currentRoute.startsWith(Routes.RESIDENT_PAYMENTS) ||
            (currentRoute.startsWith(Routes.PROFILE_SCREEN) && navBackStackEntry?.arguments?.getString("type") == "resident") ||
            (currentRoute.startsWith(Routes.SOCIETY_DIRECTORY) && navBackStackEntry?.arguments?.getBoolean(NavArgs.IS_ADMIN) == false) -> {
                val flatId = navBackStackEntry?.arguments?.getString(NavArgs.FLAT_ID) ?: ""
                val userId = navBackStackEntry?.arguments?.getString(NavArgs.USER_ID) ?: ""
                val societyId = navBackStackEntry?.arguments?.getString(NavArgs.SOCIETY_ID) ?: ""
                listOf(
                    NavItem("Home", Routes.buildResidentHome(flatId, userId), Icons.Default.Home, Routes.RESIDENT_HOME),
                    NavItem("Gate Pass", Routes.buildResidentCreatePass(flatId, userId), Icons.Default.QrCode, Routes.RESIDENT_CREATE_PASS),
                    NavItem("Notices", Routes.buildResidentNotices(flatId, userId, societyId), Icons.Default.Campaign, Routes.RESIDENT_NOTICES),
                    NavItem("Profile", Routes.buildProfile(userId, "resident", flatId), Icons.Default.Person, Routes.PROFILE_SCREEN)
                )
            }
            currentRoute.startsWith("admin_") || 
            (currentRoute.startsWith(Routes.PROFILE_SCREEN) && navBackStackEntry?.arguments?.getString("type") == "admin") ||
            (currentRoute.startsWith(Routes.SOCIETY_DIRECTORY) && navBackStackEntry?.arguments?.getBoolean(NavArgs.IS_ADMIN) == true) -> {
                val adminId = FirebaseAuth.getInstance().currentUser?.uid ?: "admin"
                listOf(
                    NavItem("Dashboard", Routes.ADMIN_DASHBOARD, Icons.Default.Dashboard, Routes.ADMIN_DASHBOARD),
                    NavItem("Complaints", Routes.ADMIN_COMPLAINTS, Icons.Default.Report, Routes.ADMIN_COMPLAINTS),
                    NavItem("Notices", Routes.ADMIN_POST_NOTICE, Icons.Default.Campaign, Routes.ADMIN_POST_NOTICE),
                    NavItem("Profile", Routes.buildProfile(adminId, "admin"), Icons.Default.Person, Routes.PROFILE_SCREEN)
                )
            }
            else -> emptyList()
        }
    }

    AdaptiveAppShell(navController, navItems) {
        NavHost(navController = navController, startDestination = startDestination) {

            composable(Routes.SPLASH) {
                SplashScreen(
                    isDeepLinkPending = isDeepLinkPending,
                    onNavigate = { dest ->
                    navController.navigate(dest) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                })
            }
            composable(Routes.LOGIN) {
                LoginScreen(
                    onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                    onLoginSuccess = { profile ->
                        val dest = NavigationRouter.getHomeDestination(profile)
                        navController.navigate(dest) { popUpTo(Routes.LOGIN) { inclusive = true } }
                    }
                )
            }
            composable(Routes.REGISTER) { RegisterScreen(onNavigateToLogin = { navController.navigateUp() }) }
            composable(Routes.PENDING_APPROVAL) {
                PendingApprovalScreen(onBackToLogin = {
                    navController.navigate(Routes.LOGIN) { popUpTo(0) }
                })
            }

            composable(
                route = "${Routes.RESIDENT_HOME}/{${NavArgs.FLAT_ID}}/{${NavArgs.USER_ID}}",
                arguments = listOf(
                    navArgument(NavArgs.FLAT_ID) { type = NavType.StringType },
                    navArgument(NavArgs.USER_ID) { type = NavType.StringType })
            ) { backStack ->
                val flatId = backStack.arguments?.getString(NavArgs.FLAT_ID) ?: ""
                val userId = backStack.arguments?.getString(NavArgs.USER_ID) ?: ""
                ResidentHomeScreen(navController, flatId, userId)
            }

            composable(
                route = "${Routes.RESIDENT_CREATE_PASS}/{${NavArgs.FLAT_ID}}/{${NavArgs.USER_ID}}?tab={tab}",
                arguments = listOf(
                    navArgument(NavArgs.FLAT_ID) { type = NavType.StringType },
                    navArgument(NavArgs.USER_ID) { type = NavType.StringType },
                    navArgument("tab") { type = NavType.IntType; defaultValue = 0 }
                )
            ) { backStack ->
                val flatId = backStack.arguments?.getString(NavArgs.FLAT_ID) ?: ""
                val userId = backStack.arguments?.getString(NavArgs.USER_ID) ?: ""
                val tab = backStack.arguments?.getInt("tab") ?: 0
                VisitorManagementScreen(
                    hostFlatId = flatId,
                    hostUserId = userId,
                    initialTab = tab,
                    onBackClick = { navController.popBackStack() })
            }

            composable(
                route = "${Routes.RESIDENT_NOTICES}/{${NavArgs.FLAT_ID}}/{${NavArgs.USER_ID}}?${NavArgs.SOCIETY_ID}={${NavArgs.SOCIETY_ID}}",
                arguments = listOf(
                    navArgument(NavArgs.FLAT_ID) { type = NavType.StringType },
                    navArgument(NavArgs.USER_ID) { type = NavType.StringType },
                    navArgument(NavArgs.SOCIETY_ID) { type = NavType.StringType; defaultValue = "" }
                )
            ) { backStack ->
                val societyId = backStack.arguments?.getString(NavArgs.SOCIETY_ID) ?: ""
                NoticeBoardScreen(
                    societyId = societyId,
                    userWing = "A", // Placeholder - in real app fetch from user profile
                    userRoles = listOf(UserRole.OWNER), // Placeholder
                    onBack = { navController.popBackStack() })
            }

            composable(
                "${Routes.RESIDENT_COMPLAINTS}/{${NavArgs.FLAT_ID}}/{${NavArgs.USER_ID}}",
                arguments = listOf(
                    navArgument(NavArgs.FLAT_ID) { type = NavType.StringType },
                    navArgument(NavArgs.USER_ID) { type = NavType.StringType })
            ) { backStack ->
                val flatId = backStack.arguments?.getString(NavArgs.FLAT_ID) ?: ""
                val userId = backStack.arguments?.getString(NavArgs.USER_ID) ?: ""
                ResidentComplaintScreen(
                    flatId = flatId,
                    userId = userId,
                    onBack = { navController.popBackStack() })
            }

            composable(
                route = "${Routes.RESIDENT_PAYMENTS}/{${NavArgs.FLAT_ID}}/{${NavArgs.USER_ID}}",
                arguments = listOf(
                    navArgument(NavArgs.FLAT_ID) { type = NavType.StringType },
                    navArgument(NavArgs.USER_ID) { type = NavType.StringType })
            ) { backStack ->
                val flatId = backStack.arguments?.getString(NavArgs.FLAT_ID) ?: ""
                val userId = backStack.arguments?.getString(NavArgs.USER_ID) ?: ""
                ResidentPaymentsScreen(
                    flatId = flatId,
                    userId = userId,
                    onBack = { navController.popBackStack() })
            }

            composable(Routes.ADMIN_DASHBOARD) { AdminDashboardScreen(navController) }
            composable(Routes.ADMIN_COMPLAINTS) { AdminComplaintScreen(onBack = { navController.popBackStack() }) }
            composable(
                route = "${Routes.ADMIN_POST_NOTICE}?${NavArgs.SOCIETY_ID}={${NavArgs.SOCIETY_ID}}",
                arguments = listOf(navArgument(NavArgs.SOCIETY_ID) { type = NavType.StringType; nullable = true; defaultValue = null })
            ) { backStack ->
                val societyId = backStack.arguments?.getString(NavArgs.SOCIETY_ID) ?: ""
                CreateNoticeScreen(
                    adminId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                    societyId = societyId,
                    onBack = { navController.popBackStack() })
            }
            composable(Routes.ADMIN_MANAGE_FLATS) {
                AdminSetupScreen(onSetupComplete = { navController.popBackStack() }, onBack = { navController.popBackStack() })
            }
            composable(
                route = "${Routes.ADMIN_APPROVALS}/{${NavArgs.SOCIETY_ID}}",
                arguments = listOf(navArgument(NavArgs.SOCIETY_ID) { type = NavType.StringType })
            ) { backStack ->
                val societyId = backStack.arguments?.getString(NavArgs.SOCIETY_ID) ?: ""
                AdminApprovalScreen(societyId = societyId, onBack = { navController.popBackStack() })
            }
            composable(
                route = "${Routes.ADMIN_BILLING}/{${NavArgs.SOCIETY_ID}}",
                arguments = listOf(navArgument(NavArgs.SOCIETY_ID) { type = NavType.StringType })
            ) { backStack ->
                val societyId = backStack.arguments?.getString(NavArgs.SOCIETY_ID) ?: ""
                BillingScreen(
                    userId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                    flatId = null,
                    isAdmin = true,
                    societyId = societyId,
                    onBack = { navController.popBackStack() })
            }

            composable(
                route = "${Routes.SOCIETY_DIRECTORY}/{${NavArgs.IS_ADMIN}}/{${NavArgs.SOCIETY_ID}}?flatId={flatId}&userId={userId}",
                arguments = listOf(
                    navArgument(NavArgs.IS_ADMIN) { type = NavType.BoolType },
                    navArgument(NavArgs.SOCIETY_ID) { type = NavType.StringType },
                    navArgument("flatId") { type = NavType.StringType; nullable = true },
                    navArgument("userId") { type = NavType.StringType; nullable = true }
                )
            ) { backStack ->
                val isAdmin = backStack.arguments?.getBoolean(NavArgs.IS_ADMIN) ?: false
                val societyId = backStack.arguments?.getString(NavArgs.SOCIETY_ID) ?: ""
                val flatId = backStack.arguments?.getString("flatId") ?: ""
                SocietyDirectoryScreen(isAdmin = isAdmin, societyId = societyId, flatId = flatId, onBack = { navController.popBackStack() })
            }

            composable(
                route = "${Routes.PROFILE_SCREEN}/{${NavArgs.USER_ID}}?type={type}&flatId={flatId}",
                arguments = listOf(
                    navArgument(NavArgs.USER_ID) { type = NavType.StringType },
                    navArgument("type") { type = NavType.StringType; defaultValue = "resident" },
                    navArgument("flatId") { type = NavType.StringType; nullable = true; defaultValue = "" }
                )
            ) { backStack ->
                val userId = backStack.arguments?.getString(NavArgs.USER_ID) ?: ""
                val type = backStack.arguments?.getString("type") ?: "resident"
                val viewModel: ProfileViewModel = hiltViewModel()
                val onSwitch = { target: String ->
                    if (target == "admin") {
                        navController.navigate(Routes.ADMIN_DASHBOARD) { popUpTo(Routes.ADMIN_DASHBOARD) { inclusive = true } }
                    } else {
                        val user = viewModel.uiState.value.userProfile
                        if (user != null && !user.flatId.isNullOrBlank()) {
                            val route = Routes.buildResidentHome(user.flatId, user.id)
                            navController.navigate(route) { popUpTo(Routes.RESIDENT_HOME) { inclusive = true } }
                        }
                    }
                }
                ProfileScreen(userId = userId, currentViewType = type, onSwitchRole = { onSwitch(it) }, onBack = { navController.popBackStack() }, onLogout = { navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } } })
            }

            composable(Routes.SETTINGS_SCREEN) { SettingsScreen(onBack = { navController.popBackStack() }) }

            composable(Routes.SECURITY_DASHBOARD) {
                SecurityScannerScreen(
                    onNavigateToManualEntry = { navController.navigate(Routes.SECURITY_MANUAL_ENTRY) },
                    onNavigateToStaffAttendance = { navController.navigate(Routes.buildSecurityStaff(NavArgs.SOCIETY_ID)) },
                    onLogout = { navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } } }
                )
            }
            composable(Routes.SECURITY_MANUAL_ENTRY) {
                ManualEntryScreen(onEntryLogged = { navController.popBackStack() }, onBack = { navController.popBackStack() })
            }
            composable(
                route = "${Routes.SECURITY_STAFF}/{${NavArgs.SOCIETY_ID}}",
                arguments = listOf(navArgument(NavArgs.SOCIETY_ID) { type = NavType.StringType })
            ) { backStack ->
                val societyId = backStack.arguments?.getString(NavArgs.SOCIETY_ID) ?: ""
                SecurityStaffScreen(societyId = societyId, onBack = { navController.popBackStack() })
            }
        }
    }
}
