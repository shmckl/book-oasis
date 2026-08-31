package com.bookoasis.service;

import com.bookoasis.dto.BookRequest;
import com.bookoasis.dto.BookResponse;
import com.bookoasis.dto.PagedResponse;
import com.bookoasis.exception.BookNotFoundException;
import com.bookoasis.model.BookEntity;
import com.bookoasis.repository.BookRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public BookResponse findById(Long id) {
        return bookRepository.findById(id)
                .map(BookResponse::from)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public PagedResponse<BookResponse> findAll(Pageable pageable) {
        return PagedResponse.from(bookRepository.findAll(pageable), BookResponse::from);
    }

    @Transactional
    public BookResponse update(Long id, BookRequest request) {
        BookEntity entity = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));

        entity.setTitle(request.title());
        entity.setAuthor(request.author());
        entity.setPublicationYear(request.publicationYear());

        return BookResponse.from(bookRepository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new BookNotFoundException(id);
        }
        bookRepository.deleteById(id);
    }
}