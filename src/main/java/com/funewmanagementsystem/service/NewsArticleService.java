package com.funewmanagementsystem.service;

import com.funewmanagementsystem.dto.request.UpdateNewsArticleRequest;
import com.funewmanagementsystem.dto.respone.NewsArticleResponse;
import com.funewmanagementsystem.dto.respone.PageResponse;
import com.funewmanagementsystem.entity.NewsArticle;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;

public interface NewsArticleService {
    NewsArticle getById(long id);
    NewsArticleResponse createNewsArticle(UpdateNewsArticleRequest newsArticleRequest);

    @Transactional
    NewsArticleResponse updateNewsArticle(UpdateNewsArticleRequest newsArticleRequest, Long id);

    @Transactional
    void deleteNewsArticle(Long id);

    PageResponse<NewsArticleResponse> search(String title, Long categoryId, Boolean status, Pageable pageable);
}
