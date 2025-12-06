package fan.dang.blog.util

import java.text.MessageFormat
import java.util.*

object Messages {
    private val bundles = mutableMapOf<String, Map<String, String>>()

    init {
        // English (default)
        bundles["en"] = mapOf(
            "nav.home" to "Home",
            "nav.about" to "About",
            "nav.language" to "Language",
            "time" to "Posted on {0}",
            "tag" to "Tag:",
            "404.title" to "Page Not Found",
            "404.oops" to "Oops",
            "404.content" to "You requested the page that is no longer There.",
            "404.home" to "Back To Home",
            "pagination.prev" to "Newer Posts",
            "pagination.next" to "Older Posts",
            "main.blogrolls" to "Blogrolls",
            "main.copyright" to "Copyright © 2012 - 2026 DANG Fan"
        )

        // Simplified Chinese
        bundles["zh-Hans"] = mapOf(
            "nav.home" to "主页",
            "nav.about" to "关于",
            "nav.language" to "语言",
            "time" to "发布于{0}",
            "tag" to "标签：",
            "404.title" to "找不到该页",
            "404.oops" to "哦！不！",
            "404.content" to "您访问的页面并不存在",
            "404.home" to "返回首页",
            "pagination.prev" to "上一页",
            "pagination.next" to "下一页",
            "main.blogrolls" to "友情链接",
            "main.copyright" to "版权所有 © 2012 - 2026 党凡"
        )

        // Traditional Chinese
        bundles["zh-Hant"] = mapOf(
            "nav.home" to "主頁",
            "nav.about" to "關於",
            "nav.language" to "語言",
            "time" to "發佈於{0}",
            "tag" to "標籤：",
            "404.title" to "找不到該頁",
            "404.oops" to "哦！不！",
            "404.content" to "您訪問的頁面並不存在",
            "404.home" to "返回首頁",
            "pagination.prev" to "上一頁",
            "pagination.next" to "下一頁",
            "main.blogrolls" to "友情鏈接",
            "main.copyright" to "版權所有 © 2012 - 2026 党凡"
        )
    }

    fun get(key: String, lang: String, vararg args: Any): String {
        val bundle = bundles[lang] ?: bundles["en"] ?: return key
        val pattern = bundle[key] ?: return key
        return if (args.isNotEmpty()) {
            MessageFormat.format(pattern, *args)
        } else {
            pattern
        }
    }
}
