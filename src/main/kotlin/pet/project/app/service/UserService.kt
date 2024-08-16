package pet.project.app.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import pet.project.app.exception.UserNotFoundException
import pet.project.app.model.User
import pet.project.app.repository.UserRepository

@Service
class UserService(private val userRepository: UserRepository) {
    fun create(user: User): User {
        return userRepository.save(user)
    }

    fun getById(userId: String): User {
        return userRepository.findByIdOrNull(userId) ?: throw UserNotFoundException(userId, "GET request")
    }

    fun update(userId: String, user: User): User {
        if (userRepository.existsById(userId)) {
            user.id = userId
            return userRepository.save(user)
        }
        throw UserNotFoundException(userId, "UPDATE request")
    }

    fun addBookToWishList(userId: String, bookId: String): Boolean {
        TODO("Implement")
    }

    fun delete(userId: String) {
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId)
        }
        throw UserNotFoundException(userId, "DELETE request")
    }
}
