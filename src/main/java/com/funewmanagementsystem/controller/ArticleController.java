package com.funewmanagementsystem.controller;

import com.funewmanagementsystem.dto.request.CreateArticleRequest;
import com.funewmanagementsystem.dto.request.UpdateArticleRequest;
import com.funewmanagementsystem.dto.respone.ArticleDetailResponse;
import com.funewmanagementsystem.dto.respone.PageResponse;
import com.funewmanagementsystem.dto.respone.SubmitArticleResponse;
import com.funewmanagementsystem.dto.respone.TagResponse;
import com.funewmanagementsystem.entity.ArticleStatus;
import com.funewmanagementsystem.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/articles", "/api/v1/articles"})
@RequiredArgsConstructor
@Tag(name = "Contributor Article Management", description = "Các API dành cho Contributor quản lý bài viết")
public class ArticleController {

    private final ArticleService articleService;

    // 1. Tạo bài viết mới
    @Operation(summary = "Tạo bài viết mới (Status mặc định là DRAFT)")
    @PostMapping
    public ResponseEntity<ArticleDetailResponse> createArticle(@Valid @RequestBody CreateArticleRequest request) {
        ArticleDetailResponse response = articleService.createArticle(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2. Xem danh sách bài viết của tác giả hiện tại
    @Operation(summary = "Xem danh sách bài viết của chính tác giả đang đăng nhập")
    @GetMapping("/my")
    public ResponseEntity<PageResponse<ArticleDetailResponse>> getMyArticles(
            @RequestParam(required = false) ArticleStatus status,
            @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<ArticleDetailResponse> response = articleService.getMyArticles(status, pageable);
        return ResponseEntity.ok(response);
    }

    // 3. Xem chi tiết bài viết theo ID
    @Operation(summary = "Xem chi tiết bài viết theo ID")
    @GetMapping("/{id}")
    public ResponseEntity<ArticleDetailResponse> getArticleDetail(@PathVariable Long id) {
        ArticleDetailResponse response = articleService.getArticleDetail(id);
        return ResponseEntity.ok(response);
    }

    // 4. Chỉnh sửa bài viết
    @Operation(summary = "Chỉnh sửa bài viết của chính mình (chỉ cho phép khi ở DRAFT hoặc CHANGES_REQUESTED)")
    @PutMapping("/{id}")
    public ResponseEntity<ArticleDetailResponse> updateArticle(
            @PathVariable Long id,
            @Valid @RequestBody UpdateArticleRequest request) {
        ArticleDetailResponse response = articleService.updateArticle(id, request);
        return ResponseEntity.ok(response);
    }

    // 5. Gửi bài viết để xét duyệt
    @Operation(summary = "Gửi bài viết để xét duyệt (chuyển status từ DRAFT sang PENDING_REVIEW)")
    @PutMapping("/{id}/submit")
    public ResponseEntity<SubmitArticleResponse> submitArticle(@PathVariable Long id) {
        SubmitArticleResponse response = articleService.submitForReview(id);
        return ResponseEntity.ok(response);
    }

    // 6. Lấy danh sách toàn bộ Tags
    @Operation(summary = "Get all tags (for the article form)")
    @GetMapping("/tags")
    public ResponseEntity<List<TagResponse>> getAllTags() {
        return ResponseEntity.ok(articleService.getAllTags());
    }
}
