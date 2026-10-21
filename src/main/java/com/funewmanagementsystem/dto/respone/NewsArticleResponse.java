package com.funewmanagementsystem.dto.respone;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewsArticleResponse {
    private Long id;
    private String newsTitle;
    private String headline;
    private String newsContent;
    private String newsSource;
    private Boolean newsStatus;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private Long  categoryId;
    private String createdByName;
    private Long updatedById;
    private List<String> tags;
}
