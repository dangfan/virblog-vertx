package fan.dang.wcfont

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.FileSystemAccess
import io.vertx.ext.web.handler.StaticHandler
import fan.dang.web.coroutineHandler

object Routes {
    fun create(vertx: Vertx, fontService: FontService, config: AppConfig): Router {
        val router = Router.router(vertx)

        // GET / - returns JavaScript loader
        router.get("/").handler { ctx ->
            val selector = ctx.queryParam("s").firstOrNull() ?: ""
            val family = ctx.queryParam("family").firstOrNull() ?: ""

            val js = Templates.loaderJs(selector, family)
            ctx.response()
                .putHeader("Content-Type", "application/javascript")
                .end(js)
        }

        // GET /css - returns CSS with font-face
        router.get("/css").coroutineHandler { ctx ->
            try {
                val family = ctx.queryParam("family").firstOrNull() ?: ""
                val content = ctx.queryParam("content").firstOrNull() ?: ""

                val filename = fontService.subsetFont(family, content)
                val css = Templates.fontFaceCss(family, filename)

                ctx.response()
                    .putHeader("Content-Type", "text/css")
                    .end(css)
            } catch (e: Exception) {
                ctx.response()
                    .setStatusCode(500)
                    .end("Error: ${e.message}")
            }
        }

        // Serve generated font files
        router.get("/fonts/*").handler(
            StaticHandler.create(FileSystemAccess.ROOT, config.fontOutputPath)
                .setCachingEnabled(true)
                .setMaxAgeSeconds(86400)
        )

        return router
    }
}
