package fan.dang.blog.util

import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.MutableDataSet
import com.vladsch.flexmark.ast.Image
import com.vladsch.flexmark.util.ast.NodeVisitor
import com.vladsch.flexmark.util.ast.VisitHandler

object MarkdownParser {
    private val options = MutableDataSet().apply {
        set(Parser.EXTENSIONS, listOf(StrikethroughExtension.create()))
    }
    private val parser = Parser.builder(options).build()
    private val renderer = HtmlRenderer.builder(options).build()

    fun parse(markdown: String): String {
        val document = parser.parse(markdown)
        return renderer.render(document)
    }

    /**
     * Extract the first image URL from markdown content
     */
    fun extractFirstImage(markdown: String): String? {
        val document = parser.parse(markdown)
        var firstImageUrl: String? = null

        val visitor = NodeVisitor(
            VisitHandler(Image::class.java) { image ->
                if (firstImageUrl == null) {
                    firstImageUrl = image.url.toString()
                }
            }
        )

        visitor.visit(document)
        return firstImageUrl
    }
}
