package fan.dang.blog.service

import fan.dang.blog.config.BlogOptions
import fan.dang.blog.dao.OptionsDao
import fan.dang.blog.models.OptionInfo

class OptionsService(private val optionsDao: OptionsDao) {
    suspend fun loadOptions() = optionsDao.load()

    fun snapshot(): OptionInfo = OptionInfo(
        blogName = BlogOptions.blogName,
        blogDescription = BlogOptions.blogDescription,
        locales = BlogOptions.locales,
        datetimeFormat = BlogOptions.datetimeFormat,
        defaultLocale = BlogOptions.defaultLocale,
        pageSize = BlogOptions.pageSize,
        disqusShortName = BlogOptions.disqusShortName,
        gaId = BlogOptions.gaId
    )

    suspend fun update(options: OptionInfo) {
        optionsDao.setBlogName(options.blogName)
        optionsDao.setBlogDescription(options.blogDescription)
        optionsDao.setLocales(options.locales)
        optionsDao.setDatetimeFormat(options.datetimeFormat)
        optionsDao.setDefaultLocale(options.defaultLocale)
        optionsDao.setPageSize(options.pageSize)
        optionsDao.setDisqusShortName(options.disqusShortName)
        optionsDao.setGAId(options.gaId)
    }
}
