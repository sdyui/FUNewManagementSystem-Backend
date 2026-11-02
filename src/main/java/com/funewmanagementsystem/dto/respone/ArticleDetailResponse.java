package com.funewmanagementsystem.dto.respone;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.funewmanagementsystem.entity.ArticleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDetailResponse {
    private Long id;
    private String title;
    private String thumbnail;
    private String content;
    private ArticleStatus status;
    private Long categoryId;
    private String categoryName;
    private List<Long> tagIds;
    private List<String> tagNames;
    private AuthorDto author;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorDto {
        private Long id;
        private String name;
        private String email;
    }
}
