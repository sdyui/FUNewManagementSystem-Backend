package com.funewmanagementsystem.controller;

import com.funewmanagementsystem.dto.request.UpdateNewsArticleRequest;
import com.funewmanagementsystem.dto.respone.NewsArticleResponse;
import com.funewmanagementsystem.dto.respone.PageResponse;
import com.funewmanagementsystem.dto.respone.TagResponse;
import com.funewmanagementsystem.exception.ErrorResponse;
import com.funewmanagementsystem.repository.TagRepository;
import com.funewmanagementsystem.service.NewsArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/news", "/api/v1/news-articles"})
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "News Article Management", description = "APIs for managing news articles")
public class NewsArticleController {

    private final NewsArticleService newsArticleService;
    private final TagRepository tagRepository;

    // 1. Search and paginate news articles
    @Operation(summary = "Search and paginate news articles")
    @GetMapping
    public ResponseEntity<PageResponse<NewsArticleResponse>> searchNews(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean status,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<NewsArticleResponse> result = newsArticleService.search(title, categoryId, status, pageable);
        return ResponseEntity.ok(result);
    }

    // 2. Get news article details by ID
    @Operation(summary = "Get news article details by ID")
    @GetMapping("/{id}")
    public ResponseEntity<NewsArticleResponse> getNewsById(@PathVariable Long id) {
        NewsArticleResponse result = newsArticleService.getNewsById(id);
        return ResponseEntity.ok(result);
    }

    // 3. Create a new news article
    @Operation(summary = "Create a new news article")
    @PostMapping
    public ResponseEntity<NewsArticleResponse> createNews(@Valid @RequestBody UpdateNewsArticleRequest request) {
        NewsArticleResponse created = newsArticleService.createNewsArticle(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // 4. Update news article
    @Operation(summary = "Update news article")
    @PutMapping("/{id}")
    public ResponseEntity<NewsArticleResponse> updateNews(
            @PathVariable Long id,
            @Valid @RequestBody UpdateNewsArticleRequest request) {
        NewsArticleResponse updated = newsArticleService.updateNewsArticle(request, id);
        return ResponseEntity.ok(updated);
    }

    // 5. Delete news article
    @Operation(summary = "Delete news article")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNews(@PathVariable Long id) {
        newsArticleService.deleteNewsArticle(id);
        return ResponseEntity.noContent().build();
    }

    // 6. Get all tags for dropdown selection
    @Operation(summary = "Get all tags")
    @GetMapping("/tags")
    public ResponseEntity<List<TagResponse>> getAllTags() {
        List<TagResponse> tags = tagRepository.findAll()
                .stream()
                .map(t -> new TagResponse(t.getId(), t.getTagName(), t.getNote()))
                .toList();
        return ResponseEntity.ok(tags);
    }
}
