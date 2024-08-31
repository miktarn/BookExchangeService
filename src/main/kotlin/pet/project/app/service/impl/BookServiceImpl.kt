package pet.project.app.service.impl

import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import pet.project.app.annotation.Profiling
import pet.project.app.exception.BookNotFoundException
import pet.project.app.model.Book
import pet.project.app.repository.BookRepository
import pet.project.app.service.BookService
import kotlin.math.abs

@Profiling
@Service
class BookServiceImpl(private val bookRepository: BookRepository) : BookService {

    override fun create(book: Book): Book = bookRepository.save(book)

    override fun getById(bookId: String): Book =
        bookRepository.findByIdOrNull(bookId) ?: throw BookNotFoundException(bookId, "GET request")

    override fun update(book: Book): Book {
        if (bookRepository.existsById(book.id!!.toHexString())) {
            return bookRepository.save(book)
        } else {
            throw BookNotFoundException(book.id.toHexString(), "UPDATE request")
        }

    }

    override fun changeAmount(bookId: String, delta: Int): Int {
        val book = getById(bookId)
        val updatedAmount = (book.amountAvailable ?: 0) + delta
        check(updatedAmount >= 0) { "Can't withdraw ${abs(delta)} book(s), when amount is ${book.amountAvailable}" }
        val updatedBook = book.copy(amountAvailable = updatedAmount)
        bookRepository.save(updatedBook)
        if (updatedAmount == delta) {
            log.info("Message for subscribers of book (id={})", bookId)
        }
        return updatedAmount
    }

    override fun delete(bookId: String) {
        if (bookRepository.existsById(bookId)) {
            bookRepository.deleteById(bookId)
        } else {
            log.warn("Attempting to delete absent book with id={}", bookId)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(BookServiceImpl::class.java)
    }
}