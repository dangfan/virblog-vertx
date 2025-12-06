package fan.dang.blog.util

import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.MutableDataSet

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
}
