package pet.project.app.service.impl

import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.verify
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.repository.findByIdOrNull
import pet.project.app.exception.BookNotFoundException
import pet.project.app.exception.UserNotFoundException
import pet.project.app.model.User
import pet.project.app.repository.BookRepository
import pet.project.app.repository.UserRepository

@ExtendWith(MockKExtension::class)
class UserServiceImplTest {

    @MockK
    lateinit var userRepositoryMock: UserRepository

    @MockK
    lateinit var bookRepositoryMock: BookRepository

    @InjectMockKs
    lateinit var userService: UserServiceImpl

    private val dummyWishlist = setOf(
        "66bf6bf8039339103054e21a",
        "66c3636647ff4c2f0242073d",
        "66c3637847ff4c2f0242073e",
    )

    @Test
    fun `check creates user`() {
        // GIVEN
        val inputUser = User(login = "testUser123", bookWishList = dummyWishlist)
        val expected = User(ObjectId("66c35b050da7b9523070cb3a"), "testUser123", dummyWishlist)
        every { userRepositoryMock.save(inputUser) } returns expected

        // WHEN
        val actual = userService.create(inputUser)

        // THEN
        verify { userRepositoryMock.save((inputUser)) }
        assertEquals(expected, actual)
    }

    @Test
    fun `check getting user`() {
        // GIVEN
        val testRequestUserId = "66c35b050da7b9523070cb3a"
        val expected = User(ObjectId("66c35b050da7b9523070cb3a"), "testUser123", dummyWishlist)
        every { userRepositoryMock.findByIdOrNull(testRequestUserId) } returns expected

        // WHEN
        val actual = userService.getById(testRequestUserId)

        // THEN
        verify { userRepositoryMock.findByIdOrNull(testRequestUserId) }
        assertEquals(expected, actual)
    }

    @Test
    fun `check getting user by id throws exception when not found`() {
        // GIVEN
        val testRequestUserId = "66c35b050da7b9523070cb3a"
        every { userRepositoryMock.findByIdOrNull(testRequestUserId) } returns null

        // WHEN
        assertThrows<UserNotFoundException> {
            userService.getById(testRequestUserId)
        }

        // THEN
        verify { userRepositoryMock.findByIdOrNull(testRequestUserId) }
    }

    @Test
    fun `check updating user`() {
        // GIVEN
        val testRequestUserId = "66c35b050da7b9523070cb3a"
        val user = User(ObjectId(testRequestUserId), "John Doe", dummyWishlist)
        every { userRepositoryMock.existsById(testRequestUserId) } returns true
        every { userRepositoryMock.save(user) } returns user

        // WHEN
        val result = userService.update(user)

        // THEN
        assertEquals(user, result)
        verify { userRepositoryMock.existsById(testRequestUserId) }
        verify { userRepositoryMock.save(user) }
    }

    @Test
    fun `check updating user throws exception when user not found`() {
        // GIVEN
        val testRequestUserId = "66c35b050da7b9523070cb3a"
        val user = User(ObjectId(testRequestUserId), "John Doe", dummyWishlist)
        every { userRepositoryMock.existsById(testRequestUserId) } returns false

        // WHEN
        assertThrows<UserNotFoundException> {
            userService.update(user)
        }

        // THEN
        verify { userRepositoryMock.existsById(testRequestUserId) }
    }

    @Test
    fun `addBookToWishList should throw UserNotFoundException if user not found`() {
        // GIVEN
        val userId = "nonexistentUserId"
        val bookId = "60f1b13e8f1b2c000b355777"

        every { userRepositoryMock.findByIdOrNull(userId) } returns null

        // WHEN
        val exception = assertThrows<UserNotFoundException> {
            userService.addBookToWishList(userId, bookId)
        }

        // THEN
        assertEquals(
            "User with id=$userId was not found during adding book with id=$bookId into user wishlist",
            exception.message,
        )
    }

    @Test
    fun `check adding book to wishlist`() {
        // GIVEN
        val testRequestUserId = "66c35b050da7b9523070cb3a"
        val testRequestBookId = "66c3637847ff4c2f0242073e"
        val user = User(ObjectId(testRequestUserId), "John Doe", setOf())
        every { userRepositoryMock.findByIdOrNull(testRequestUserId) } returns user
        every { bookRepositoryMock.existsById(testRequestBookId) } returns true
        every { userRepositoryMock.save(any()) } returns user.copy(bookWishList = setOf(testRequestBookId))

        // WHEN
        val result = userService.addBookToWishList(testRequestUserId, testRequestBookId)

        // THEN
        assertTrue(result.bookWishList.contains(testRequestBookId))
        verify { userRepositoryMock.findByIdOrNull(testRequestUserId) }
        verify { bookRepositoryMock.existsById(testRequestBookId) }
        verify { userRepositoryMock.save(any()) }
    }

    @Test
    fun `check adding book to wishlist throws exception when book not found`() {
        // GIVEN
        val testRequestUserId = "66c35b050da7b9523070cb3a"
        val testRequestBookId = "66c3637847ff4c2f0242073e"
        val user = User(ObjectId(testRequestUserId), "John Doe", dummyWishlist)

        every { userRepositoryMock.findByIdOrNull(testRequestUserId) } returns user
        every { bookRepositoryMock.existsById(testRequestBookId) } returns false

        // WHEN
        assertThrows<BookNotFoundException> {
            userService.addBookToWishList(testRequestUserId, testRequestBookId)
        }

        // THEN
        verify { userRepositoryMock.findByIdOrNull(testRequestUserId) }
        verify { bookRepositoryMock.existsById(testRequestBookId) }
    }

    @Test
    fun `check delete user`() {
        // GIVEN
        val testRequestUserId = "66c35b050da7b9523070cb3a"
        every { userRepositoryMock.existsById(testRequestUserId) } returns true
        every { userRepositoryMock.deleteById(testRequestUserId) } just Runs

        // WHEN
        userService.delete(testRequestUserId)

        // THEN
        verify { userRepositoryMock.existsById(testRequestUserId) }
        verify { userRepositoryMock.deleteById(testRequestUserId) }
    }

    @Test
    fun `check deleting user when user does not exist`() {
        // GIVEN
        val testRequestUserId = "66c35b050da7b9523070cb3a"
        every { userRepositoryMock.existsById(testRequestUserId) } returns false

        // WHEN
        userService.delete(testRequestUserId)

        // THEN
        verify { userRepositoryMock.existsById(testRequestUserId) }
        verify(exactly = 0) { userRepositoryMock.deleteById(testRequestUserId) }
    }
}
