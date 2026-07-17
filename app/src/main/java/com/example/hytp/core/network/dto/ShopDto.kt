package com.example.hytp.core.network.dto

/**
 * 交易区（只读浏览）DTO —— 对齐后端 api 入口的商品/分类/店铺响应。
 * 字段名与后端 toListArray / toDetailArray / toArray 的驼峰 key 一致，Moshi 反射解析。
 */

/** 通用分页结构 data：{ list, pagination }（对齐 03 §3 列表分页约定）。 */
data class PageData<T>(
    val list: List<T> = emptyList(),
    val pagination: Pagination = Pagination(),
)

data class Pagination(
    val page: Int = 1,
    val pageSize: Int = 20,
    val total: Int = 0,
)

/** 商品列表卡片项（对齐 Product::toListArray）。 */
data class ProductListItem(
    val id: Long,
    val shopId: Long = 0,
    val title: String = "",
    val categoryId: Int = 0,
    val formeDynasty: Int = 0,
    val formeType: String = "",
    val style: String = "",
    val tradeType: Int = 1,
    val price: String = "0.00",
    val cover: String = "",
    val stock: Int = 0,
    val isOriginal: Int = 0,
    val sales: Int = 0,
    val rating: String = "0.00",
    val status: Int = 1,
)

/** 商品详情（对齐 Product::toDetailArray + skus + shop）。 */
data class ProductDetail(
    val id: Long,
    val shopId: Long = 0,
    val title: String = "",
    val categoryId: Int = 0,
    val formeDynasty: Int = 0,
    val formeType: String = "",
    val style: String = "",
    val tradeType: Int = 1,
    val price: String = "0.00",
    val cover: String = "",
    val stock: Int = 0,
    val isOriginal: Int = 0,
    val sales: Int = 0,
    val rating: String = "0.00",
    val status: Int = 1,
    val images: List<String> = emptyList(),
    val detail: String = "",
    val tryonModelUrl: String? = null,
    val auditRemark: String = "",
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val skus: List<ProductSku> = emptyList(),
    val shop: ShopPublic? = null,
)

/** 商品规格（对齐 ProductSku::toArray）。spec 是尺码/颜色键值。 */
data class ProductSku(
    val id: Long,
    val productId: Long = 0,
    val spec: Map<String, String> = emptyMap(),
    val price: String = "0.00",
    val stock: Int = 0,
    val skuCode: String = "",
)

/** 商品评价（对齐 ProductReview::toArray）。 */
data class Review(
    val id: Long,
    val productId: Long = 0,
    val userId: Long = 0,
    val rating: Int = 5,
    val content: String = "",
    val images: List<String> = emptyList(),
    val sentiment: Int? = null,
    val keywords: List<String> = emptyList(),
    val createdAt: Long = 0,
)

/** 分类节点（对齐 ProductCategory::toArray + children 树）。 */
data class Category(
    val id: Int,
    val parentId: Int = 0,
    val name: String = "",
    val level: Int = 1,
    val sort: Int = 0,
    val icon: String = "",
    val children: List<Category> = emptyList(),
)

/** 店铺公开信息（对齐 Shop::toPublicArray；主页额外带 onSaleCount）。 */
data class ShopPublic(
    val id: Long,
    val name: String = "",
    val logo: String = "",
    val type: Int = 1,
    val region: String = "",
    val creditScore: Int = 100,
    val onSaleCount: Int = 0,
)
