package fan.dang.app

import fan.dang.blog.config.BlogConfig
import fan.dang.blog.dao.*
import fan.dang.blog.db.Database
import fan.dang.blog.routes.AdminRoutes
import fan.dang.blog.routes.BlogRoutes
import fan.dang.blog.routes.I18nRoutes
import fan.dang.blog.service.BlogrollService
import fan.dang.blog.service.OptionsService
import fan.dang.blog.service.PostService
import fan.dang.blog.service.TagService
import fan.dang.blog.service.UserService
import fan.dang.wcfont.AppConfig
import fan.dang.wcfont.FontService
import fan.dang.wcfont.Routes
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.coAwait
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
    runBlocking {
        val vertx = Vertx.vertx()
        vertx.deployVerticle(MainVerticle()).coAwait()
    }
}

class MainVerticle : CoroutineVerticle() {
    override suspend fun start() {
        // Load configurations
        val fontConfig = AppConfig.load()
        val blogConfig = BlogConfig.load()

        // Initialize font service
        val fontService = FontService(fontConfig)

        // Initialize database and DAOs
        val database = Database(vertx, blogConfig)
        val userDao = UserDao(database, blogConfig)
        val postDao = PostDao(database)
        val postTagDao = PostTagDao(database)
        val optionsDao = OptionsDao(database)
        val blogrollDao = BlogrollDao(database)
        val postService = PostService(postDao)
        val tagService = TagService(postTagDao)
        val userService = UserService(userDao)
        val optionsService = OptionsService(optionsDao)
        val blogrollService = BlogrollService(blogrollDao)

        // Load options and blogrolls from database
        optionsService.loadOptions()
        blogrollService.load()

        // Create main router
        val router = Router.router(vertx)

        // Mount font routes (existing wcfont functionality)
        val fontRouter = Routes.create(vertx, fontService, fontConfig)
        router.route("/wcfont/*").subRouter(fontRouter)

        // Mount blog API routes
        val adminRoutes = AdminRoutes(vertx, userService, postService, tagService, optionsService, blogrollService)
        router.route("/api/v1/*").subRouter(adminRoutes.create())

        // Mount I18n routes
        val i18nRoutes = I18nRoutes(vertx)
        router.route("/api/v1/i18n/*").subRouter(i18nRoutes.create())

        // Serve static assets packaged under resources/webroot/assets
        router.route("/assets/*").handler(
            StaticHandler.create("webroot/assets")
                .setCachingEnabled(true)
                .setMaxAgeSeconds(86400)
        )

        // Serve admin frontend from packaged webroot
        router.route("/admin/*").handler(
            StaticHandler.create("webroot/admin")
                .setCachingEnabled(false)
        )
        router.get("/admin").handler { ctx ->
            ctx.response()
                .setStatusCode(302)
                .putHeader("Location", "/admin/")
                .end()
        }

        // Mount blog frontend routes (must be last due to catch-all patterns)
        val blogRoutes = BlogRoutes(vertx, postService, tagService, blogConfig)
        router.route("/*").subRouter(blogRoutes.create())

        // Start HTTP server
        vertx.createHttpServer()
            .requestHandler(router)
            .listen(blogConfig.port)
            .coAwait()

        println("Blog and WCFont server started on http://localhost:${blogConfig.port}")
    }
}
