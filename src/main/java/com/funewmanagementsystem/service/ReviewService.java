package com.funewmanagementsystem.service;

import com.funewmanagementsystem.dto.respone.ArticleDetailResponse;
import com.funewmanagementsystem.dto.respone.PageResponse;
import com.funewmanagementsystem.dto.respone.WorkflowLogResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface ReviewService {
    PageResponse<ArticleDetailResponse> getPendingQueue(Pageable pageable);
    ArticleDetailResponse getArticle(Long id);
    List<WorkflowLogResponse> history(Long id);
    ArticleDetailResponse requestChanges(Long id, String note);
    ArticleDetailResponse reject(Long id, String note);
    ArticleDetailResponse approve(Long id, LocalDateTime publishDate, String note);
    ArticleDetailResponse publishNow(Long id);
    ArticleDetailResponse archive(Long id, String note);
}