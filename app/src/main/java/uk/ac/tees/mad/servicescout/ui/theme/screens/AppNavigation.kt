package uk.ac.tees.mad.servicescout.ui.theme.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import uk.ac.tees.mad.servicescout.repositories.AuthRepository
import uk.ac.tees.mad.servicescout.repositories.ServiceRepository
import uk.ac.tees.mad.servicescout.ui.theme.viewmodels.ServiceViewModel
import uk.ac.tees.mad.servicescout.ui.theme.viewmodels.AuthViewModel
import uk.ac.tees.mad.servicescout.ui.theme.viewmodels.AuthViewModelFactory
import uk.ac.tees.mad.servicescout.ui.theme.viewmodels.ServiceViewModelFactory

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current

    val authViewModel: AuthViewModel =
        viewModel(factory = AuthViewModelFactory(AuthRepository()))

    val serviceViewModel: ServiceViewModel =
        viewModel(factory = ServiceViewModelFactory(ServiceRepository()))
    LaunchedEffect(Unit) {
        serviceViewModel.fetchCurrentLocation(context)
    }
    NavHost(
        navController = navController,
        startDestination = "splash_screen",
        modifier = modifier
    ) {
        composable("splash_screen") { SplashScreen(navController) }
        composable("login_screen") { LoginScreen(navController, authViewModel) }
        composable("register_screen") { RegisterScreen(navController, authViewModel) }
        composable("home_screen") { HomeScreen(navController, serviceViewModel) }
        composable("add_service_screen") { AddServiceScreen(serviceViewModel, navController) }
        composable("service_details_screen/{serviceId}") { backStackEntry ->
            val serviceId = backStackEntry.arguments?.getString("serviceId") ?: return@composable
            ServiceDetailsScreen(serviceId, serviceViewModel, navController)
        }
        composable("user_profile_screen") { UserProfileScreen(authViewModel, navController) }
    }
}