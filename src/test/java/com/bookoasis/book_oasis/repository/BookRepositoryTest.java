package com.bookoasis.book_oasis.repository;

import com.bookoasis.book_oasis.model.BookEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BookRepositoryTest {

    public static final String AH = "Atomic Habits";
    public static final String JAMES_CLEAR = "James Clear";
    public static final int PUB_YEAR = 2018;
    @Autowired
    private BookRepository bookRepository;

    @Test
    void savesAndRetrievesABook() {
        BookEntity saved = bookRepository.save(new BookEntity(AH, JAMES_CLEAR, PUB_YEAR));

        Optional<BookEntity> found = bookRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo(AH);
        assertThat(found.get().getAuthor()).isEqualTo(JAMES_CLEAR);
        assertThat(found.get().getPublicationYear()).isEqualTo(PUB_YEAR);
    }
}