package com.bookoasis.controller;

import com.bookoasis.dto.BookRequest;
import com.bookoasis.dto.BookResponse;
import com.bookoasis.dto.PagedResponse;
import com.bookoasis.service.BookService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    public ResponseEntity<BookResponse> create(@RequestBody BookRequest request) {
        BookResponse created = bookService.create(request);
        return ResponseEntity
                .created(URI.create("/api/books/" + created.id()))
                .body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getById(@PathVariable Long id) {
        return bookService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<PagedResponse<BookResponse>> getAll(
            @PageableDefault(size = 10, sort = "title") Pageable pageable) {
        return ResponseEntity.ok(bookService.findAll(pageable));
    }
}