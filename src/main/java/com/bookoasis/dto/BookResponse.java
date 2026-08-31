package com.bookoasis.dto;

import com.bookoasis.model.BookEntity;

public record BookResponse(
        Long id,
        String title,
        String author,
        Integer publicationYear
) {

    public static BookResponse from(BookEntity entity) {
        return new BookResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getAuthor(),
                entity.getPublicationYear()
        );
    }
}