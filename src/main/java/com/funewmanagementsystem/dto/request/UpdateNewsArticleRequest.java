package com.funewmanagementsystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateNewsArticleRequest {
    @NotBlank(message = "Title can not empty")
    @Size(max = 200, message = "Title must be at most 200 characters")
    private String newsTitle;
    @Size(max = 500, message = "Headline must be at most 500 characters")
    private String newsHeadline;
    private String newContent;
    @Size(max = 255, message = "Source must be at most 255 characters")
    private String newsSource;
    @NotNull(message = "Category is required")
    private Long categoryId;
    @NotNull(message = "Account is required")
    private Long accountId;
    private Boolean newsStatus;
    private Set<Long> tagIds = new HashSet<>();

}
