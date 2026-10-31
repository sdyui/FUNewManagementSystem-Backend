package com.funewmanagementsystem.service;

import com.funewmanagementsystem.dto.respone.ArticleDetailResponse;
import com.funewmanagementsystem.dto.respone.PageResponse;
import com.funewmanagementsystem.dto.respone.WorkflowLogResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReviewService {
    PageResponse<ArticleDetailResponse> getPendingQueue(Pageable pageable);
    ArticleDetailResponse getArticle(Long id);
    List<WorkflowLogResponse> history(Long id);
}