package com.bookoasis.book_oasis.repository;

import com.bookoasis.book_oasis.model.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<BookEntity, Long> {
}