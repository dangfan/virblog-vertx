package fan.dang.blog.config

import java.io.File
import java.util.Properties

data class BlogConfig(
    val dbPath: String = "data/virblog.db",
    val jwtKey: String = "123456",
    val availableLocales: List<String> = listOf("zh-Hans", "zh-Hant", "en"),
    val port: Int = 8080,
    val baseUrl: String = ""
) {
    companion object {
        fun load(): BlogConfig {
            val props = loadProps()

            return BlogConfig(
                dbPath = props.getProperty("db.path", "data/virblog.db"),
                jwtKey = props.getProperty("jwt.key", "123456"),
                availableLocales = props.getProperty("locales", "zh-Hans,zh-Hant,en")
                    .split(",").map { it.trim() },
                port = props.getProperty("port", "8080").toInt(),
                baseUrl = props.getProperty("base.url", "")
            )
        }

        private fun loadProps(): Properties {
            val props = Properties()
            BlogConfig::class.java.classLoader.getResourceAsStream("application.conf")?.use {
                props.load(it)
            }
            val configFile = File("application.conf")
            if (configFile.exists()) {
                configFile.inputStream().use { props.load(it) }
            }
            return props
        }
    }
}
