package fan.dang.blog.dao

import fan.dang.blog.config.BlogOptions
import fan.dang.blog.db.Database
import fan.dang.blog.db.Database.Companion.getStringMap
import fan.dang.blog.db.Database.Companion.toJson
import io.vertx.sqlclient.Tuple

class OptionsDao(private val db: Database) {

    suspend fun load() {
        val rows = db.query("""SELECT * FROM OPTIONS""")
        rows.forEach { row ->
            val name = row.getString("NAME")
            val value = row.getStringMap("VALUE")
            when (name) {
                "blog_name" -> BlogOptions.blogName = value
                "blog_description" -> BlogOptions.blogDescription = value
                "locales" -> BlogOptions.locales = value
                "datetime_format" -> BlogOptions.datetimeFormat = value
                "default_locale" -> BlogOptions.defaultLocale = value["value"] ?: "en"
                "page_size" -> BlogOptions.pageSize = value["value"]?.toIntOrNull() ?: 10
                "disqus_short_name" -> BlogOptions.disqusShortName = value["value"] ?: ""
                "ga_id" -> BlogOptions.gaId = value["value"] ?: ""
            }
        }
    }

    suspend fun setBlogName(map: Map<String, String>): Int {
        BlogOptions.blogName = map
        return updateOption("blog_name", map)
    }

    suspend fun setBlogDescription(map: Map<String, String>): Int {
        BlogOptions.blogDescription = map
        return updateOption("blog_description", map)
    }

    suspend fun setLocales(map: Map<String, String>): Int {
        BlogOptions.locales = map
        return updateOption("locales", map)
    }

    suspend fun setDatetimeFormat(map: Map<String, String>): Int {
        BlogOptions.datetimeFormat = map
        return updateOption("datetime_format", map)
    }

    suspend fun setDefaultLocale(value: String): Int {
        BlogOptions.defaultLocale = value
        return updateOption("default_locale", mapOf("value" to value))
    }

    suspend fun setPageSize(value: Int): Int {
        BlogOptions.pageSize = value
        return updateOption("page_size", mapOf("value" to value.toString()))
    }

    suspend fun setDisqusShortName(value: String): Int {
        BlogOptions.disqusShortName = value
        return updateOption("disqus_short_name", mapOf("value" to value))
    }

    suspend fun setGAId(value: String): Int {
        BlogOptions.gaId = value
        return updateOption("ga_id", mapOf("value" to value))
    }

    private suspend fun updateOption(name: String, value: Map<String, String>): Int {
        return db.execute(
            """UPDATE OPTIONS SET VALUE = ? WHERE NAME = ?""",
            Tuple.of(value.toJson(), name)
        )
    }
}
