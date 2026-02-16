package com.zhd.socionet.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Report
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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
import com.zhd.socionet.ui.directory.SocietyDirectoryScreen
import com.zhd.socionet.ui.profile.ProfileScreen
import com.zhd.socionet.ui.profile.ProfileViewModel
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
    startDestination: String = Routes.SPLASH,
) {
    NavHost(navController = navController, startDestination = startDestination) {

        // --- SPLASH & AUTH ---
        composable(Routes.SPLASH) {
            SplashScreen(onNavigate = { dest ->
                navController.navigate(dest) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            })
        }
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
        composable(Routes.PENDING_APPROVAL) {
            PendingApprovalScreen(onBackToLogin = {
                navController.navigate(Routes.LOGIN) { popUpTo(0) }
            })
        }

        // --- HELPER: RESIDENT SHELL ---
        fun residentShell(content: @Composable () -> Unit) = @Composable {
            val backStack = navController.currentBackStackEntry
            val flatId = backStack?.arguments?.getString(NavArgs.FLAT_ID) ?: ""
            val userId = backStack?.arguments?.getString(NavArgs.USER_ID)
                ?: FirebaseAuth.getInstance().currentUser?.uid ?: ""

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
            route = "${Routes.RESIDENT_HOME}/{${NavArgs.FLAT_ID}}/{${NavArgs.USER_ID}}",
            arguments = listOf(
                navArgument(NavArgs.FLAT_ID) { type = NavType.StringType },
                navArgument(NavArgs.USER_ID) { type = NavType.StringType })
        ) { backStack ->
            val flatId = backStack.arguments?.getString(NavArgs.FLAT_ID) ?: ""
            val userId = backStack.arguments?.getString(NavArgs.USER_ID) ?: ""
            residentShell { ResidentHomeScreen(navController, flatId, userId) }()
        }

        composable(
            route = "${Routes.RESIDENT_CREATE_PASS}/{${NavArgs.FLAT_ID}}/{${NavArgs.USER_ID}}",
            arguments = listOf(
                navArgument(NavArgs.FLAT_ID) { type = NavType.StringType },
                navArgument(NavArgs.USER_ID) { type = NavType.StringType })
        ) { backStack ->
            val flatId = backStack.arguments?.getString(NavArgs.FLAT_ID) ?: ""
            val userId = backStack.arguments?.getString(NavArgs.USER_ID) ?: ""
            residentShell {
                VisitorManagementScreen(hostFlatId = flatId, hostUserId = userId, onBackClick = { navController.popBackStack() })
            }()
        }

        composable(Routes.RESIDENT_NOTICES) {
            residentShell {
                NoticeBoardScreen(userWing = "A", userRoles = listOf(UserRole.OWNER), onBack = { navController.popBackStack() })
            }()
        }

        composable(
            "${Routes.RESIDENT_COMPLAINTS}/{${NavArgs.FLAT_ID}}/{${NavArgs.USER_ID}}",
            arguments = listOf(
                navArgument(NavArgs.FLAT_ID) { type = NavType.StringType },
                navArgument(NavArgs.USER_ID) { type = NavType.StringType })
        ) { backStack ->
            val flatId = backStack.arguments?.getString(NavArgs.FLAT_ID) ?: ""
            val userId = backStack.arguments?.getString(NavArgs.USER_ID) ?: ""
            residentShell {
                ResidentComplaintScreen(flatId = flatId, userId = userId, onBack = { navController.popBackStack() })
            }()
        }

        composable(
            route = "${Routes.RESIDENT_STAFF}/{${NavArgs.FLAT_ID}}",
            arguments = listOf(navArgument(NavArgs.FLAT_ID) { type = NavType.StringType })
        ) { backStack ->
            val flatId = backStack.arguments?.getString(NavArgs.FLAT_ID) ?: ""
            residentShell {
                ResidentStaffScreen(flatId = flatId, onBack = { navController.popBackStack() })
            }()
        }

        composable(
            route = "${Routes.RESIDENT_PAYMENTS}/{${NavArgs.FLAT_ID}}/{${NavArgs.USER_ID}}",
            arguments = listOf(
                navArgument(NavArgs.FLAT_ID) { type = NavType.StringType },
                navArgument(NavArgs.USER_ID) { type = NavType.StringType })
        ) { backStack ->
            val flatId = backStack.arguments?.getString(NavArgs.FLAT_ID) ?: ""
            val userId = backStack.arguments?.getString(NavArgs.USER_ID) ?: ""
            residentShell {
                ResidentPaymentsScreen(flatId = flatId, userId = userId, onBack = { navController.popBackStack() })
            }()
        }

        // --- ADMIN SCREENS ---
        composable(Routes.ADMIN_DASHBOARD) { adminShell { AdminDashboardScreen(navController) }() }
        
        composable(Routes.ADMIN_COMPLAINTS) { 
            adminShell { AdminComplaintScreen(onBack = { navController.popBackStack() }) }() 
        }

        composable(
            route = "${Routes.ADMIN_POST_NOTICE}?${NavArgs.SOCIETY_ID}={${NavArgs.SOCIETY_ID}}",
            arguments = listOf(navArgument(NavArgs.SOCIETY_ID) { 
                type = NavType.StringType
                nullable = true
                defaultValue = null 
            })
        ) { backStack ->
            val societyId = backStack.arguments?.getString(NavArgs.SOCIETY_ID) ?: ""
            adminShell {
                CreateNoticeScreen(
                    adminId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                    societyId = societyId,
                    onBack = { navController.popBackStack() })
            }()
        }
        composable(Routes.ADMIN_MANAGE_FLATS) {
            adminShell {
                AdminSetupScreen(onSetupComplete = { navController.popBackStack() }, onBack = { navController.popBackStack() })
            }()
        }
        composable(
            route = "${Routes.ADMIN_APPROVALS}/{${NavArgs.SOCIETY_ID}}",
            arguments = listOf(navArgument(NavArgs.SOCIETY_ID) { type = NavType.StringType })
        ) { backStack ->
            val societyId = backStack.arguments?.getString(NavArgs.SOCIETY_ID) ?: ""
            adminShell { AdminApprovalScreen(societyId = societyId, onBack = { navController.popBackStack() }) }() 
        }
        composable(
            route = "${Routes.ADMIN_BILLING}/{${NavArgs.SOCIETY_ID}}",
            arguments = listOf(navArgument(NavArgs.SOCIETY_ID) { type = NavType.StringType })
        ) { backStack ->
            val societyId = backStack.arguments?.getString(NavArgs.SOCIETY_ID) ?: ""
            adminShell {
                BillingScreen(
                    userId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                    flatId = null,
                    isAdmin = true,
                    societyId = societyId,
                    onBack = { navController.popBackStack() })
            }()
        }
        composable(
            route = "${Routes.ADMIN_STAFF_REGISTRY}/{${NavArgs.SOCIETY_ID}}",
            arguments = listOf(navArgument(NavArgs.SOCIETY_ID) { type = NavType.StringType })
        ) { backStack ->
            val societyId = backStack.arguments?.getString(NavArgs.SOCIETY_ID) ?: ""
            adminShell { StaffRegistryScreen(societyId = societyId, onBack = { navController.popBackStack() }) }() 
        }

        // --- DIRECTORY SCREEN (Shared) ---
        composable(
            route = "${Routes.SOCIETY_DIRECTORY}/{${NavArgs.IS_ADMIN}}/{${NavArgs.SOCIETY_ID}}",
            arguments = listOf(
                navArgument(NavArgs.IS_ADMIN) { type = NavType.BoolType },
                navArgument(NavArgs.SOCIETY_ID) { type = NavType.StringType }
            )
        ) { backStack ->
            val isAdmin = backStack.arguments?.getBoolean(NavArgs.IS_ADMIN) ?: false
            val societyId = backStack.arguments?.getString(NavArgs.SOCIETY_ID) ?: ""

            val content = @Composable {
                SocietyDirectoryScreen(isAdmin = isAdmin, societyId = societyId, onBack = { navController.popBackStack() })
            }
            if (isAdmin) adminShell { content() }() else residentShell { content() }()
        }

        // --- PROFILE & SETTINGS ---
        composable(
            route = "${Routes.PROFILE_SCREEN}/{${NavArgs.USER_ID}}?type={type}",
            arguments = listOf(
                navArgument(NavArgs.USER_ID) { type = NavType.StringType },
                navArgument("type") { type = NavType.StringType; defaultValue = "resident" })
        ) { backStack ->
            val userId = backStack.arguments?.getString(NavArgs.USER_ID) ?: ""
            val type = backStack.arguments?.getString("type") ?: "resident"
            val viewModel: ProfileViewModel = hiltViewModel()
            val onSwitch = { target: String ->
                if (target == "admin") {
                    navController.navigate(Routes.ADMIN_DASHBOARD) {
                        popUpTo(Routes.ADMIN_DASHBOARD) { inclusive = true }
                    }
                } else {
                    val user = viewModel.uiState.value.userProfile
                    if (user != null && !user.assignedFlatId.isNullOrBlank()) {
                        navController.navigate("${Routes.RESIDENT_HOME}/${user.assignedFlatId}/${user.id}") {
                            popUpTo(Routes.RESIDENT_HOME) { inclusive = true }
                        }
                    }
                }
            }
            val content = @Composable {
                ProfileScreen(
                    userId = userId,
                    currentViewType = type,
                    onSwitchRole = { onSwitch(it) },
                    onBack = { navController.popBackStack() },
                    onLogout = {
                        navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                    }
                )
            }
            if (type == "admin") adminShell { content() }() else residentShell { content() }()
        }

        composable(Routes.SETTINGS_SCREEN) { SettingsScreen(onBack = { navController.popBackStack() }) }

        // --- SECURITY ---
        composable(Routes.SECURITY_DASHBOARD) {
            SecurityScannerScreen(
                onNavigateToManualEntry = { navController.navigate(Routes.SECURITY_MANUAL_ENTRY) },
                onNavigateToStaffAttendance = {
                    // Logic to get societyId for security staff
                    // For now, using a placeholder if profile not loaded
                    navController.navigate("${Routes.SECURITY_STAFF}/${NavArgs.SOCIETY_ID}")
                },
                onLogout = {
                    navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                }
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