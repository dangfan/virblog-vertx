package fan.dang.blog.routes

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import fan.dang.blog.util.OpenCC
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler

class I18nRoutes(private val vertx: Vertx) {
    private val objectMapper = ObjectMapper().registerKotlinModule()

    fun create(): Router {
        val router = Router.router(vertx)
        router.route().handler(BodyHandler.create())

        router.post("/zhs2zht").handler(::zhs2Zht)

        return router
    }

    private fun zhs2Zht(ctx: RoutingContext) {
        try {
            val body: Map<String, String> = objectMapper.readValue(ctx.body().asString())
            val content = body["content"] ?: ""
            val converted = OpenCC.convertWithQuotes(content)

            ctx.response()
                .putHeader("Content-Type", "text/plain; charset=utf-8")
                .end(converted)
        } catch (e: Exception) {
            ctx.response()
                .setStatusCode(400)
                .putHeader("Content-Type", "text/plain")
                .end("Error: ${e.message}")
        }
    }
}
