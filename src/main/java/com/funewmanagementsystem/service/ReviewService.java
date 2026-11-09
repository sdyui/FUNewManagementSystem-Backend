package com.funewmanagementsystem.service;

import com.funewmanagementsystem.dto.respone.ArticleDetailResponse;
import com.funewmanagementsystem.dto.respone.AuditLogResponse;
import com.funewmanagementsystem.dto.respone.PageResponse;
import com.funewmanagementsystem.dto.respone.WorkflowLogResponse;
import com.funewmanagementsystem.entity.ArticleStatus;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
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
    PageResponse<AuditLogResponse> searchLogs(String keyword, String actor, LocalDate from, LocalDate to, Pageable pageable, ArticleStatus toStatus);
}