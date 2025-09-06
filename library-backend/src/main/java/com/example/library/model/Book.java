package com.example.library.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false)
    private String author;
    
    @Column(unique = true, nullable = false)
    private String isbn;

    @Column(name = "publication_year")
    private Integer publicationYear;
    
    @Column(name = "available_copies")
    private Integer availableCopies = 1;
    
    @Column(name = "total_copies")
    private Integer totalCopies = 1;
    
    @Column(name = "created_at")
    private LocalDate createdAt = LocalDate.now();
    
    @Column(name = "updated_at")
    private LocalDate updatedAt = LocalDate.now();
}
