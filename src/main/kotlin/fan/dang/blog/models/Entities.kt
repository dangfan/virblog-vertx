package fan.dang.blog.models

import java.time.LocalDateTime

data class PostEntity(
    val id: Int? = null,
    val slug: String,
    val time: LocalDateTime,
    val title: Map<String, String>,
    val subtitle: Map<String, String>,
    val excerpt: Map<String, String>,
    val content: Map<String, String>,
    val headerImage: String,
    val status: PostStatus,
    val postType: PostType,
    val tags: List<String>
)

data class UserEntity(
    val username: String,
    val password: String,
    val email: String? = null,
    val nickname: String? = null
)

data class PostTagEntity(
    val slug: String,
    val name: Map<String, String>
)

data class BlogrollEntity(
    val name: String,
    val link: String
)

data class OptionInfo(
    val blogName: Map<String, String>,
    val blogDescription: Map<String, String>,
    val locales: Map<String, String>,
    val datetimeFormat: Map<String, String>,
    val defaultLocale: String,
    val pageSize: Int,
    val disqusShortName: String,
    val gaId: String
)

data class LoginInfo(
    val username: String,
    val password: String
)
