package com.example.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class BookRequest {
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Author is required")
    private String author;
    
    @NotBlank(message = "ISBN is required")
    private String isbn;
    
    @PositiveOrZero(message = "Publication year must be a positive number or zero")
    private Integer publicationYear;
    
    @PositiveOrZero(message = "Total copies must be a positive number or zero")
    private Integer totalCopies = 1;
}
