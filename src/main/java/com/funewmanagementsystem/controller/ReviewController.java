package com.funewmanagementsystem.controller;

import com.funewmanagementsystem.dto.request.ReviewRequest;
import com.funewmanagementsystem.dto.respone.ArticleDetailResponse;
import com.funewmanagementsystem.dto.respone.PageResponse;
import com.funewmanagementsystem.dto.respone.WorkflowLogResponse;
import com.funewmanagementsystem.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @PutMapping("/{id}/request-changes")
    public ResponseEntity<ArticleDetailResponse> requestChanges(@PathVariable long id,
                                                                @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(reviewService.requestChanges(id, request.getNote()));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ArticleDetailResponse> reject(@PathVariable long id,
                                                        @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(reviewService.reject(id, request.getNote()));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ArticleDetailResponse> approve(@PathVariable long id,
                                                         @Valid @RequestBody(required = false) ReviewRequest request) {
        ReviewRequest r = (request == null) ? new ReviewRequest() : request;
        return ResponseEntity.ok(reviewService.approve(id, r.getPublishDate(), r.getNote()));
    }

    @PutMapping("/{id}/publish")
    public ResponseEntity<ArticleDetailResponse> publishNow(@PathVariable long id) {
        return ResponseEntity.ok(reviewService.publishNow(id));
    }

    @PutMapping("/{id}/archive")
    public ResponseEntity<ArticleDetailResponse> archive(@PathVariable long id,
                                                         @Valid @RequestBody(required = false) ReviewRequest request) {
        return ResponseEntity.ok(reviewService.archive(id, request == null ? null : request.getNote()));
    }
}
