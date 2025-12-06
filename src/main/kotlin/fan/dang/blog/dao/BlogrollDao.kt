package fan.dang.blog.dao

import fan.dang.blog.config.BlogOptions
import fan.dang.blog.db.Database
import fan.dang.blog.models.BlogrollEntity
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.Tuple

class BlogrollDao(private val db: Database) {

    private fun Row.toBlogrollEntity(): BlogrollEntity {
        return BlogrollEntity(
            name = getString("NAME"),
            link = getString("LINK")
        )
    }

    suspend fun load() {
        val rows = db.query("""SELECT * FROM BLOGROLLS""")
        BlogOptions.blogrolls = rows.map { it.toBlogrollEntity() }
    }

    suspend fun reset(blogrolls: List<BlogrollEntity>) {
        BlogOptions.blogrolls = blogrolls
        db.query("""DELETE FROM BLOGROLLS""")
        blogrolls.forEach { blogroll ->
            db.execute(
                """INSERT INTO BLOGROLLS (NAME, LINK) VALUES (?, ?)""",
                Tuple.of(blogroll.name, blogroll.link)
            )
        }
    }
}
