package fan.dang.blog.templates

import fan.dang.blog.config.BlogOptions
import fan.dang.blog.models.BlogrollEntity
import fan.dang.blog.models.PostEntity
import fan.dang.blog.models.PostTagEntity
import fan.dang.blog.util.MarkdownParser
import fan.dang.blog.util.Messages
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object BlogTemplates {

    private fun Map<String, String>.localize(lang: String): String {
        return this[lang] ?: this[BlogOptions.defaultLocale] ?: ""
    }

    private fun LocalDateTime.localize(lang: String): String {
        val pattern = BlogOptions.datetimeFormat.localize(lang).ifEmpty { "yyyy-MM-dd" }
        return this.format(DateTimeFormatter.ofPattern(pattern).withLocale(Locale(lang)))
    }

    private fun getMainLang(langCode: String): String {
        return langCode.split("-")[0]
    }

    private fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
    }

    fun mainLayout(
        title: String,
        headerImg: String,
        lang: String,
        heading: String,
        content: String,
        currentPath: String = "",
        queryString: String = "",
        ogUrl: String = "",
        ogImage: String = "",
        ogDescription: String = ""
    ): String {
        val bgImage = headerImg.ifEmpty { "https://img.dang.fan/home-bg.jpg" }
        val blogrolls = BlogOptions.blogrolls

        val ogMetaTags = if (ogUrl.isNotEmpty()) {
            val ogImageTag = if (ogImage.isNotEmpty()) {
                """<meta property="og:image" content="${escapeHtml(ogImage)}">"""  
            } else ""
            val ogDescriptionTag = if (ogDescription.isNotEmpty()) {
                """<meta property="og:description" content="${escapeHtml(ogDescription)}">"""  
            } else ""
            """
        <meta property="og:title" content="${escapeHtml(title)}">
        <meta property="og:type" content="article">
        <meta property="og:url" content="${escapeHtml(ogUrl)}">
        $ogImageTag
        $ogDescriptionTag"""
        } else ""

        return """
<!DOCTYPE html>
<html prefix="og: https://ogp.me/ns#" lang="$lang" class="han-init">
    <head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
        <title>${escapeHtml(title)}</title>
        $ogMetaTags
        <link href="/assets/stylesheets/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.7.2/css/all.min.css" rel="stylesheet">
        <link href="https://fonts.loli.net/css?family=Lora:400,700,400italic,700italic" rel="stylesheet">
        <link href="https://fonts.loli.net/css?family=Open+Sans:300italic,400italic,600italic,700italic,800italic,400,300,600,700,800" rel="stylesheet">
        <link href="/assets/stylesheets/main.css" rel="stylesheet">
        <link href="/assets/stylesheets/i18n/${getMainLang(lang)}.css" rel="stylesheet">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.11.1/styles/default.min.css" rel="stylesheet">
    </head>
    <body>
        <nav class="navbar navbar-expand-lg navbar-light fixed-top" id="mainNav">
            <div class="container">
                <button class="navbar-toggler navbar-toggler-right" type="button" data-bs-toggle="collapse" data-bs-target="#navbarResponsive" aria-controls="navbarResponsive" aria-expanded="false" aria-label="Toggle navigation">
                    <span class="fas fa-bars"></span>
                </button>
                <div class="collapse navbar-collapse" id="navbarResponsive">
                    <ul class="navbar-nav">
                        <li class="nav-item">
                            <a href="/$lang/">${Messages.get("nav.home", lang)}</a>
                        </li>
                        <li class="nav-item">
                            <a href="https://dang.fan">${Messages.get("nav.about", lang)}</a>
                        </li>
                    </ul>
                    <hr>
                    <ul class="navbar-nav ml-auto">
                        ${BlogOptions.locales.entries.joinToString("\n") { (id, name) ->
                            val url = if (currentPath.isEmpty()) "/$id/" else "/$id$currentPath"
                            val fullUrl = if (queryString.isNotEmpty()) "$url?$queryString" else url
                            """<li class="nav-item"><a class="nav-link" href="$fullUrl">$name</a></li>"""
                        }}
                    </ul>
                </div>
            </div>
        </nav>
        <header class="masthead" style="background-image: url($bgImage)">
            <div class="overlay"></div>
            <div class="container">
                <div class="row">
                    <div class="col-lg-8 col-md-10 mx-auto">
                    $heading
                    </div>
                </div>
            </div>
        </header>
        <div class="container">
            <div class="row">
                <div class="col-lg-8 col-md-10 mx-auto">
                $content
                </div>
            </div>
        </div>
        <hr>
        <footer>
            <div class="container">
                <div class="row">
                    <div class="col-lg-8 col-md-10 mx-auto">
                        <h4>${Messages.get("main.blogrolls", lang)}</h4>
                        <ul class="blogroll row">
                            ${blogrolls.joinToString("\n") { blogroll ->
                                """<li class="col-sm-4"><a href="${blogroll.link}" target="_blank">${escapeHtml(blogroll.name)}</a></li>"""
                            }}
                        </ul>
                    </div>
                </div>
                <div class="row socials">
                    <div class="col-lg-8 col-md-10 mx-auto">
                        <ul class="list-inline text-center">
                            <li class="list-inline-item">
                                <a href="https://www.linkedin.com/in/dangfan/" target="_blank">
                                    <span class="fa-stack fa-lg">
                                        <span class="fas fa-circle fa-stack-2x"></span>
                                        <span class="fab fa-linkedin fa-stack-1x fa-inverse"></span>
                                    </span>
                                </a>
                            </li>
                            <li class="list-inline-item">
                                <a href="https://github.com/dangfan" target="_blank">
                                    <span class="fa-stack fa-lg">
                                        <span class="fas fa-circle fa-stack-2x"></span>
                                        <span class="fab fa-github fa-stack-1x fa-inverse"></span>
                                    </span>
                                </a>
                            </li>
                        </ul>
                        <p class="copyright text-muted">${Messages.get("main.copyright", lang)}</p>
                    </div>
                </div>
            </div>
        </footer>
        <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.3.1/jquery.slim.min.js"></script>
        <script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.11.1/highlight.min.js"></script>
        <script>
            ${'$'}('p:has(img)').css('text-align', 'center');
            hljs.highlightAll();
        </script>
        ${if (lang.startsWith("zh")) """
            <script>
                ${'$'}('p:has(img)').css('text-indent', '0');
            </script>
            <script src="/wcfont/?family=beiwei&s=h1" async></script>
            <script src="/wcfont/?family=beiwei&s=.subheading" async></script>
            <script src="https://cdnjs.cloudflare.com/ajax/libs/Han/3.3.0/han.min.js"></script>
        """ else ""}
        ${if (BlogOptions.gaId.isNotEmpty()) """
            <script async src="https://www.googletagmanager.com/gtag/js?id=${BlogOptions.gaId}"></script>
            <script>
                window.dataLayer=window.dataLayer||[];
                function gtag(){dataLayer.push(arguments);}
                gtag('js',new Date());
                gtag('config','${BlogOptions.gaId}');
            </script>
        """ else ""}
        <script async src="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/5.3.8/js/bootstrap.min.js"></script>
        <script defer src="https://cdn.jsdelivr.net/npm/mathjax@4/tex-chtml.js"></script>
    </body>
</html>
        """.trimIndent()
    }

    fun indexPage(posts: List<PostEntity>, pageNumber: Int, postCount: Int, lang: String, queryString: String = ""): String {
        val blogName = BlogOptions.blogName.localize(lang)
        val blogDescription = BlogOptions.blogDescription.localize(lang)

        val heading = """
            <div class="site-heading">
                <h1>${escapeHtml(blogName)}</h1>
                <span class="subheading">${escapeHtml(blogDescription)}</span>
            </div>
        """.trimIndent()

        val postsHtml = posts.mapIndexed { index, post ->
            val separator = if (index != 0) "<hr>" else ""
            """
            $separator
            <div class="post-preview">
                <a href="posts/${post.slug}">
                    <h2 class="post-title">${escapeHtml(post.title.localize(lang))}</h2>
                    <h3 class="post-subtitle">${escapeHtml(post.subtitle.localize(lang))}</h3>
                </a>
                <p class="post-meta">${Messages.get("time", lang, post.time.localize(lang))}</p>
                <article>
                ${MarkdownParser.parse(post.excerpt.localize(lang))}
                </article>
            </div>
            """.trimIndent()
        }.joinToString("\n")

        val prevLink = if (pageNumber > 1) {
            """<a class="page-link" href="?page=${pageNumber - 1}">${Messages.get("pagination.prev", lang)} &larr;</a>"""
        } else ""

        val nextLink = if (postCount > pageNumber * BlogOptions.pageSize) {
            """<a class="page-link" href="?page=${pageNumber + 1}">${Messages.get("pagination.next", lang)} &rarr;</a>"""
        } else ""

        val content = """
            $postsHtml
            <ul class="pagination justify-content-between">
                <li>$prevLink</li>
                <li>$nextLink</li>
            </ul>
        """.trimIndent()

        return mainLayout(blogName, "", lang, heading, content, "", queryString)
    }

    fun postPage(post: PostEntity, tags: List<PostTagEntity>, lang: String, baseUrl: String = ""): String {
        val title = "${post.title.localize(lang)} - ${BlogOptions.blogName.localize(lang)}"

        val tagsHtml = tags.mapIndexed { index, tag ->
            val separator = if (index != 0) "/ " else ""
            """$separator<a href="/$lang/tags/${tag.slug}">${escapeHtml(tag.name.localize(lang))}</a>"""
        }.joinToString("")

        val heading = """
            <div class="post-heading">
                <h1>${escapeHtml(post.title.localize(lang))}</h1>
                <h2 class="subheading">${escapeHtml(post.subtitle.localize(lang))}</h2>
                <span class="meta">${Messages.get("time", lang, post.time.localize(lang))} -
                    <span class="post-tags">$tagsHtml</span>
                </span>
            </div>
        """.trimIndent()

        val disqusHtml = if (BlogOptions.disqusShortName.isNotEmpty()) """
            <div id="disqus_thread"></div>
            <script type="text/javascript">
                var disqus_config=function(){this.page.identifier = '${post.slug}';};
                (function() {
                    var d=document,s=d.createElement('script');
                    s.src='https://${BlogOptions.disqusShortName}.disqus.com/embed.js';
                    s.setAttribute('data-timestamp',+new Date());
                    (d.head||d.body).appendChild(s);
                })();
            </script>
        """ else ""

        val content = """
            <article>
            ${MarkdownParser.parse(post.content.localize(lang))}
            </article>
            $disqusHtml
        """.trimIndent()

        // Extract OG metadata
        val ogUrl = if (baseUrl.isNotEmpty()) "$baseUrl/$lang/posts/${post.slug}" else ""
        val ogImage = MarkdownParser.extractFirstImage(post.content.localize(lang)) 
            ?: post.headerImage.ifEmpty { "" }
        val ogDescription = MarkdownParser.parse(post.excerpt.localize(lang))

        return mainLayout(title, post.headerImage, lang, heading, content, "/posts/${post.slug}", "", ogUrl, ogImage, ogDescription)
    }

    fun pagePage(post: PostEntity, lang: String, baseUrl: String = ""): String {
        val title = "${post.title.localize(lang)} - ${BlogOptions.blogName.localize(lang)}"

        val heading = """
            <div class="post-heading">
                <h1>${escapeHtml(post.title.localize(lang))}</h1>
                <h2 class="subheading">${escapeHtml(post.subtitle.localize(lang))}</h2>
            </div>
        """.trimIndent()

        val content = """
            <article class="$lang">
            ${MarkdownParser.parse(post.content.localize(lang))}
            </article>
        """.trimIndent()

        // Extract OG metadata
        val ogUrl = if (baseUrl.isNotEmpty()) "$baseUrl/$lang/pages/${post.slug}" else ""
        val ogImage = MarkdownParser.extractFirstImage(post.content.localize(lang)) 
            ?: post.headerImage.ifEmpty { "" }
        val ogDescription = MarkdownParser.parse(post.excerpt.localize(lang))

        return mainLayout(title, post.headerImage, lang, heading, content, "/pages/${post.slug}", "", ogUrl, ogImage, ogDescription)
    }

    fun tagPage(tag: PostTagEntity, posts: List<PostEntity>, pageNumber: Int, postCount: Int, lang: String, queryString: String = ""): String {
        val title = "${tag.name.localize(lang)} - ${BlogOptions.blogName.localize(lang)}"
        val blogName = BlogOptions.blogName.localize(lang)

        val heading = """
            <div class="site-heading">
                <h1>${escapeHtml(blogName)}</h1>
                <hr class="small">
                <span class="subheading">${Messages.get("tag", lang)} ${escapeHtml(tag.name.localize(lang))}</span>
            </div>
        """.trimIndent()

        val postsHtml = posts.mapIndexed { index, post ->
            val separator = if (index != 0) "<hr>" else ""
            """
            $separator
            <div class="post-preview">
                <a href="/$lang/posts/${post.slug}">
                    <h2 class="post-title">${escapeHtml(post.title.localize(lang))}</h2>
                    <h3 class="post-subtitle">${escapeHtml(post.subtitle.localize(lang))}</h3>
                </a>
                <p class="post-meta">${Messages.get("time", lang, post.time.localize(lang))}</p>
                <article>
                ${MarkdownParser.parse(post.excerpt.localize(lang))}
                </article>
            </div>
            """.trimIndent()
        }.joinToString("\n")

        val prevLink = if (pageNumber > 1) {
            """<a class="page-link" href="?page=${pageNumber - 1}">${Messages.get("pagination.prev", lang)} &larr;</a>"""
        } else ""

        val nextLink = if (postCount > pageNumber * BlogOptions.pageSize) {
            """<a class="page-link" href="?page=${pageNumber + 1}">${Messages.get("pagination.next", lang)} &rarr;</a>"""
        } else ""

        val content = """
            $postsHtml
            <ul class="pagination justify-content-between">
                <li>$prevLink</li>
                <li>$nextLink</li>
            </ul>
        """.trimIndent()

        return mainLayout(title, "", lang, heading, content, "", queryString)
    }

    fun notFoundPage(lang: String): String {
        return """
<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>${Messages.get("404.title", lang)}</title>
        <link href="/assets/stylesheets/404.css" rel="stylesheet" type="text/css">
    </head>
    <body>
        <div class="wrap">
            <div class="header">
                <div class="logo">
                    <h1><a href="/$lang/">${Messages.get("404.oops", lang)}</a></h1>
                </div>
            </div>
            <div class="content">
                <img src="/assets/images/error-img.png" title="error" />
                <p>${Messages.get("404.content", lang)}</p>
                <a href="/$lang/">${Messages.get("404.home", lang)}</a>
            </div>
        </div>
    </body>
</html>
        """.trimIndent()
    }
}
