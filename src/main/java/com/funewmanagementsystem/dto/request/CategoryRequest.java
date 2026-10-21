package com.funewmanagementsystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {

    @NotBlank(message = "Category name cannot be empty")
    @Size(max = 100, message = "Category name must be at most 100 characters")
    private String categoryName;

    @Size(max = 1000, message = "Description must be at most 1000 characters")
    private String categoryDescription;

    private Long parentCategoryId;

    private Boolean isActive = true;
}
