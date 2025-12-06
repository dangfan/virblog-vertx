package fan.dang.blog.dao

import fan.dang.blog.config.BlogOptions
import fan.dang.blog.db.Database
import fan.dang.blog.db.Database.Companion.getDateTime
import fan.dang.blog.db.Database.Companion.getStringList
import fan.dang.blog.db.Database.Companion.getStringMap
import fan.dang.blog.db.Database.Companion.toJson
import fan.dang.blog.db.Database.Companion.toJsonArray
import fan.dang.blog.models.PostEntity
import fan.dang.blog.models.PostStatus
import fan.dang.blog.models.PostType
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.Tuple

class PostDao(private val db: Database) {

    private fun Row.toPostEntity(): PostEntity {
        return PostEntity(
            id = getInteger("ID"),
            slug = getString("SLUG"),
            time = getDateTime("TIME") ?: error("Invalid TIME value"),
            title = getStringMap("TITLE"),
            subtitle = getStringMap("SUBTITLE"),
            excerpt = getStringMap("EXCERPT"),
            content = getStringMap("CONTENT"),
            headerImage = getString("HEADER_IMAGE") ?: "",
            status = PostStatus.valueOf(getString("POST_STATUS")),
            postType = PostType.valueOf(getString("POST_TYPE")),
            tags = getStringList("TAGS")
        )
    }

    suspend fun count(): Int {
        val rows = db.query("""SELECT COUNT(*) as cnt FROM POSTS""")
        return rows.firstOrNull()?.getInteger("cnt") ?: 0
    }

    suspend fun insert(post: PostEntity): Int {
        val sql = """
            INSERT INTO POSTS (SLUG, TIME, TITLE, SUBTITLE, EXCERPT, CONTENT, HEADER_IMAGE, POST_STATUS, POST_TYPE, TAGS)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()
        return db.execute(sql, Tuple.of(
            post.slug,
            post.time.toString(),
            post.title.toJson(),
            post.subtitle.toJson(),
            post.excerpt.toJson(),
            post.content.toJson(),
            post.headerImage,
            post.status.name,
            post.postType.name,
            post.tags.toJsonArray()
        ))
    }

    suspend fun delete(slug: String): Int {
        return db.execute("""DELETE FROM POSTS WHERE SLUG = ?""", Tuple.of(slug))
    }

    suspend fun update(post: PostEntity): Int {
        val sql = """
            UPDATE POSTS SET 
                SLUG = ?, TIME = ?, TITLE = ?, SUBTITLE = ?, 
                EXCERPT = ?, CONTENT = ?, HEADER_IMAGE = ?, 
                POST_STATUS = ?, POST_TYPE = ?, TAGS = ?
            WHERE ID = ?
        """.trimIndent()
        return db.execute(sql, Tuple.of(
            post.slug,
            post.time.toString(),
            post.title.toJson(),
            post.subtitle.toJson(),
            post.excerpt.toJson(),
            post.content.toJson(),
            post.headerImage,
            post.status.name,
            post.postType.name,
            post.tags.toJsonArray(),
            post.id
        ))
    }

    suspend fun getBySlug(slug: String): PostEntity? {
        val rows = db.preparedQuery("""SELECT * FROM POSTS WHERE SLUG = ?""", Tuple.of(slug))
        return rows.firstOrNull()?.toPostEntity()
    }

    suspend fun listByPage(
        page: Int,
        postType: PostType = PostType.Post,
        status: PostStatus = PostStatus.Published
    ): Pair<List<PostEntity>, Int> {
        val pageSize = BlogOptions.pageSize
        val offset = pageSize * (page - 1)

        val countSql = """
            SELECT COUNT(*) as cnt FROM POSTS 
            WHERE POST_TYPE = ? AND POST_STATUS = ?
        """.trimIndent()
        val countRows = db.preparedQuery(countSql, Tuple.of(postType.name, status.name))
        val total = countRows.firstOrNull()?.getInteger("cnt") ?: 0

        val sql = """
            SELECT * FROM POSTS 
            WHERE POST_TYPE = ? AND POST_STATUS = ?
            ORDER BY TIME DESC
            LIMIT ? OFFSET ?
        """.trimIndent()
        val rows = db.preparedQuery(sql, Tuple.of(postType.name, status.name, pageSize, offset))
        val posts = rows.map { it.toPostEntity() }

        return Pair(posts, total)
    }

    suspend fun listByTag(slug: String, page: Int): Pair<List<PostEntity>, Int> {
        val pageSize = BlogOptions.pageSize
        val offset = pageSize * (page - 1)

        val countSql = """
            SELECT COUNT(*) as cnt FROM POSTS 
            WHERE POST_TYPE = 'Post' AND POST_STATUS = 'Published'
            AND EXISTS (SELECT 1 FROM json_each(TAGS) WHERE value = ?)
        """.trimIndent()
        val countRows = db.preparedQuery(countSql, Tuple.of(slug))
        val total = countRows.firstOrNull()?.getInteger("cnt") ?: 0

        val sql = """
            SELECT * FROM POSTS 
            WHERE POST_TYPE = 'Post' AND POST_STATUS = 'Published'
            AND EXISTS (SELECT 1 FROM json_each(TAGS) WHERE value = ?)
            ORDER BY TIME DESC
            LIMIT ? OFFSET ?
        """.trimIndent()
        val rows = db.preparedQuery(sql, Tuple.of(slug, pageSize, offset))
        val posts = rows.map { it.toPostEntity() }

        return Pair(posts, total)
    }
}
