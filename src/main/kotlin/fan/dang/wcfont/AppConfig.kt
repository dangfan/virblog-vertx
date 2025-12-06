package fan.dang.wcfont

import java.io.File
import java.util.Properties

data class AppConfig(
    val fontInputPath: String,
    val fontOutputPath: String,
) {
    companion object {
        fun load(): AppConfig {
            val props = loadProps()

            return AppConfig(
                fontInputPath = props.getProperty("font.input", "fonts/input"),
                fontOutputPath = props.getProperty("font.output", "fonts/output"),
            )
        }

        private fun loadProps(): Properties {
            val props = Properties()
            // Prefer classpath config for packaged app
            AppConfig::class.java.classLoader.getResourceAsStream("application.conf")?.use {
                props.load(it)
            }
            // Allow override via local file for dev
            val configFile = File("application.conf")
            if (configFile.exists()) {
                configFile.inputStream().use { props.load(it) }
            }
            return props
        }
    }
}
