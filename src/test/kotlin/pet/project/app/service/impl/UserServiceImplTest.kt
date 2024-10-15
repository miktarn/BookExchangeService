package pet.project.app.service.impl

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import pet.project.app.dto.user.CreateUserRequest
import pet.project.app.dto.user.UpdateUserRequest
import pet.project.app.exception.BookNotFoundException
import pet.project.app.exception.UserNotFoundException
import pet.project.app.model.domain.DomainUser
import pet.project.app.repository.BookRepository
import pet.project.app.repository.UserRepository
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@ExtendWith(MockKExtension::class)
class UserServiceImplTest {

    @MockK
    lateinit var userRepositoryMock: UserRepository

    @MockK
    lateinit var bookRepositoryMock: BookRepository

    @InjectMockKs
    lateinit var userService: UserServiceImpl

    private val dummyStringWishList =
        setOf("66bf6bf8039339103054e21a", "66c3636647ff4c2f0242073d", "66c3637847ff4c2f0242073e")

        @Test
    fun `should create user successfully`() {
        // GIVEN
        val userId = ObjectId.get().toHexString()
        val expectedUser = DomainUser(userId, "testUser123", "test.user@test.com", dummyStringWishList)
        val testRequest = CreateUserRequest("testUser123", "test.user@test.com", dummyStringWishList)
        every { userRepositoryMock.insert(testRequest) } returns Mono.just(expectedUser)

        // WHEN
        StepVerifier.create(userService.create(testRequest))
            .expectNext(expectedUser)
            .verifyComplete()

        // THEN
        verify { userRepositoryMock.insert(testRequest) }
    }

    @Test
    fun `should retrieve user by id`() {
        // GIVEN
        val userId = "66c35b050da7b9523070cb3a"
        val expected = DomainUser(userId, "testUser123", "test.user@example.com", dummyStringWishList)
        every { userRepositoryMock.findById(userId) } returns Mono.just(expected)

        // WHEN
        StepVerifier.create(userService.getById(userId))
            .expectNext(expected)
            .verifyComplete()

        // THEN
        verify { userRepositoryMock.findById(userId) }
    }

    @Test
    fun `should return exception when user not found by id`() {
        // GIVEN
        val testRequestUserId = "66c35b050da7b9523070cb3a"
        every { userRepositoryMock.findById(testRequestUserId) } returns Mono.empty()

        // WHEN
        StepVerifier.create(userService.getById(testRequestUserId))
            .expectError(UserNotFoundException::class.java)
            .verify()

        // THEN
        verify { userRepositoryMock.findById(testRequestUserId) }
    }

    @Test
    fun `should update user successfully`() {
        // GIVEN
        val userId = "66c35b050da7b9523070cb3a"
        val updateUserRequest = UpdateUserRequest("John Doe", "test.user@example.com", dummyStringWishList)
        val updatedUser = DomainUser(userId, "John Doe", "test.user@example.com", dummyStringWishList)
        every { userRepositoryMock.update(userId, updateUserRequest) } returns Mono.just(updatedUser)

        // WHEN
        StepVerifier.create(userService.update(userId, updateUserRequest))
            .expectNext(updatedUser)
            .verifyComplete()

        // THEN
        verify { userRepositoryMock.update(userId, updateUserRequest) }
    }

    @Test
    fun `should throw UserNotFoundException when user not found while adding book to wishlist`() {
        // GIVEN
        val userId = "nonexistentUserId"
        val bookId = "60f1b13e8f1b2c000b355777"

        every { bookRepositoryMock.existsById(bookId) } returns Mono.just(true)
        every { userRepositoryMock.addBookToWishList(userId, bookId) } returns Mono.just(0L)

        // WHEN & THEN
        StepVerifier.create(userService.addBookToWishList(userId, bookId))
            .consumeErrorWith { ex ->
                assertEquals(UserNotFoundException::class.java, ex.javaClass)
                assertEquals(
                    "User with id=$userId was not found during adding book with id=$bookId into user wishlist",
                    ex.message
                )
            }
            .verify()

        verify { bookRepositoryMock.existsById(bookId) }
        verify { userRepositoryMock.addBookToWishList(userId, bookId) }
    }

    @Test
    fun `should throw BookNotFoundException when book does not exist while adding to wishlist`() {
        // GIVEN
        val userId = "validUserId"
        val bookId = "nonexistentBookId"

        every { bookRepositoryMock.existsById(bookId) } returns Mono.just(false)

        // WHEN & THEN
        StepVerifier.create(userService.addBookToWishList(userId, bookId))
            .consumeErrorWith { ex ->
                assertEquals(BookNotFoundException::class.java, ex.javaClass)
                assertEquals(
                    "Book with id=$bookId was not found during adding book to users (id=$userId) wishlist",
                    ex.message
                )
            }
            .verify()

        verify { bookRepositoryMock.existsById(bookId) }
        verify(exactly = 0) {
            userRepositoryMock.addBookToWishList(any(), any())
        }
    }

    @Test
    fun `should add book to user's wishlist successfully`() {
        // GIVEN
        val testRequestUserId = "66c35b050da7b9523070cb3a"
        val testRequestBookId = "66c3637847ff4c2f0242073e"
        every { bookRepositoryMock.existsById(testRequestBookId) } returns Mono.just(true)
        every { userRepositoryMock.addBookToWishList(testRequestUserId, testRequestBookId) } returns Mono.just(1L)

        // WHEN & THEN
        StepVerifier.create(userService.addBookToWishList(testRequestUserId, testRequestBookId))
            .expectNext(Unit)
            .verifyComplete()

        // THEN
        verify { bookRepositoryMock.existsById(testRequestBookId) }
        verify { userRepositoryMock.addBookToWishList(testRequestUserId, testRequestBookId) }
    }

    @Test
    fun `should throw BookNotFoundException when adding book to wishlist if book is not found`() {
        // GIVEN
        val testRequestUserId = "66c35b050da7b9523070cb3a"
        val testRequestBookId = "66c3637847ff4c2f0242073e"

        every { bookRepositoryMock.existsById(testRequestBookId) } returns Mono.just(false)

        // WHEN & THEN
        StepVerifier.create(userService.addBookToWishList(testRequestUserId, testRequestBookId))
            .expectError(BookNotFoundException::class.java)
            .verify()

        // THEN
        verify { bookRepositoryMock.existsById(testRequestBookId) }
    }

    @Test
    fun `should delete user successfully`() {
        // GIVEN
        val testRequestUserId = "66c35b050da7b9523070cb3a"
        every { userRepositoryMock.delete(testRequestUserId) } returns Mono.just(1L)

        // WHEN
        StepVerifier.create(userService.delete(testRequestUserId))
            .expectNext(Unit)
            .verifyComplete()

        // THEN
        verify { userRepositoryMock.delete(testRequestUserId) }
    }

    @Test
    fun `should log warning when trying to delete non-existent user`() {
        // GIVEN
        val logger: Logger = LoggerFactory.getLogger(UserServiceImpl::class.java) as Logger
        val listAppender = ListAppender<ILoggingEvent>().apply { start() }
        logger.addAppender(listAppender)

        val userId = "66c35b050da7b9523070cb3a"
        every { userRepositoryMock.delete(userId) } returns Mono.just(0L)

        // WHEN & THEN
        StepVerifier.create(userService.delete(userId))
            .expectNext(Unit)
            .verifyComplete()

        // THEN
        val logs = listAppender.list
        val expectedMessage = "Affected 0 documents while trying to delete user with id=$userId"
        assertEquals(expectedMessage, logs.first().formattedMessage)
        assertEquals(Level.WARN, logs.first().level)
    }
}
