package fan.dang.blog.auth

import fan.dang.blog.models.UserEntity
import fan.dang.blog.service.UserService
import io.vertx.core.Vertx
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Authentication handler for protected routes.
 * Validates JWT token from session cookie and attaches user to context.
 */
class AuthHandler(
    private val vertx: Vertx,
    private val userService: UserService
) {
    companion object {
        const val USER_KEY = "authenticated_user"
        const val SESSION_COOKIE = "PLAY_SESSION"
    }

    fun handle(ctx: RoutingContext) {
        CoroutineScope(vertx.dispatcher()).launch {
            try {
                val token = extractToken(ctx)
                if (token == null) {
                    sendUnauthorized(ctx)
                    return@launch
                }

                val user = userService.getUser(token)
                if (user == null) {
                    sendUnauthorized(ctx)
                    return@launch
                }

                ctx.put(USER_KEY, user)
                ctx.next()
            } catch (e: Exception) {
                sendUnauthorized(ctx)
            }
        }
    }

    private fun extractToken(ctx: RoutingContext): String? {
        // Try to get token from session cookie (Play-style)
        val cookie = ctx.request().getCookie(SESSION_COOKIE)
        if (cookie != null) {
            val value = cookie.value
            // Play session format: token=<jwt_token>
            val tokenMatch = Regex("token=([^&]+)").find(value)
            if (tokenMatch != null) {
                return java.net.URLDecoder.decode(tokenMatch.groupValues[1], "UTF-8")
            }
        }

        // Also try Authorization header
        val authHeader = ctx.request().getHeader("Authorization")
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7)
        }

        return null
    }

    private fun sendUnauthorized(ctx: RoutingContext) {
        ctx.response()
            .setStatusCode(401)
            .putHeader("Content-Type", "application/json")
            .end("""{"status":"err","message":"Invalid username and password"}""")
    }
}

fun RoutingContext.getUser(): UserEntity {
    return this.get(AuthHandler.USER_KEY)
}
