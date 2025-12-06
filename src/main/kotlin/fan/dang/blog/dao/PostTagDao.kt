package fan.dang.blog.dao

import fan.dang.blog.db.Database
import fan.dang.blog.db.Database.Companion.getStringMap
import fan.dang.blog.db.Database.Companion.toJson
import fan.dang.blog.models.PostTagEntity
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.Tuple

class PostTagDao(private val db: Database) {

    private fun Row.toPostTagEntity(): PostTagEntity {
        return PostTagEntity(
            slug = getString("SLUG"),
            name = getStringMap("NAME")
        )
    }

    suspend fun count(): Int {
        val rows = db.query("""SELECT COUNT(*) as cnt FROM POST_TAGS""")
        return rows.firstOrNull()?.getInteger("cnt") ?: 0
    }

    suspend fun insert(tag: PostTagEntity): Int {
        return db.execute(
            """INSERT INTO POST_TAGS (SLUG, NAME) VALUES (?, ?)""",
            Tuple.of(tag.slug, tag.name.toJson())
        )
    }

    suspend fun delete(slug: String): Int {
        return db.execute("""DELETE FROM POST_TAGS WHERE SLUG = ?""", Tuple.of(slug))
    }

    suspend fun clear(): Int {
        val rows = db.query("""DELETE FROM POST_TAGS""")
        return rows.size
    }

    suspend fun update(tag: PostTagEntity): Int {
        return db.execute(
            """UPDATE POST_TAGS SET NAME = ? WHERE SLUG = ?""",
            Tuple.of(tag.name.toJson(), tag.slug)
        )
    }

    suspend fun all(): List<PostTagEntity> {
        val rows = db.query("""SELECT * FROM POST_TAGS""")
        return rows.map { it.toPostTagEntity() }
    }

    suspend fun getBySlug(slug: String): PostTagEntity? {
        val rows = db.preparedQuery("""SELECT * FROM POST_TAGS WHERE SLUG = ?""", Tuple.of(slug))
        return rows.firstOrNull()?.toPostTagEntity()
    }

    suspend fun getBySlugs(slugs: List<String>): List<PostTagEntity> {
        if (slugs.isEmpty()) return emptyList()
        val placeholders = slugs.indices.joinToString(", ") { "?" }
        val sql = """SELECT * FROM POST_TAGS WHERE SLUG IN ($placeholders)"""
        val rows = db.preparedQuery(sql, Tuple.from(slugs))
        return rows.map { it.toPostTagEntity() }
    }
}
