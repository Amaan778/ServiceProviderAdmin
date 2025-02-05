package com.app.serviceprovideradmin.Category

data class Category(
    val name: String? = null,
    val imageUrl: String? = null,
    val key: String? = null // Add key to hold the unique Firebase key
)