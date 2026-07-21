package com.example.hytp.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.hytp.core.ui.BottomTab
import com.example.hytp.core.ui.HytpBottomBar
import com.example.hytp.feature.chat.ui.ChatScreen
import com.example.hytp.feature.chat.ui.ConversationListScreen
import com.example.hytp.feature.group.ui.GroupChatScreen
import com.example.hytp.feature.group.ui.GroupListScreen
import com.example.hytp.feature.home.ui.HomeScreen
import com.example.hytp.feature.mine.ui.MineScreen
import com.example.hytp.feature.order.ui.OrderDetailScreen
import com.example.hytp.feature.order.ui.OrderListScreen
import com.example.hytp.feature.order.ui.ReviewScreen
import com.example.hytp.feature.cart.ui.CartScreen
import com.example.hytp.feature.order.ui.CheckoutScreen
import com.example.hytp.feature.shop.ui.MallScreen
import com.example.hytp.feature.shop.ui.ProductDetailScreen
import com.example.hytp.feature.shop.ui.ShopScreen
import com.example.hytp.feature.social.ui.FeedDetailScreen
import com.example.hytp.feature.social.ui.FeedListScreen
import com.example.hytp.feature.social.ui.FeedPublishScreen
import com.example.hytp.feature.social.ui.UserProfileScreen
import com.example.hytp.feature.address.ui.AddressScreen

/**
 * 主页面骨架：底部 4 Tab 导航（首页/社交/商城/我的），每 Tab 独立导航栈。
 * 对齐 docs/dev/15 §6.7 与 docs/dev/04 §6。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLoggedOut: () -> Unit,
) {
    val rootNavController = rememberNavController()

    // 每 Tab 独立的 NavController，保存各自的回退栈
    val homeNavController = rememberNavController()
    val socialNavController = rememberNavController()
    val mallNavController = rememberNavController()
    val mineNavController = rememberNavController()

    var currentTab by rememberSaveable { mutableStateOf(BottomTab.Home) }

    Scaffold(
        bottomBar = {
            HytpBottomBar(
                currentTab = currentTab,
                onTabSelected = { tab ->
                    if (tab == currentTab) {
                        // 已在当前 Tab，点同一 Tab 回根
                        val target = when (tab) {
                            BottomTab.Home -> homeNavController
                            BottomTab.Social -> socialNavController
                            BottomTab.Mall -> mallNavController
                            BottomTab.Mine -> mineNavController
                        }
                        target.navigate(TabRoutes.getRoot(tab)) {
                            popUpTo(TabRoutes.getRoot(tab)) { inclusive = true }
                        }
                    } else {
                        currentTab = tab
                    }
                },
            )
        },
    ) { innerPadding ->
        // 首页 Tab 导航
        NavHost(
            navController = homeNavController,
            startDestination = TabRoutes.HOME_ROOT,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(200)) },
            exitTransition = { androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(200)) },
        ) {
            composable(TabRoutes.HOME_ROOT) {
                HomeScreen(
                    onOpenMall = {
                        currentTab = BottomTab.Mall
                        mallNavController.navigate(TabRoutes.MALL_ROOT) {
                            popUpTo(TabRoutes.MALL_ROOT) { inclusive = true }
                        }
                    },
                    onOpenSocial = {
                        currentTab = BottomTab.Social
                        socialNavController.navigate(TabRoutes.SOCIAL_ROOT) {
                            popUpTo(TabRoutes.SOCIAL_ROOT) { inclusive = true }
                        }
                    },
                )
            }
        }

        // 社交 Tab 导航
        NavHost(
            navController = socialNavController,
            startDestination = TabRoutes.SOCIAL_ROOT,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(200)) },
            exitTransition = { androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(200)) },
        ) {
            composable(TabRoutes.SOCIAL_ROOT) { entry ->
                val published by entry.savedStateHandle
                    .getStateFlow("feedPublished", false)
                    .collectAsStateWithLifecycle()
                FeedListScreen(
                    onBack = null, // Tab 根页面：无返回按钮
                    onFeedClick = { id -> socialNavController.navigate(Routes.feedDetail(id)) },
                    onAuthorClick = { id -> socialNavController.navigate(Routes.userProfile(id)) },
                    onPublish = { socialNavController.navigate(Routes.FEED_PUBLISH) },
                    refreshSignal = published,
                    onRefreshConsumed = { entry.savedStateHandle["feedPublished"] = false },
                )
            }
            composable(Routes.FEED_PUBLISH) {
                FeedPublishScreen(
                    onBack = { socialNavController.popBackStack() },
                    onPublished = {
                        socialNavController.previousBackStackEntry?.savedStateHandle?.set("feedPublished", true)
                        socialNavController.popBackStack()
                    },
                )
            }
            composable(
                route = Routes.FEED_DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
            ) {
                FeedDetailScreen(
                    onBack = { socialNavController.popBackStack() },
                    onAuthorClick = { id -> socialNavController.navigate(Routes.userProfile(id)) },
                )
            }
            composable(
                route = Routes.USER_PROFILE,
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
            ) {
                UserProfileScreen(
                    onBack = { socialNavController.popBackStack() },
                    onFeedClick = { id -> socialNavController.navigate(Routes.feedDetail(id)) },
                    onMessage = { convId, nickname ->
                        socialNavController.navigate(Routes.chat(convId))
                        socialNavController.getBackStackEntry(Routes.CHAT).savedStateHandle["chatTitle"] = nickname
                    },
                )
            }
            composable(Routes.CONVERSATION_LIST) {
                ConversationListScreen(
                    onBack = { socialNavController.popBackStack() },
                    onConversationClick = { convId, nickname ->
                        socialNavController.navigate(Routes.chat(convId))
                        socialNavController.getBackStackEntry(Routes.CHAT).savedStateHandle["chatTitle"] = nickname
                    },
                )
            }
            composable(
                route = Routes.CHAT,
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
            ) { entry ->
                val title = entry.savedStateHandle.get<String>("chatTitle") ?: "私信"
                ChatScreen(
                    title = title,
                    onBack = { socialNavController.popBackStack() },
                )
            }
            composable(Routes.GROUP_LIST) {
                GroupListScreen(
                    onBack = { socialNavController.popBackStack() },
                    onGroupClick = { id -> socialNavController.navigate(Routes.groupChat(id)) },
                )
            }
            composable(
                route = Routes.GROUP_CHAT,
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
            ) {
                GroupChatScreen(
                    onBack = { socialNavController.popBackStack() },
                )
            }
        }

        // 商城 Tab 导航
        NavHost(
            navController = mallNavController,
            startDestination = TabRoutes.MALL_ROOT,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(200)) },
            exitTransition = { androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(200)) },
        ) {
            composable(TabRoutes.MALL_ROOT) {
                MallScreen(
                    onProductClick = { id -> mallNavController.navigate(Routes.productDetail(id)) },
                )
            }
            composable(
                route = Routes.PRODUCT_DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
            ) {
                ProductDetailScreen(
                    onBack = { mallNavController.popBackStack() },
                    onShopClick = { id -> mallNavController.navigate(Routes.shop(id)) },
                    onGoCart = { mallNavController.navigate(Routes.CART) },
                    onRentBooked = { orderNo -> mallNavController.navigate(Routes.orderDetail(orderNo)) },
                )
            }
            composable(
                route = Routes.SHOP,
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
            ) {
                ShopScreen(
                    onBack = { mallNavController.popBackStack() },
                    onProductClick = { id -> mallNavController.navigate(Routes.productDetail(id)) },
                )
            }
            composable(Routes.CART) {
                CartScreen(
                    onBack = { mallNavController.popBackStack() },
                    onCheckout = { mallNavController.navigate(Routes.CHECKOUT) },
                    onProductClick = { id -> mallNavController.navigate(Routes.productDetail(id)) },
                )
            }
            composable(Routes.CHECKOUT) { entry ->
                val pickedAddressId by entry.savedStateHandle
                    .getStateFlow<Long?>("pickedAddressId", null)
                    .collectAsStateWithLifecycle()
                CheckoutScreen(
                    pickedAddressId = pickedAddressId,
                    onBack = { mallNavController.popBackStack() },
                    onManageAddress = { mallNavController.navigate(Routes.ADDRESS) },
                    onOrderCreated = { orderNo ->
                        mallNavController.navigate(Routes.orderDetail(orderNo)) {
                            popUpTo(Routes.CART) { inclusive = true }
                        }
                    },
                )
            }
            composable(Routes.ADDRESS) {
                AddressScreen(
                    onBack = { mallNavController.popBackStack() },
                    onPick = { addressId ->
                        mallNavController.previousBackStackEntry
                            ?.savedStateHandle?.set("pickedAddressId", addressId)
                        mallNavController.popBackStack()
                    },
                )
            }
            composable(
                route = Routes.ORDER_DETAIL,
                arguments = listOf(navArgument("orderNo") { type = NavType.StringType }),
            ) {
                OrderDetailScreen(
                    onBack = { mallNavController.popBackStack() },
                    onReview = { orderNo, productId ->
                        mallNavController.navigate(Routes.review(orderNo, productId))
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
                    onBack = { mallNavController.popBackStack() },
                    onDone = { mallNavController.popBackStack() },
                )
            }
        }

        // 我的 Tab 导航
        NavHost(
            navController = mineNavController,
            startDestination = TabRoutes.MINE_ROOT,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(200)) },
            exitTransition = { androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(200)) },
        ) {
            composable(TabRoutes.MINE_ROOT) {
                MineScreen(
                    onLoggedOut = onLoggedOut,
                    onOpenOrders = { mineNavController.navigate(Routes.ORDER_LIST) },
                )
            }
            composable(Routes.ORDER_LIST) {
                OrderListScreen(
                    onBack = { mineNavController.popBackStack() },
                    onOrderClick = { orderNo -> mineNavController.navigate(Routes.orderDetail(orderNo)) },
                )
            }
            composable(
                route = Routes.ORDER_DETAIL,
                arguments = listOf(navArgument("orderNo") { type = NavType.StringType }),
            ) {
                OrderDetailScreen(
                    onBack = { mineNavController.popBackStack() },
                    onReview = { orderNo, productId ->
                        mineNavController.navigate(Routes.review(orderNo, productId))
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
                    onBack = { mineNavController.popBackStack() },
                    onDone = { mineNavController.popBackStack() },
                )
            }
        }
    }
}
