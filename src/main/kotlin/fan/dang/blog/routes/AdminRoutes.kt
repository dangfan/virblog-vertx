package fan.dang.blog.routes

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import fan.dang.blog.auth.AuthHandler
import fan.dang.blog.auth.getUser
import fan.dang.blog.models.*
import fan.dang.blog.service.BlogrollService
import fan.dang.blog.service.OptionsService
import fan.dang.blog.service.PostService
import fan.dang.blog.service.TagService
import fan.dang.blog.service.UserService
import io.vertx.core.Vertx
import io.vertx.core.http.Cookie
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import fan.dang.web.coroutineHandler
import java.net.URLEncoder

class AdminRoutes(
    private val vertx: Vertx,
    private val userService: UserService,
    private val postService: PostService,
    private val tagService: TagService,
    private val optionsService: OptionsService,
    private val blogrollService: BlogrollService
) {
    private val objectMapper = ObjectMapper()
        .registerKotlinModule()
        .registerModule(JavaTimeModule())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    private val authHandler = AuthHandler(vertx, userService)

    fun create(): Router {
        val router = Router.router(vertx)
        router.route().handler(BodyHandler.create())
        router.route().failureHandler(::handleFailure)

        // Public routes
        router.post("/login").coroutineHandler(::login)

        // Protected routes
        router.get("/logout").handler(authHandler::handle).coroutineHandler(::logout)
        router.get("/user-info").handler(authHandler::handle).coroutineHandler(::getUserInfo)

        // Tags
        router.get("/tags").handler(authHandler::handle).coroutineHandler(::getTags)
        router.post("/tags").handler(authHandler::handle).coroutineHandler(::addTag)
        router.put("/tags").handler(authHandler::handle).coroutineHandler(::updateTags)
        router.delete("/tags/:slug").handler(authHandler::handle).coroutineHandler(::delTag)

        // Posts
        router.get("/posts").handler(authHandler::handle).coroutineHandler(::getPosts)
        router.post("/posts").handler(authHandler::handle).coroutineHandler(::addPost)
        router.put("/posts").handler(authHandler::handle).coroutineHandler(::updatePost)
        router.delete("/posts/:slug").handler(authHandler::handle).coroutineHandler(::delPost)
        router.get("/posts/:slug").handler(authHandler::handle).coroutineHandler(::getPost)

        // Options
        router.get("/options").handler(authHandler::handle).coroutineHandler(::getOptions)
        router.put("/options").handler(authHandler::handle).coroutineHandler(::updateOptions)

        // Users
        router.put("/users/update-password").handler(authHandler::handle).coroutineHandler(::updatePassword)
        router.put("/users/update").handler(authHandler::handle).coroutineHandler(::updateUser)

        // Blogrolls
        router.get("/blogrolls").handler(authHandler::handle).coroutineHandler(::getBlogrolls)
        router.post("/blogrolls").handler(authHandler::handle).coroutineHandler(::updateBlogrolls)

        return router
    }

    private suspend fun login(ctx: RoutingContext) {
        try {
            val loginInfo: LoginInfo = objectMapper.readValue(ctx.body().asString())
            val token = userService.login(loginInfo.username, loginInfo.password)

            if (token != null) {
                val encodedToken = URLEncoder.encode(token, "UTF-8")
                ctx.response()
                    .addCookie(Cookie.cookie(AuthHandler.SESSION_COOKIE, "token=$encodedToken").setPath("/"))
                    .putHeader("Content-Type", "application/json")
                    .end("""{"status":"ok"}""")
            } else {
                ctx.response()
                    .setStatusCode(401)
                    .putHeader("Content-Type", "application/json")
                    .end("""{"status":"err","message":"Invalid username and password"}""")
            }
        } catch (e: Exception) {
            sendError(ctx, e.message ?: "Login failed")
        }
    }

    private suspend fun logout(ctx: RoutingContext) {
        ctx.response()
            .addCookie(Cookie.cookie(AuthHandler.SESSION_COOKIE, "").setPath("/").setMaxAge(0))
            .setStatusCode(302)
            .putHeader("Location", "/admin/")
            .end()
    }

    private suspend fun getUserInfo(ctx: RoutingContext) {
        val user = ctx.getUser()
        ctx.response()
            .putHeader("Content-Type", "application/json")
            .end(objectMapper.writeValueAsString(mapOf(
                "username" to user.username,
                "email" to user.email,
                "nickname" to user.nickname
            )))
    }

    private suspend fun getTags(ctx: RoutingContext) {
        try {
            val tags = tagService.all()
            ctx.response()
                .putHeader("Content-Type", "application/json")
                .end(objectMapper.writeValueAsString(tags))
        } catch (e: Exception) {
            sendError(ctx, e.message ?: "Failed to get tags")
        }
    }

    private suspend fun addTag(ctx: RoutingContext) {
        try {
            val tag: PostTagEntity = objectMapper.readValue(ctx.body().asString())
            tagService.insert(tag)
            sendOk(ctx)
        } catch (e: Exception) {
            sendError(ctx, e.message ?: "Failed to add tag")
        }
    }

    private suspend fun updateTags(ctx: RoutingContext) {
        try {
            val tags: List<PostTagEntity> = objectMapper.readValue(ctx.body().asString())
            tagService.replaceAll(tags)
            sendOk(ctx)
        } catch (e: Exception) {
            sendError(ctx, e.message ?: "Failed to update tags")
        }
    }

    private suspend fun delTag(ctx: RoutingContext) {
        try {
            val slug = ctx.pathParam("slug")
            tagService.delete(slug)
            sendOk(ctx)
        } catch (e: Exception) {
            sendError(ctx, e.message ?: "Failed to delete tag")
        }
    }

    private suspend fun getPosts(ctx: RoutingContext) {
        try {
            val page = ctx.queryParam("page").firstOrNull()?.toIntOrNull() ?: 1
            val postType = ctx.queryParam("type").firstOrNull()?.let { PostType.valueOf(it) } ?: PostType.Post
            val status = ctx.queryParam("status").firstOrNull()?.let { PostStatus.valueOf(it) } ?: PostStatus.Published

            val (posts, count) = postService.listByPage(if (page < 1) 1 else page, postType, status)
            ctx.response()
                .putHeader("Content-Type", "application/json")
                .end(objectMapper.writeValueAsString(mapOf("count" to count, "data" to posts)))
        } catch (e: Exception) {
            sendError(ctx, e.message ?: "Failed to get posts")
        }
    }

    private suspend fun getPost(ctx: RoutingContext) {
        try {
            val slug = ctx.pathParam("slug")
            val post = postService.getBySlug(slug)
            if (post != null) {
                ctx.response()
                    .putHeader("Content-Type", "application/json")
                    .end(objectMapper.writeValueAsString(post))
            } else {
                ctx.response()
                    .setStatusCode(404)
                    .putHeader("Content-Type", "application/json")
                    .end("""{"status":"err"}""")
            }
        } catch (e: Exception) {
            sendError(ctx, e.message ?: "Failed to get post")
        }
    }

    private suspend fun addPost(ctx: RoutingContext) {
        try {
            val post: PostEntity = objectMapper.readValue(ctx.body().asString())
            postService.insert(post)
            sendOk(ctx)
        } catch (e: Exception) {
            sendError(ctx, e.message ?: "Failed to add post")
        }
    }

    private suspend fun updatePost(ctx: RoutingContext) {
        try {
            val post: PostEntity = objectMapper.readValue(ctx.body().asString())
            postService.update(post)
            sendOk(ctx)
        } catch (e: Exception) {
            sendError(ctx, e.message ?: "Failed to update post")
        }
    }

    private suspend fun delPost(ctx: RoutingContext) {
        try {
            val slug = ctx.pathParam("slug")
            postService.delete(slug)
            sendOk(ctx)
        } catch (e: Exception) {
            sendError(ctx, e.message ?: "Failed to delete post")
        }
    }

    private suspend fun getOptions(ctx: RoutingContext) {
        val optionInfo = optionsService.snapshot()
        ctx.response()
            .putHeader("Content-Type", "application/json")
            .end(objectMapper.writeValueAsString(optionInfo))
    }

    private suspend fun updateOptions(ctx: RoutingContext) {
        try {
            val options: OptionInfo = objectMapper.readValue(ctx.body().asString())
            optionsService.update(options)
            sendOk(ctx)
        } catch (e: Exception) {
            sendError(ctx, e.message ?: "Failed to update options")
        }
    }

    private suspend fun updatePassword(ctx: RoutingContext) {
        try {
            val user = ctx.getUser()
            val body: Map<String, String> = objectMapper.readValue(ctx.body().asString())
            val oldPassword = body["old"] ?: throw IllegalArgumentException("Old password required")
            val newPassword = body["new"] ?: throw IllegalArgumentException("New password required")

            val success = userService.updatePassword(user.username, newPassword, oldPassword)
            if (success) {
                sendOk(ctx)
            } else {
                sendError(ctx, "Password not match")
            }
        } catch (e: Exception) {
            sendError(ctx, e.message ?: "Failed to update password")
        }
    }

    private suspend fun updateUser(ctx: RoutingContext) {
        try {
            val currentUser = ctx.getUser()
            val body: Map<String, String> = objectMapper.readValue(ctx.body().asString())
            val email = body["email"]
            val nickname = body["nickname"]

            userService.update(UserEntity(currentUser.username, "", email, nickname))
            sendOk(ctx)
        } catch (e: Exception) {
            sendError(ctx, e.message ?: "Failed to update user")
        }
    }

    private suspend fun getBlogrolls(ctx: RoutingContext) {
        ctx.response()
            .putHeader("Content-Type", "application/json")
            .end(objectMapper.writeValueAsString(blogrollService.current()))
    }

    private suspend fun updateBlogrolls(ctx: RoutingContext) {
        try {
            val blogrolls: List<BlogrollEntity> = objectMapper.readValue(ctx.body().asString())
            blogrollService.reset(blogrolls)
            sendOk(ctx)
        } catch (e: Exception) {
            sendError(ctx, e.message ?: "Failed to update blogrolls")
        }
    }

    private fun sendOk(ctx: RoutingContext) {
        ctx.response()
            .putHeader("Content-Type", "application/json")
            .end("""{"status":"ok"}""")
    }

    private fun handleFailure(ctx: RoutingContext) {
        val message = ctx.failure()?.message ?: "Internal server error"
        val status = if (ctx.statusCode() >= 400) ctx.statusCode() else 500
        ctx.response()
            .setStatusCode(status)
            .putHeader("Content-Type", "application/json")
            .end("""{"status":"err","message":"$message"}""")
    }

    private fun sendError(ctx: RoutingContext, message: String) {
        ctx.response()
            .setStatusCode(400)
            .putHeader("Content-Type", "application/json")
            .end("""{"status":"err","message":"$message"}""")
    }
}
