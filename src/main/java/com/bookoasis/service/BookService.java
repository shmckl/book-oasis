package com.bookoasis.service;

import com.bookoasis.dto.BookRequest;
import com.bookoasis.dto.BookResponse;
import com.bookoasis.model.BookEntity;
import com.bookoasis.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Transactional
    public BookResponse create(BookRequest request) {
        BookEntity entity = new BookEntity(
                request.title(),
                request.author(),
                request.publicationYear()
        );
        return BookResponse.from(bookRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public Optional<BookResponse> findById(Long id) {
        return bookRepository.findById(id).map(BookResponse::from);
    }
}