package com.funewmanagementsystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateArticleRequest {

    @NotBlank(message = "Title cannot be empty")
    @Size(max = 200 , message = "Title must not exceed 200 characters")
    private String title;

    private String content;

    @Size(max = 500, message = "Thumbnail URL must not exceed 500 characters ")
    private String thumbnail;

    private Long categoryId;
    private Set<Long> tagIds;
}
