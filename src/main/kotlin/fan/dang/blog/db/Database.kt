package fan.dang.blog.db

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import fan.dang.blog.config.BlogConfig
import io.vertx.core.Vertx
import io.vertx.jdbcclient.JDBCConnectOptions
import io.vertx.jdbcclient.JDBCPool
import io.vertx.kotlin.coroutines.coAwait
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.PoolOptions
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.Tuple
import java.time.LocalDateTime

class Database(vertx: Vertx, config: BlogConfig) {
    val pool: Pool = JDBCPool.pool(
        vertx,
        JDBCConnectOptions()
            .setJdbcUrl("jdbc:sqlite:${config.dbPath}"),
        PoolOptions()
            .setMaxSize(1) // SQLite only supports one writer at a time
    )

    suspend fun query(sql: String): List<Row> {
        return pool.query(sql).execute().coAwait().toList()
    }

    suspend fun preparedQuery(sql: String, tuple: Tuple): List<Row> {
        return pool.preparedQuery(sql).execute(tuple).coAwait().toList()
    }

    suspend fun execute(sql: String, tuple: Tuple): Int {
        return pool.preparedQuery(sql).execute(tuple).coAwait().rowCount()
    }

    companion object {
        val objectMapper: ObjectMapper = ObjectMapper()
            .registerKotlinModule()
            .registerModule(JavaTimeModule())

        val mapTypeRef = object : TypeReference<Map<String, String>>() {}
        val listTypeRef = object : TypeReference<List<String>>() {}

        fun Row.getStringMap(column: String): Map<String, String> {
            val value = this.getValue(column) ?: return emptyMap()
            return when (value) {
                is Map<*, *> -> value.mapNotNull { (k, v) ->
                    if (k is String && v is String) k to v else null
                }.toMap()
                is String -> objectMapper.readValue(value, mapTypeRef)
                else -> emptyMap()
            }
        }

        fun Row.getStringList(column: String): List<String> {
            val value = this.getValue(column) ?: return emptyList()
            return when (value) {
                is List<*> -> value.filterIsInstance<String>()
                is Array<*> -> value.filterIsInstance<String>()
                is String -> objectMapper.readValue(value, listTypeRef)
                else -> emptyList()
            }
        }

        fun Row.getDateTime(column: String): LocalDateTime? {
            val value = this.getValue(column) ?: return null
            return when (value) {
                is LocalDateTime -> value
                is String -> LocalDateTime.parse(value)
                else -> null
            }
        }

        fun Map<String, String>.toJson(): String {
            return objectMapper.writeValueAsString(this)
        }

        fun List<String>.toJsonArray(): String {
            return objectMapper.writeValueAsString(this)
        }
    }
}
