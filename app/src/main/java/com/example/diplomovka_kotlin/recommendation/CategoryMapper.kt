package com.example.diplomovka_kotlin.recommendation

import com.example.diplomovka_kotlin.data.models.CATEGORY_MAP

object CategoryMapper {
    // Reverse map: subcategory → main category
    private val subToMain: Map<String, String> by lazy {
        val map = mutableMapOf<String, String>()
        CATEGORY_MAP.forEach { (main, subs) ->
            subs.forEach { sub -> map[sub] = main }
        }
        map
    }

    fun getMainCategory(subcategory: String): String =
        subToMain[subcategory] ?: "Iné"
}
