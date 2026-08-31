package com.bookoasis.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BookRequest(

        @NotBlank(message = "Title is required")
        @Size(max = 255, message = "Title must be 255 characters or fewer")
        String title,

        @NotBlank(message = "Author is required")
        @Size(max = 255, message = "Author must be 255 characters or fewer")
        String author,

        @NotNull(message = "Publication year is required")
        @Min(value = 1450, message = "Publication year must be 1450 or later")
        @Max(value = 2100, message = "Publication year must be 2100 or earlier")
        Integer publicationYear
) {
}