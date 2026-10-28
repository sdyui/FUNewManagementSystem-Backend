package com.funewmanagementsystem.dto.respone;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.funewmanagementsystem.entity.ArticleStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmitArticleResponse {
    @JsonProperty("article_id")
    private Long articleId;

    @JsonProperty("new_status")
    private ArticleStatus articleStatus;

    private String message;
}
