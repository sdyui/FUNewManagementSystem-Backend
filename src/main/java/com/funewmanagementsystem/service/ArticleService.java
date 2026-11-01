package com.funewmanagementsystem.service;


import com.funewmanagementsystem.dto.request.CreateArticleRequest;
import com.funewmanagementsystem.dto.request.UpdateArticleRequest;
import com.funewmanagementsystem.dto.respone.ArticleDetailResponse;
import com.funewmanagementsystem.dto.respone.PageResponse;
import com.funewmanagementsystem.dto.respone.SubmitArticleResponse;
import com.funewmanagementsystem.dto.respone.TagResponse;
import com.funewmanagementsystem.entity.ArticleStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface ArticleService {

    ArticleDetailResponse createArticle(CreateArticleRequest request);
    ArticleDetailResponse updateArticle(Long id, UpdateArticleRequest request);
    ArticleDetailResponse getArticleDetail(Long id);
    PageResponse<ArticleDetailResponse> getMyArticles(ArticleStatus status, Pageable pageable);
    SubmitArticleResponse submitForReview(Long id);
    List<TagResponse> getAllTags();
}
