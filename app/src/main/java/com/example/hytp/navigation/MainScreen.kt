package com.example.hytp.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.hytp.core.ui.BottomTab
import com.example.hytp.core.ui.HytpBottomBar
import com.example.hytp.feature.chat.ui.ChatScreen
import com.example.hytp.feature.chat.ui.ConversationListScreen
import com.example.hytp.feature.group.ui.GroupChatScreen
import com.example.hytp.feature.group.ui.GroupListScreen
import com.example.hytp.feature.home.ui.HomeScreen
import com.example.hytp.feature.message.ui.MessageCenterScreen
import com.example.hytp.feature.mine.ui.MineScreen
import com.example.hytp.feature.mine.ui.TaskScreen
import com.example.hytp.feature.mine.ui.RechargeScreen
import com.example.hytp.feature.mine.ui.WithdrawScreen
import com.example.hytp.feature.order.ui.OrderDetailScreen
import com.example.hytp.feature.order.ui.OrderListScreen
import com.example.hytp.feature.order.ui.ReviewScreen
import com.example.hytp.feature.cart.ui.CartScreen
import com.example.hytp.feature.order.ui.CheckoutScreen
import com.example.hytp.feature.shop.ui.MallScreen
import com.example.hytp.feature.shop.ui.ProductDetailScreen
import com.example.hytp.feature.shop.ui.SearchScreen
import com.example.hytp.feature.shop.ui.ShopScreen
import com.example.hytp.feature.social.ui.FeedDetailScreen
import com.example.hytp.feature.social.ui.FeedListScreen
import com.example.hytp.feature.social.ui.FeedPublishScreen
import com.example.hytp.feature.social.ui.UserProfileScreen
import com.example.hytp.feature.address.ui.AddressScreen
import com.example.hytp.feature.ai.ui.QaScreen

/**
 * 主页面骨架：底部 4 Tab 导航（首页/社交/商城/我的），每 Tab 独立导航栈。
 * 只会渲染当前选中 Tab 的 NavHost（其余 Tab 的 NavHost 不可见，但 NavController 存活保持回退栈）。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLoggedOut: () -> Unit,
) {
    // 每 Tab 独立的 NavController，保存各自的回退栈
    val homeNavController = rememberNavController()
    val socialNavController = rememberNavController()
    val mallNavController = rememberNavController()
    val mineNavController = rememberNavController()

    var currentTab by rememberSaveable { mutableStateOf(BottomTab.Home) }
    // 社交 tab 发布动态后置脏，切回首页时触发推荐流刷新（跨 tab 信号）
    var homeFeedDirty by rememberSaveable { mutableStateOf(false) }

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
                            BottomTab.Qa -> null // 智能问答无子页面，再次点击无需回根
                        }
                        target?.navigate(TabRoutes.getRoot(tab)) {
                            popUpTo(TabRoutes.getRoot(tab)) { inclusive = true }
                        }
                    } else {
                        currentTab = tab
                    }
                },
            )
        },
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding)

        // 应用内更新检查（进程内仅一次，有新版弹窗）
        com.example.hytp.feature.update.UpdateGate()

        // 只渲染当前选中 Tab 的 NavHost，其余不可见（但 NavController 存活）
        AnimatedContent(
            targetState = currentTab,
            transitionSpec = {
                fadeIn(androidx.compose.animation.core.tween(150)) togetherWith
                    fadeOut(androidx.compose.animation.core.tween(150))
            },
            label = "tab",
        ) { tab ->
            when (tab) {
                BottomTab.Home -> HomeNavHost(
                    homeNavController, contentModifier, socialNavController, mallNavController, mineNavController,
                    feedDirty = homeFeedDirty,
                    onFeedRefreshed = { homeFeedDirty = false },
                    onSwitchTab = { currentTab = it },
                )

                BottomTab.Social -> SocialNavHost(
                    socialNavController, contentModifier,
                    onFeedPublished = { homeFeedDirty = true },
                )

                BottomTab.Qa -> QaScreen(modifier = contentModifier)

                BottomTab.Mall -> MallNavHost(
                    mallNavController, contentModifier,
                )

                BottomTab.Mine -> MineNavHost(
                    mineNavController, contentModifier, onLoggedOut,
                )
            }
        }
    }
}

// ── 各 Tab 独立的 NavHost ──

@Composable
private fun HomeNavHost(
    navController: androidx.navigation.NavHostController,
    modifier: Modifier,
    socialNavController: androidx.navigation.NavHostController,
    mallNavController: androidx.navigation.NavHostController,
    mineNavController: androidx.navigation.NavHostController,
    feedDirty: Boolean,
    onFeedRefreshed: () -> Unit,
    onSwitchTab: (BottomTab) -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = TabRoutes.HOME_ROOT,
        modifier = modifier,
    ) {
        composable(TabRoutes.HOME_ROOT) {
            HomeScreen(
                onOpenMall = {
                    onSwitchTab(BottomTab.Mall)
                    mallNavController.navigate(TabRoutes.MALL_ROOT) {
                        popUpTo(TabRoutes.MALL_ROOT) { inclusive = true }
                    }
                },
                onOpenSocial = {
                    onSwitchTab(BottomTab.Social)
                    socialNavController.navigate(TabRoutes.SOCIAL_ROOT) {
                        popUpTo(TabRoutes.SOCIAL_ROOT) { inclusive = true }
                    }
                },
                onOpenMessages = {
                    onSwitchTab(BottomTab.Mine)
                    mineNavController.navigate(Routes.MESSAGE_CENTER) {
                        popUpTo(TabRoutes.MINE_ROOT) { inclusive = true }
                    }
                },
                onOpenSearch = {
                    onSwitchTab(BottomTab.Mall)
                    mallNavController.navigate(Routes.SEARCH)
                },
                onFeedClick = { id ->
                    onSwitchTab(BottomTab.Social)
                    socialNavController.navigate(Routes.feedDetail(id))
                },
                onAuthorClick = { id ->
                    onSwitchTab(BottomTab.Social)
                    socialNavController.navigate(Routes.userProfile(id))
                },
                refreshSignal = feedDirty,
                onRefreshConsumed = onFeedRefreshed,
            )
        }
    }
}

@Composable
private fun SocialNavHost(
    navController: androidx.navigation.NavHostController,
    modifier: Modifier,
    onFeedPublished: () -> Unit = {},
) {
    NavHost(
        navController = navController,
        startDestination = TabRoutes.SOCIAL_ROOT,
        modifier = modifier,
    ) {
        composable(TabRoutes.SOCIAL_ROOT) { entry ->
            val published by entry.savedStateHandle
                .getStateFlow("feedPublished", false)
                .collectAsStateWithLifecycle()
            FeedListScreen(
                onBack = null,
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
                    onFeedPublished()
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
                onMessage = { convId, nickname ->
                    navController.navigate(Routes.chat(convId))
                    navController.getBackStackEntry(Routes.CHAT).savedStateHandle["chatTitle"] = nickname
                },
            )
        }
        composable(Routes.CONVERSATION_LIST) {
            ConversationListScreen(
                onBack = { navController.popBackStack() },
                onConversationClick = { convId, nickname ->
                    navController.navigate(Routes.chat(convId))
                    navController.getBackStackEntry(Routes.CHAT).savedStateHandle["chatTitle"] = nickname
                },
            )
        }
        composable(
            route = Routes.CHAT,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            val title = entry.savedStateHandle.get<String>("chatTitle") ?: "私信"
            ChatScreen(title = title, onBack = { navController.popBackStack() })
        }
        composable(Routes.GROUP_LIST) {
            GroupListScreen(
                onBack = { navController.popBackStack() },
                onGroupClick = { id -> navController.navigate(Routes.groupChat(id)) },
            )
        }
        composable(
            route = Routes.GROUP_CHAT,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) {
            GroupChatScreen(onBack = { navController.popBackStack() })
        }
    }
}

@Composable
private fun MallNavHost(
    navController: androidx.navigation.NavHostController,
    modifier: Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = TabRoutes.MALL_ROOT,
        modifier = modifier,
    ) {
        composable(TabRoutes.MALL_ROOT) {
            MallScreen(
                onProductClick = { id -> navController.navigate(Routes.productDetail(id)) },
            )
        }
        composable(Routes.SEARCH) {
            SearchScreen(
                onBack = { navController.popBackStack() },
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
                    navController.previousBackStackEntry
                        ?.savedStateHandle?.set("pickedAddressId", addressId)
                    navController.popBackStack()
                },
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
    }
}

@Composable
private fun MineNavHost(
    navController: androidx.navigation.NavHostController,
    modifier: Modifier,
    onLoggedOut: () -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = TabRoutes.MINE_ROOT,
        modifier = modifier,
    ) {
        composable(TabRoutes.MINE_ROOT) { entry ->
            // 充值成功后回传信号，回到「我的」时刷新余额
            val rechargedCoin = entry.savedStateHandle
                .getStateFlow<Int?>("recharged_coin", null)
                .collectAsStateWithLifecycle()
            MineScreen(
                onLoggedOut = onLoggedOut,
                onOpenOrders = { navController.navigate(Routes.ORDER_LIST) },
                onOpenRecharge = { navController.navigate(Routes.RECHARGE) },
                onOpenWithdraw = { balanceCoin -> navController.navigate(Routes.withdraw(balanceCoin)) },
                onOpenTasks = { navController.navigate(Routes.TASKS) },
                onOpenMessages = { navController.navigate(Routes.MESSAGE_CENTER) },
                refreshSignal = rechargedCoin.value,
                onRefreshConsumed = { entry.savedStateHandle["recharged_coin"] = null },
            )
        }
        composable(Routes.RECHARGE) {
            RechargeScreen(
                onBack = { navController.popBackStack() },
                onRecharged = { coin ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle?.set("recharged_coin", coin)
                    navController.popBackStack()
                },
            )
        }
        composable(
            route = Routes.WITHDRAW,
            arguments = listOf(navArgument("balanceCoin") { type = NavType.StringType }),
        ) {
            WithdrawScreen(
                onBack = { navController.popBackStack() },
                onWithdrawn = { coin ->
                    // 复用「我的」余额刷新信号（任意非空值即触发 loadProfile）
                    navController.previousBackStackEntry
                        ?.savedStateHandle?.set("recharged_coin", coin)
                    navController.popBackStack()
                },
            )
        }
        composable(Routes.TASKS) {
            TaskScreen(
                onBack = { navController.popBackStack() },
                onEarned = { balanceCoin ->
                    // 复用「我的」余额刷新信号（任意非空值即触发 loadProfile）
                    navController.previousBackStackEntry
                        ?.savedStateHandle?.set("recharged_coin", balanceCoin)
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
        // ── 消息中心（私信 + 社群聚合） ──
        composable(Routes.MESSAGE_CENTER) {
            MessageCenterScreen(
                onBack = { navController.popBackStack() },
                onConversationClick = { convId, nickname ->
                    navController.navigate(Routes.chat(convId))
                    navController.getBackStackEntry(Routes.CHAT).savedStateHandle["chatTitle"] = nickname
                },
                onGroupClick = { id -> navController.navigate(Routes.groupChat(id)) },
            )
        }
        composable(
            route = Routes.CHAT,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            val title = entry.savedStateHandle.get<String>("chatTitle") ?: "私信"
            ChatScreen(title = title, onBack = { navController.popBackStack() })
        }
        composable(
            route = Routes.GROUP_CHAT,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) {
            GroupChatScreen(onBack = { navController.popBackStack() })
        }
    }
}
