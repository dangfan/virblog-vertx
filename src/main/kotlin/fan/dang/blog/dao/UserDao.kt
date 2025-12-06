package fan.dang.blog.dao

import at.favre.lib.crypto.bcrypt.BCrypt
import fan.dang.blog.config.BlogConfig
import fan.dang.blog.db.Database
import fan.dang.blog.models.UserEntity
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.Tuple
import java.nio.charset.StandardCharsets
import javax.crypto.SecretKey

class UserDao(private val db: Database, config: BlogConfig) {
    private val jwtKey: SecretKey = Keys.hmacShaKeyFor(
        config.jwtKey.padEnd(32, '0').toByteArray(StandardCharsets.UTF_8)
    )

    private fun Row.toUserEntity(): UserEntity {
        return UserEntity(
            username = getString("USERNAME"),
            password = getString("PASSWORD"),
            email = getString("EMAIL"),
            nickname = getString("NICKNAME")
        )
    }

    suspend fun count(): Int {
        val rows = db.query("""SELECT COUNT(*) as cnt FROM USERS""")
        return rows.firstOrNull()?.getInteger("cnt") ?: 0
    }

    suspend fun create(username: String, password: String): Int {
        val hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray())
        return db.execute(
            """INSERT INTO USERS (USERNAME, PASSWORD) VALUES (?, ?)""",
            Tuple.of(username, hashedPassword)
        )
    }

    suspend fun login(username: String, password: String): String? {
        val rows = db.preparedQuery(
            """SELECT * FROM USERS WHERE USERNAME = ?""",
            Tuple.of(username.trim())
        )
        val user = rows.firstOrNull()?.toUserEntity() ?: return null

        val result = BCrypt.verifyer().verify(password.toCharArray(), user.password)
        if (!result.verified) return null

        return Jwts.builder()
            .claim("name", user.username)
            .signWith(jwtKey)
            .compact()
    }

    suspend fun getUser(token: String): UserEntity? {
        return try {
            val claims = Jwts.parser()
                .verifyWith(jwtKey)
                .build()
                .parseSignedClaims(token)
                .payload

            val username = claims["name"] as? String ?: return null
            val rows = db.preparedQuery(
                """SELECT * FROM USERS WHERE USERNAME = ?""",
                Tuple.of(username)
            )
            rows.firstOrNull()?.toUserEntity()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updatePassword(username: String, newPassword: String, oldPassword: String): Boolean {
        val rows = db.preparedQuery(
            """SELECT * FROM USERS WHERE USERNAME = ?""",
            Tuple.of(username.trim())
        )
        val user = rows.firstOrNull()?.toUserEntity() ?: return false

        val result = BCrypt.verifyer().verify(oldPassword.toCharArray(), user.password)
        if (!result.verified) return false

        val hashedPassword = BCrypt.withDefaults().hashToString(12, newPassword.toCharArray())
        val updated = db.execute(
            """UPDATE USERS SET PASSWORD = ? WHERE USERNAME = ?""",
            Tuple.of(hashedPassword, username.trim())
        )
        return updated == 1
    }

    suspend fun update(user: UserEntity): Boolean {
        val updated = db.execute(
            """UPDATE USERS SET EMAIL = ?, NICKNAME = ? WHERE USERNAME = ?""",
            Tuple.of(user.email, user.nickname, user.username)
        )
        return updated == 1
    }
}
