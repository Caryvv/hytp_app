package com.example.hytp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.hytp.feature.auth.ui.LoginScreen
import com.example.hytp.feature.home.ui.HomeScreen
import com.example.hytp.feature.shop.ui.MallScreen
import com.example.hytp.feature.shop.ui.ProductDetailScreen
import com.example.hytp.feature.shop.ui.ShopScreen
import com.example.hytp.feature.splash.ui.SplashScreen

/**
 * 全局导航图（单 NavHost，对齐 docs/dev/04 §6）。
 * 阶段1：splash → login → home。登录成功后清空 login 回退栈。
 */
@Composable
fun HytpNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.SPLASH) {

        composable(Routes.SPLASH) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onLoggedOut = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onOpenMall = { navController.navigate(Routes.MALL) },
            )
        }

        composable(Routes.MALL) {
            MallScreen(
                onProductClick = { id -> navController.navigate(Routes.productDetail(id)) },
            )
        }

        composable(
            route = Routes.PRODUCT_DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) {
            ProductDetailScreen(
                onBack = { navController.popBackStack() },
                onShopClick = { id -> navController.navigate(Routes.shop(id)) },
            )
        }

        composable(
            route = Routes.SHOP,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) {
            ShopScreen(
                onBack = { navController.popBackStack() },
                onProductClick = { id -> navController.navigate(Routes.productDetail(id)) },
            )
        }
    }
}
