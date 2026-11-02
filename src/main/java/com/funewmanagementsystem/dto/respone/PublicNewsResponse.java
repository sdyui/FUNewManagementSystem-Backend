package com.funewmanagementsystem.dto.respone;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicNewsResponse {
    private Long id;
    private String title;
    private String headline;
    private String thumbnail;
    private String content;
    private String newsSource;
    private Long categoryId;
    private String categoryName;
    private String authorName;
    private LocalDateTime publishDate;
}