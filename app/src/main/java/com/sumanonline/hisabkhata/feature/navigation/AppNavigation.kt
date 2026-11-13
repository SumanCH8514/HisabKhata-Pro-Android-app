package com.sumanonline.hisabkhata.feature.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.sumanonline.hisabkhata.core.designsystem.components.BottomTab
import com.sumanonline.hisabkhata.feature.about.AboutProjectScreen
import com.sumanonline.hisabkhata.feature.auth.AuthViewModel
import com.sumanonline.hisabkhata.feature.auth.ForgotPasswordScreen
import com.sumanonline.hisabkhata.feature.auth.LoginScreen
import com.sumanonline.hisabkhata.feature.auth.RegisterScreen
import com.sumanonline.hisabkhata.feature.customer.CustomerLedgerScreen
import com.sumanonline.hisabkhata.feature.customer.CustomerListScreen
import com.sumanonline.hisabkhata.feature.customer.CustomerViewModel
import com.sumanonline.hisabkhata.feature.payments.PaymentsScreen
import com.sumanonline.hisabkhata.feature.payments.PaymentsViewModel
import com.sumanonline.hisabkhata.feature.profile.AccountProfileSettingsScreen
import com.sumanonline.hisabkhata.feature.profile.ApplicationSettingsScreen
import com.sumanonline.hisabkhata.feature.profile.PreferencesSettingsScreen
import com.sumanonline.hisabkhata.feature.profile.ProfileScreen
import com.sumanonline.hisabkhata.feature.profile.ProfileViewModel
import com.sumanonline.hisabkhata.feature.reminders.RemindersScreen
import com.sumanonline.hisabkhata.feature.reminders.RemindersViewModel
import com.sumanonline.hisabkhata.feature.reports.CustomerReportScreen
import com.sumanonline.hisabkhata.feature.reports.ReportsScreen
import com.sumanonline.hisabkhata.feature.reports.ReportsViewModel
import com.sumanonline.hisabkhata.feature.splash.SplashScreen
import kotlinx.coroutines.flow.StateFlow

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel(),
    isBiometricAuthenticated: Boolean = true,
    deepLinkFlow: StateFlow<Uri?>? = null
) {
    // Handle runtime deep link events (e.g., when an email link is clicked while app is in background or running)
    LaunchedEffect(deepLinkFlow) {
        deepLinkFlow?.collect { uri ->
            if (uri != null && authViewModel.isUserLoggedIn()) {
                val targetRoute = resolveRouteFromUri(uri)
                if (targetRoute != null) {
                    navController.navigate(targetRoute) {
                        popUpTo(Screen.Customers.route) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                isBiometricAuthenticated = isBiometricAuthenticated,
                onSplashFinished = {
                    val deepLinkTarget = deepLinkFlow?.value?.let { resolveRouteFromUri(it) }
                    val nextRoute = if (authViewModel.isUserLoggedIn()) {
                        deepLinkTarget ?: Screen.Customers.route
                    } else {
                        Screen.Login.route
                    }
                    navController.navigate(nextRoute) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                onLoginSuccess = {
                    val deepLinkTarget = deepLinkFlow?.value?.let { resolveRouteFromUri(it) }
                    val nextRoute = deepLinkTarget ?: Screen.Customers.route
                    navController.navigate(nextRoute) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Screen.Customers.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                viewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Customers.route) {
            val customerViewModel: CustomerViewModel = hiltViewModel()
            CustomerListScreen(
                viewModel = customerViewModel,
                onCustomerClick = { customerId ->
                    navController.navigate(Screen.CustomerLedger.createRoute(customerId))
                },
                onTabSelected = { tab ->
                    handleBottomNavigation(navController, tab)
                },
                onLogoutSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.CustomerLedger.route,
            arguments = listOf(navArgument("customerId") { type = NavType.StringType }),
            deepLinks = listOf(
                navDeepLink { uriPattern = "https://hisabkhata.sumanonline.com/customer/{customerId}" },
                navDeepLink { uriPattern = "https://hisabkhata.sumanonline.com/ledger/{customerId}" },
                navDeepLink { uriPattern = "https://hisabkhata.sumanonline.com/transaction/{customerId}" },
                navDeepLink { uriPattern = "hisabkhata://customer/{customerId}" },
                navDeepLink { uriPattern = "hisabkhata://ledger/{customerId}" },
                navDeepLink { uriPattern = "hisabkhata://transaction/{customerId}" }
            )
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getString("customerId") ?: return@composable
            val customerViewModel: CustomerViewModel = hiltViewModel()
            CustomerLedgerScreen(
                customerId = customerId,
                viewModel = customerViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToReport = { id ->
                    navController.navigate(Screen.CustomerReport.createRoute(id))
                }
            )
        }

        composable(Screen.Reports.route) {
            val reportsViewModel: ReportsViewModel = hiltViewModel()
            ReportsScreen(
                viewModel = reportsViewModel,
                onTabSelected = { tab ->
                    handleBottomNavigation(navController, tab)
                }
            )
        }

        composable(
            route = Screen.CustomerReport.route,
            arguments = listOf(navArgument("customerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getString("customerId") ?: return@composable
            val customerViewModel: CustomerViewModel = hiltViewModel()
            CustomerReportScreen(
                customerId = customerId,
                customerViewModel = customerViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Payments.route,
            deepLinks = listOf(
                navDeepLink { uriPattern = "https://hisabkhata.sumanonline.com/payments.*" },
                navDeepLink { uriPattern = "https://hisabkhata.sumanonline.com/verify-payment.*" },
                navDeepLink { uriPattern = "https://hisabkhata.sumanonline.com/payment.*" },
                navDeepLink { uriPattern = "https://hisabkhata.sumanonline.com/verify.*" },
                navDeepLink { uriPattern = "https://backend.hisabkhata.sumanonline.com/payments.*" },
                navDeepLink { uriPattern = "hisabkhata://payments.*" },
                navDeepLink { uriPattern = "hisabkhata://payment.*" },
                navDeepLink { uriPattern = "hisabkhata://verify.*" },
                navDeepLink { uriPattern = "hisabkhata://verify-payment.*" }
            )
        ) {
            val paymentsViewModel: PaymentsViewModel = hiltViewModel()
            PaymentsScreen(
                viewModel = paymentsViewModel,
                onTabSelected = { tab ->
                    handleBottomNavigation(navController, tab)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToLedger = { customerId ->
                    navController.navigate(Screen.CustomerLedger.createRoute(customerId))
                }
            )
        }

        composable(Screen.Reminders.route) {
            val remindersViewModel: RemindersViewModel = hiltViewModel()
            RemindersScreen(
                viewModel = remindersViewModel,
                onTabSelected = { tab ->
                    handleBottomNavigation(navController, tab)
                },
                onCustomerClick = { customerId ->
                    navController.navigate(Screen.CustomerLedger.createRoute(customerId))
                }
            )
        }

        composable(Screen.Profile.route) {
            val profileViewModel: ProfileViewModel = hiltViewModel()
            ProfileScreen(
                viewModel = profileViewModel,
                onTabSelected = { tab ->
                    handleBottomNavigation(navController, tab)
                },
                onLogoutSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToAboutProject = {
                    navController.navigate(Screen.AboutProject.route)
                },
                onNavigateToAccountSettings = {
                    navController.navigate(Screen.AccountSettings.route)
                },
                onNavigateToApplicationSettings = {
                    navController.navigate(Screen.ApplicationSettings.route)
                },
                onNavigateToPreferencesSettings = {
                    navController.navigate(Screen.PreferencesSettings.route)
                }
            )
        }

        composable(Screen.AccountSettings.route) {
            val profileViewModel: ProfileViewModel = hiltViewModel()
            AccountProfileSettingsScreen(
                viewModel = profileViewModel,
                onNavigateBack = { navController.popBackStack() },
                onLogoutSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ApplicationSettings.route) {
            val profileViewModel: ProfileViewModel = hiltViewModel()
            ApplicationSettingsScreen(
                profileViewModel = profileViewModel,
                preferencesManager = profileViewModel.preferencesManager,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.PreferencesSettings.route) {
            val profileViewModel: ProfileViewModel = hiltViewModel()
            PreferencesSettingsScreen(
                preferencesManager = profileViewModel.preferencesManager,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AboutProject.route) {
            AboutProjectScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

private fun handleBottomNavigation(navController: NavHostController, tab: BottomTab) {
    val targetRoute = when (tab) {
        BottomTab.CUSTOMERS -> Screen.Customers.route
        BottomTab.REPORTS -> Screen.Reports.route
        BottomTab.PAYMENTS -> Screen.Payments.route
        BottomTab.REMINDERS -> Screen.Reminders.route
        BottomTab.MORE -> Screen.Profile.route
    }

    if (navController.currentDestination?.route != targetRoute) {
        navController.navigate(targetRoute) {
            popUpTo(Screen.Customers.route) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }
}

private fun resolveRouteFromUri(uri: Uri): String? {
    val scheme = uri.scheme?.lowercase() ?: return null
    val host = uri.host?.lowercase() ?: ""
    val path = uri.path ?: ""
    val segments = uri.pathSegments

    if (scheme == "hisabkhata") {
        return when {
            host.startsWith("payment") || host.startsWith("verify") -> Screen.Payments.route
            host.startsWith("customer") || host.startsWith("ledger") || host.startsWith("transaction") || host.startsWith("entry") -> {
                val customerId = segments.firstOrNull()
                    ?: uri.getQueryParameter("customerId")
                    ?: uri.getQueryParameter("customer_id")
                    ?: uri.getQueryParameter("id")
                if (!customerId.isNullOrBlank()) Screen.CustomerLedger.createRoute(customerId) else Screen.Customers.route
            }
            path.startsWith("/payment") || path.startsWith("/verify") -> Screen.Payments.route
            path.startsWith("/customer") || path.startsWith("/ledger") || path.startsWith("/transaction") || path.startsWith("/entry") -> {
                val customerId = segments.getOrNull(1)
                    ?: uri.getQueryParameter("customerId")
                    ?: uri.getQueryParameter("customer_id")
                    ?: uri.getQueryParameter("id")
                if (!customerId.isNullOrBlank()) Screen.CustomerLedger.createRoute(customerId) else Screen.Customers.route
            }
            else -> Screen.Payments.route
        }
    }

    if (scheme == "http" || scheme == "https") {
        if (host.contains("hisabkhata")) {
            if (path.startsWith("/payment") || path.startsWith("/verify")) {
                return Screen.Payments.route
            }
            if (path.startsWith("/customer") || path.startsWith("/ledger") || path.startsWith("/transaction") || path.startsWith("/entry")) {
                val customerId = segments.getOrNull(1)
                    ?: uri.getQueryParameter("customerId")
                    ?: uri.getQueryParameter("customer_id")
                    ?: uri.getQueryParameter("id")
                return if (!customerId.isNullOrBlank()) Screen.CustomerLedger.createRoute(customerId) else Screen.Customers.route
            }
        }
    }

    return null
}

