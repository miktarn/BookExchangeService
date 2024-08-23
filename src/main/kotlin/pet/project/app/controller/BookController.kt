package pet.project.app.controller

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import pet.project.app.dto.book.BookMapper.toDto
import pet.project.app.dto.book.BookMapper.toModel
import pet.project.app.dto.book.CreateBookRequest
import pet.project.app.dto.book.ResponseBookDto
import pet.project.app.dto.book.UpdateAmountRequest
import pet.project.app.dto.book.UpdateBookRequest
import pet.project.app.service.BookService

@RestController
@RequestMapping("/book")
class BookController(private val bookService: BookService) {

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody request: CreateBookRequest): ResponseBookDto {
        val createdBook = bookService.create(request.toModel())
        return createdBook.toDto()
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable("id") id: String): ResponseBookDto =
        bookService.getById(id).toDto()


    @PutMapping("/")
    fun update(@RequestBody request: UpdateBookRequest): ResponseBookDto {
        val updatedBook = bookService.update(request.toModel())
        return updatedBook.toDto()
    }

    @PatchMapping("/{id}/amount")
    fun updateAmount(
        @PathVariable("id") id: String,
        @RequestBody request: UpdateAmountRequest,
    ): Int {
        return bookService.changeAmount(id, request.delta)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable("id") id: String) = bookService.delete(id)
}
