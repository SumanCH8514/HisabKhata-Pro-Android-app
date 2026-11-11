package com.sumanonline.hisabkhata.feature.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sumanonline.hisabkhata.core.designsystem.components.BottomTab
import com.sumanonline.hisabkhata.feature.auth.AuthViewModel
import com.sumanonline.hisabkhata.feature.auth.ForgotPasswordScreen
import com.sumanonline.hisabkhata.feature.auth.LoginScreen
import com.sumanonline.hisabkhata.feature.auth.RegisterScreen
import com.sumanonline.hisabkhata.feature.customer.CustomerLedgerScreen
import com.sumanonline.hisabkhata.feature.customer.CustomerListScreen
import com.sumanonline.hisabkhata.feature.customer.CustomerViewModel
import com.sumanonline.hisabkhata.feature.payments.PaymentsScreen
import com.sumanonline.hisabkhata.feature.payments.PaymentsViewModel
import com.sumanonline.hisabkhata.feature.profile.ProfileScreen
import com.sumanonline.hisabkhata.feature.profile.ProfileViewModel
import com.sumanonline.hisabkhata.feature.reminders.RemindersScreen
import com.sumanonline.hisabkhata.feature.reminders.RemindersViewModel
import com.sumanonline.hisabkhata.feature.reports.CustomerReportScreen
import com.sumanonline.hisabkhata.feature.reports.ReportsScreen
import com.sumanonline.hisabkhata.feature.reports.ReportsViewModel
import com.sumanonline.hisabkhata.feature.splash.SplashScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel(),
    isBiometricAuthenticated: Boolean = true
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                isBiometricAuthenticated = isBiometricAuthenticated,
                onSplashFinished = {
                    val nextRoute = if (authViewModel.isUserLoggedIn()) {
                        Screen.Customers.route
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
                    navController.navigate(Screen.Customers.route) {
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
            arguments = listOf(navArgument("customerId") { type = NavType.StringType })
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

        composable(Screen.Payments.route) {
            val paymentsViewModel: PaymentsViewModel = hiltViewModel()
            PaymentsScreen(
                viewModel = paymentsViewModel,
                onTabSelected = { tab ->
                    handleBottomNavigation(navController, tab)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
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
                }
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
