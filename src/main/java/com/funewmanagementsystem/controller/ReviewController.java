package com.funewmanagementsystem.controller;

import com.funewmanagementsystem.dto.respone.ArticleDetailResponse;
import com.funewmanagementsystem.dto.respone.PageResponse;
import com.funewmanagementsystem.dto.respone.WorkflowLogResponse;
import com.funewmanagementsystem.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/review")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;
    @GetMapping("/pending")
    public ResponseEntity<PageResponse<ArticleDetailResponse>> getPendingReviews(@ParameterObject @PageableDefault(size = 10, sort = "modifiedDate", direction = Sort.Direction.ASC) Pageable pageable) {
        return  ResponseEntity.ok(reviewService.getPendingQueue(pageable));
    }
    @GetMapping("/{id}")
    public ResponseEntity<ArticleDetailResponse> getArticle(@PathVariable long id) {
        return ResponseEntity.ok(reviewService.getArticle(id));
    }
    @GetMapping("/{id}/history")
    public ResponseEntity<List<WorkflowLogResponse>> history(@PathVariable long id) {
        return ResponseEntity.ok(reviewService.history(id));
    }
}
