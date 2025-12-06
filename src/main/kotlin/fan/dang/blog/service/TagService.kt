package fan.dang.blog.service

import fan.dang.blog.dao.PostTagDao
import fan.dang.blog.models.PostTagEntity

class TagService(private val postTagDao: PostTagDao) {
    suspend fun all() = postTagDao.all()

    suspend fun insert(tag: PostTagEntity) = postTagDao.insert(tag)

    suspend fun replaceAll(tags: List<PostTagEntity>) {
        postTagDao.clear()
        tags.forEach { postTagDao.insert(it) }
    }

    suspend fun delete(slug: String) = postTagDao.delete(slug)

    suspend fun getBySlug(slug: String) = postTagDao.getBySlug(slug)

    suspend fun getBySlugs(slugs: List<String>) = postTagDao.getBySlugs(slugs)
}
