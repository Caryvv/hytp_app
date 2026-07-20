package com.example.hytp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.hytp.feature.address.ui.AddressScreen
import com.example.hytp.feature.auth.ui.LoginScreen
import com.example.hytp.feature.cart.ui.CartScreen
import com.example.hytp.feature.home.ui.HomeScreen
import com.example.hytp.feature.order.ui.CheckoutScreen
import com.example.hytp.feature.order.ui.OrderDetailScreen
import com.example.hytp.feature.order.ui.OrderListScreen
import com.example.hytp.feature.order.ui.ReviewScreen
import com.example.hytp.feature.shop.ui.MallScreen
import com.example.hytp.feature.shop.ui.ProductDetailScreen
import com.example.hytp.feature.shop.ui.ShopScreen
import com.example.hytp.feature.social.ui.FeedDetailScreen
import com.example.hytp.feature.social.ui.FeedListScreen
import com.example.hytp.feature.social.ui.FeedPublishScreen
import com.example.hytp.feature.social.ui.UserProfileScreen
import com.example.hytp.feature.splash.ui.SplashScreen

/**
 * 全局导航图（单 NavHost，对齐 docs/dev/04 §6）。
 * 阶段1：splash → login → home。阶段2：商城/详情/店铺。阶段3：购物车/结算/订单/评价。
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
                onOpenOrders = { navController.navigate(Routes.ORDER_LIST) },
                onOpenSocial = { navController.navigate(Routes.FEED_LIST) },
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
                onGoCart = { navController.navigate(Routes.CART) },
                onRentBooked = { orderNo -> navController.navigate(Routes.orderDetail(orderNo)) },
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

        // ---------------- 交易闭环（阶段3） ----------------

        composable(Routes.CART) {
            CartScreen(
                onBack = { navController.popBackStack() },
                onCheckout = { navController.navigate(Routes.CHECKOUT) },
                onProductClick = { id -> navController.navigate(Routes.productDetail(id)) },
            )
        }

        composable(Routes.CHECKOUT) { entry ->
            val pickedAddressId by entry.savedStateHandle
                .getStateFlow<Long?>("pickedAddressId", null)
                .collectAsStateWithLifecycle()
            CheckoutScreen(
                pickedAddressId = pickedAddressId,
                onBack = { navController.popBackStack() },
                onManageAddress = { navController.navigate(Routes.ADDRESS) },
                onOrderCreated = { orderNo ->
                    // 下单成功：进订单详情去支付，清掉购物车/结算回退栈
                    navController.navigate(Routes.orderDetail(orderNo)) {
                        popUpTo(Routes.CART) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.ADDRESS) {
            AddressScreen(
                onBack = { navController.popBackStack() },
                onPick = { addressId ->
                    // 回传选中地址给结算页
                    navController.previousBackStackEntry
                        ?.savedStateHandle?.set("pickedAddressId", addressId)
                    navController.popBackStack()
                },
            )
        }

        composable(Routes.ORDER_LIST) {
            OrderListScreen(
                onBack = { navController.popBackStack() },
                onOrderClick = { orderNo -> navController.navigate(Routes.orderDetail(orderNo)) },
            )
        }

        composable(
            route = Routes.ORDER_DETAIL,
            arguments = listOf(navArgument("orderNo") { type = NavType.StringType }),
        ) {
            OrderDetailScreen(
                onBack = { navController.popBackStack() },
                onReview = { orderNo, productId ->
                    navController.navigate(Routes.review(orderNo, productId))
                },
            )
        }

        composable(
            route = Routes.REVIEW,
            arguments = listOf(
                navArgument("orderNo") { type = NavType.StringType },
                navArgument("productId") { type = NavType.StringType },
            ),
        ) {
            ReviewScreen(
                onBack = { navController.popBackStack() },
                onDone = { navController.popBackStack() },
            )
        }

        // ---------------- 社交（阶段4 P0） ----------------

        composable(Routes.FEED_LIST) { entry ->
            val published by entry.savedStateHandle
                .getStateFlow("feedPublished", false)
                .collectAsStateWithLifecycle()
            FeedListScreen(
                onBack = { navController.popBackStack() },
                onFeedClick = { id -> navController.navigate(Routes.feedDetail(id)) },
                onAuthorClick = { id -> navController.navigate(Routes.userProfile(id)) },
                onPublish = { navController.navigate(Routes.FEED_PUBLISH) },
                refreshSignal = published,
                onRefreshConsumed = { entry.savedStateHandle["feedPublished"] = false },
            )
        }

        composable(Routes.FEED_PUBLISH) {
            FeedPublishScreen(
                onBack = { navController.popBackStack() },
                onPublished = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("feedPublished", true)
                    navController.popBackStack()
                },
            )
        }

        composable(
            route = Routes.FEED_DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) {
            FeedDetailScreen(
                onBack = { navController.popBackStack() },
                onAuthorClick = { id -> navController.navigate(Routes.userProfile(id)) },
            )
        }

        composable(
            route = Routes.USER_PROFILE,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) {
            UserProfileScreen(
                onBack = { navController.popBackStack() },
                onFeedClick = { id -> navController.navigate(Routes.feedDetail(id)) },
            )
        }
    }
}
