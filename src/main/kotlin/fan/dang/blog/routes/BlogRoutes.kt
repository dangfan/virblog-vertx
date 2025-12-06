package fan.dang.blog.routes

import fan.dang.blog.config.BlogConfig
import fan.dang.blog.config.BlogOptions
import fan.dang.blog.models.PostStatus
import fan.dang.blog.models.PostType
import fan.dang.blog.service.PostService
import fan.dang.blog.service.TagService
import fan.dang.blog.templates.BlogTemplates
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import fan.dang.web.coroutineHandler

class BlogRoutes(
    private val vertx: Vertx,
    private val postService: PostService,
    private val tagService: TagService,
    private val config: BlogConfig
) {
    fun create(): Router {
        val router = Router.router(vertx)

        // Language redirect
        router.get("/").handler(::chooseLanguage)

        // Blog pages with language prefix
        router.get("/:lang/").coroutineHandler(::index)
        router.get("/:lang/posts/:slug").coroutineHandler(::post)
        router.get("/:lang/pages/:slug").coroutineHandler(::page)
        router.get("/:lang/tags/:slug").coroutineHandler(::tag)

        return router
    }

    private fun chooseLanguage(ctx: RoutingContext) {
        val acceptLanguage = ctx.request().getHeader("Accept-Language") ?: ""
        val preferredLang = parseAcceptLanguage(acceptLanguage)
        ctx.response()
            .setStatusCode(302)
            .putHeader("Location", "/$preferredLang/")
            .end()
    }

    private fun parseAcceptLanguage(header: String): String {
        if (header.isEmpty()) return config.availableLocales.firstOrNull() ?: BlogOptions.defaultLocale

        val languages = header.split(",").map { part ->
            val segments = part.trim().split(";")
            val lang = segments[0].trim()
            val quality = if (segments.size > 1) {
                segments[1].replace("q=", "").toDoubleOrNull() ?: 1.0
            } else 1.0
            Pair(lang, quality)
        }.sortedByDescending { it.second }

        for ((lang, _) in languages) {
            // Check for Chinese variants
            if (lang.startsWith("zh")) {
                val country = lang.substringAfter("-", "").uppercase()
                return if (country == "CN" || country == "SG") "zh-Hans" else "zh-Hant"
            }
            // Check if language is available
            val matchedLocale = config.availableLocales.find { it.startsWith(lang.split("-")[0]) }
            if (matchedLocale != null) return matchedLocale
        }

        return config.availableLocales.firstOrNull() ?: BlogOptions.defaultLocale
    }

    private fun extractLang(ctx: RoutingContext): String {
        val path = ctx.request().path()
        val match = Regex("/([a-zA-Z\\-]{2,7})/.*").find(path)
        return match?.groupValues?.get(1) ?: BlogOptions.defaultLocale
    }

    private suspend fun index(ctx: RoutingContext) {
        try {
            val lang = extractLang(ctx)
            val page = ctx.queryParam("page").firstOrNull()?.toIntOrNull() ?: 1

            if (page <= 0) {
                ctx.response()
                    .setStatusCode(302)
                    .putHeader("Location", "/$lang/")
                    .end()
                return
            }

            val (posts, postCount) = postService.listByPage(page, PostType.Post, PostStatus.Published)
            val html = BlogTemplates.indexPage(posts, page, postCount, lang)

            ctx.response()
                .putHeader("Content-Type", "text/html; charset=utf-8")
                .end(html)
        } catch (e: Exception) {
            sendError(ctx, e)
        }
    }

    private suspend fun post(ctx: RoutingContext) {
        try {
            val lang = ctx.pathParam("lang") ?: extractLang(ctx)
            val slug = ctx.pathParam("slug") ?: ""

            val post = postService.getBySlug(slug)
            if (post == null || post.postType != PostType.Post) {
                ctx.response()
                    .setStatusCode(404)
                    .putHeader("Content-Type", "text/html; charset=utf-8")
                    .end(BlogTemplates.notFoundPage(lang))
                return
            }

            val tags = tagService.getBySlugs(post.tags)
            val html = BlogTemplates.postPage(post, tags, lang)

            ctx.response()
                .putHeader("Content-Type", "text/html; charset=utf-8")
                .end(html)
        } catch (e: Exception) {
            val lang = extractLang(ctx)
            ctx.response()
                .setStatusCode(404)
                .putHeader("Content-Type", "text/html; charset=utf-8")
                .end(BlogTemplates.notFoundPage(lang))
        }
    }

    private suspend fun page(ctx: RoutingContext) {
        try {
            val lang = ctx.pathParam("lang") ?: extractLang(ctx)
            val slug = ctx.pathParam("slug") ?: ""

            val post = postService.getBySlug(slug)
            if (post == null || post.postType != PostType.Page) {
                ctx.response()
                    .setStatusCode(404)
                    .putHeader("Content-Type", "text/html; charset=utf-8")
                    .end(BlogTemplates.notFoundPage(lang))
                return
            }

            val html = BlogTemplates.pagePage(post, lang)

            ctx.response()
                .putHeader("Content-Type", "text/html; charset=utf-8")
                .end(html)
        } catch (e: Exception) {
            val lang = extractLang(ctx)
            ctx.response()
                .setStatusCode(404)
                .putHeader("Content-Type", "text/html; charset=utf-8")
                .end(BlogTemplates.notFoundPage(lang))
        }
    }

    private suspend fun tag(ctx: RoutingContext) {
        try {
            val lang = ctx.pathParam("lang") ?: extractLang(ctx)
            val slug = ctx.pathParam("slug") ?: ""
            val page = ctx.queryParam("page").firstOrNull()?.toIntOrNull() ?: 1

            if (page <= 0) {
                ctx.response()
                    .setStatusCode(302)
                    .putHeader("Location", "/$lang/tags/$slug")
                    .end()
                return
            }

            val tag = tagService.getBySlug(slug)
            if (tag == null) {
                ctx.response()
                    .setStatusCode(404)
                    .putHeader("Content-Type", "text/html; charset=utf-8")
                    .end(BlogTemplates.notFoundPage(lang))
                return
            }

            val (posts, postCount) = postService.listByTag(slug, page)
            val html = BlogTemplates.tagPage(tag, posts, page, postCount, lang)

            ctx.response()
                .putHeader("Content-Type", "text/html; charset=utf-8")
                .end(html)
        } catch (e: Exception) {
            val lang = extractLang(ctx)
            ctx.response()
                .setStatusCode(404)
                .putHeader("Content-Type", "text/html; charset=utf-8")
                .end(BlogTemplates.notFoundPage(lang))
        }
    }

    private fun sendError(ctx: RoutingContext, e: Exception) {
        ctx.response()
            .setStatusCode(500)
            .putHeader("Content-Type", "text/plain")
            .end("Error: ${e.message}")
    }
}
