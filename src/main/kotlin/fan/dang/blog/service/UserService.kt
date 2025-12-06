package fan.dang.blog.service

import fan.dang.blog.dao.UserDao
import fan.dang.blog.models.UserEntity

class UserService(private val userDao: UserDao) {
    suspend fun login(username: String, password: String) = userDao.login(username, password)

    suspend fun getUser(token: String) = userDao.getUser(token)

    suspend fun updatePassword(username: String, newPassword: String, oldPassword: String) =
        userDao.updatePassword(username, newPassword, oldPassword)

    suspend fun update(user: UserEntity) = userDao.update(user)
}
