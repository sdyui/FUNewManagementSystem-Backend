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
@CrossOrigin(origins = "*")
@Tag(name = "Contributor Article Management", description = "Các API dành cho Contributor quản lý bài viết")
public class ArticleController {

    private final ArticleService articleService;

    // 1. API Tạo bài viết mới
    // POST /articles hoặc POST /api/v1/articles
    @Operation(summary = "Tạo bài viết mới (Status mặc định là DRAFT)")
    @PostMapping
    public ResponseEntity<ArticleDetailResponse> createArticle(@Valid @RequestBody CreateArticleRequest request) {
        ArticleDetailResponse response = articleService.createArticle(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2. API Xem danh sách bài viết của tác giả hiện tại
    // GET /articles/my hoặc GET /api/v1/articles/my
    @Operation(summary = "Xem danh sách bài viết của chính tác giả đang đăng nhập")
    @GetMapping("/my")
    public ResponseEntity<PageResponse<ArticleDetailResponse>> getMyArticles(
            @RequestParam(required = false) ArticleStatus status,
            @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<ArticleDetailResponse> response = articleService.getMyArticles(status, pageable);
        return ResponseEntity.ok(response);
    }

    // 3. API Xem chi tiết bài viết theo ID
    // GET /articles/{id} hoặc GET /api/v1/articles/{id}
    @Operation(summary = "Xem chi tiết bài viết theo ID")
    @GetMapping("/{id}")
    public ResponseEntity<ArticleDetailResponse> getArticleDetail(@PathVariable Long id) {
        ArticleDetailResponse response = articleService.getArticleDetail(id);
        return ResponseEntity.ok(response);
    }

    // 4. API Chỉnh sửa bài viết
    // PUT /articles/{id} hoặc PUT /api/v1/articles/{id}
    @Operation(summary = "Chỉnh sửa bài viết của chính mình (chỉ cho phép khi ở DRAFT hoặc CHANGES_REQUESTED)")
    @PutMapping("/{id}")
    public ResponseEntity<ArticleDetailResponse> updateArticle(
            @PathVariable Long id,
            @Valid @RequestBody UpdateArticleRequest request) {
        ArticleDetailResponse response = articleService.updateArticle(id, request);
        return ResponseEntity.ok(response);
    }

    // 5. API Gửi bài viết để xét duyệt
    // PUT /articles/{id}/submit hoặc PUT /api/v1/articles/{id}/submit
    @Operation(summary = "Gửi bài viết để xét duyệt (chuyển status từ DRAFT -> PENDING_REVIEW)")
    @PutMapping("/{id}/submit")
    public ResponseEntity<SubmitArticleResponse> submitArticle(@PathVariable Long id) {
        SubmitArticleResponse response = articleService.submitForReview(id);
        return ResponseEntity.ok(response);
    }
    @Operation(summary = "Get all tags (for the article form)")
    @GetMapping("/tags")
    public ResponseEntity<List<TagResponse>> getAllTags() {
        return ResponseEntity.ok(articleService.getAllTags());
    }
}

