package fan.dang.blog.config

import fan.dang.blog.models.BlogrollEntity

/**
 * Global blog options loaded from database.
 * This is a singleton that holds cached options.
 */
object BlogOptions {
    var blogName: Map<String, String> = emptyMap()
    var blogDescription: Map<String, String> = emptyMap()
    var locales: Map<String, String> = emptyMap()
    var datetimeFormat: Map<String, String> = emptyMap()
    var defaultLocale: String = "en"
    var pageSize: Int = 10
    var disqusShortName: String = ""
    var gaId: String = ""
    var blogrolls: List<BlogrollEntity> = emptyList()
}
