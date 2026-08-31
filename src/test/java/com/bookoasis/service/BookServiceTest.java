package com.bookoasis.service;

import com.bookoasis.dto.BookRequest;
import com.bookoasis.dto.BookResponse;
import com.bookoasis.dto.PagedResponse;
import com.bookoasis.exception.BookNotFoundException;
import com.bookoasis.model.BookEntity;
import com.bookoasis.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    @Test
    void createsABookFromTheRequest() {
        when(bookRepository.save(any(BookEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        bookService.create(new BookRequest("Atomic Habits", "James Clear", 2018));

        ArgumentCaptor<BookEntity> captor = ArgumentCaptor.forClass(BookEntity.class);
        verify(bookRepository).save(captor.capture());

        BookEntity saved = captor.getValue();
        assertThat(saved.getTitle()).isEqualTo("Atomic Habits");
        assertThat(saved.getAuthor()).isEqualTo("James Clear");
        assertThat(saved.getPublicationYear()).isEqualTo(2018);
    }

    @Test
    void mapsAFoundBookToAResponse() {
        BookEntity entity = new BookEntity("Atomic Habits", "James Clear", 2018);
        ReflectionTestUtils.setField(entity, "id", 1L);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(entity));

        BookResponse result = bookService.findById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.title()).isEqualTo("Atomic Habits");
    }

    @Test
    void throwsWhenBookDoesNotExist() {
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.findById(999L))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void returnsAPageOfBooksWithPagingMetadata() {
        BookEntity first = new BookEntity("Atomic Habits", "James Clear", 2018);
        ReflectionTestUtils.setField(first, "id", 1L);
        BookEntity second = new BookEntity("Clean Code", "Robert C. Martin", 2008);
        ReflectionTestUtils.setField(second, "id", 2L);

        Pageable pageable = PageRequest.of(0, 2);
        when(bookRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(first, second), pageable, 5));

        PagedResponse<BookResponse> result = bookService.findAll(pageable);

        assertThat(result.content()).hasSize(2);
        assertThat(result.content().getFirst().title()).isEqualTo("Atomic Habits");
        assertThat(result.page()).isZero();
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.totalElements()).isEqualTo(5);
        assertThat(result.totalPages()).isEqualTo(3);
        assertThat(result.first()).isTrue();
        assertThat(result.last()).isFalse();
    }

    @Test
    void updatesAnExistingBook() {
        BookEntity entity = new BookEntity("Clean Code", "Robert Martin", 2008);
        ReflectionTestUtils.setField(entity, "id", 1L);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(bookRepository.save(any(BookEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BookResponse result = bookService.update(
                1L, new BookRequest("Clean Code", "Robert C. Martin", 2008));

        assertThat(result.author()).isEqualTo("Robert C. Martin");
        assertThat(entity.getAuthor()).isEqualTo("Robert C. Martin");
    }

    @Test
    void throwsWhenUpdatingABookThatDoesNotExist() {
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.update(
                999L, new BookRequest("Nothing", "Nobody", 2000)))
                .isInstanceOf(BookNotFoundException.class);

        verify(bookRepository, never()).save(any(BookEntity.class));
    }

    @Test
    void deletesAnExistingBook() {
        when(bookRepository.existsById(1L)).thenReturn(true);

        bookService.delete(1L);

        verify(bookRepository).deleteById(1L);
    }

    @Test
    void throwsWhenDeletingABookThatDoesNotExist() {
        when(bookRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> bookService.delete(999L))
                .isInstanceOf(BookNotFoundException.class);

        verify(bookRepository, never()).deleteById(anyLong());
    }
}