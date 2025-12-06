package fan.dang.blog.service

import fan.dang.blog.config.BlogOptions
import fan.dang.blog.dao.BlogrollDao
import fan.dang.blog.models.BlogrollEntity

class BlogrollService(private val blogrollDao: BlogrollDao) {
    suspend fun load() = blogrollDao.load()

    fun current(): List<BlogrollEntity> = BlogOptions.blogrolls

    suspend fun reset(blogrolls: List<BlogrollEntity>) = blogrollDao.reset(blogrolls)
}
