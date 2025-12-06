package fan.dang.blog.service

import fan.dang.blog.dao.PostDao
import fan.dang.blog.models.PostEntity
import fan.dang.blog.models.PostStatus
import fan.dang.blog.models.PostType

class PostService(private val postDao: PostDao) {
    suspend fun listByPage(page: Int, type: PostType, status: PostStatus) =
        postDao.listByPage(page, type, status)

    suspend fun listByTag(slug: String, page: Int) = postDao.listByTag(slug, page)

    suspend fun getBySlug(slug: String) = postDao.getBySlug(slug)

    suspend fun insert(post: PostEntity) = postDao.insert(post)

    suspend fun update(post: PostEntity) = postDao.update(post)

    suspend fun delete(slug: String) = postDao.delete(slug)
}
