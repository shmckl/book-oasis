package com.bookoasis.dto;

public record BookRequest(
        String title,
        String author,
        Integer publicationYear
) {
}