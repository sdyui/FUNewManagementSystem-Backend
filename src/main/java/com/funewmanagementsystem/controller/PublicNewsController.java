package com.funewmanagementsystem.controller;

import com.funewmanagementsystem.dto.respone.PageResponse;
import com.funewmanagementsystem.dto.respone.PublicNewsResponse;
import com.funewmanagementsystem.service.PublicNewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/public/news")
@RequiredArgsConstructor
@Tag(name = "Public News", description = "Published articles for readers (no login required)")
public class PublicNewsController {

    private final PublicNewsService publicNewsService;

    @Operation(summary = "Search published news")
    @GetMapping
    public ResponseEntity<PageResponse<PublicNewsResponse>> search(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long categoryId,
            @ParameterObject
            @PageableDefault(size = 9, sort = "publishDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(publicNewsService.search(title, categoryId, pageable));
    }

    @Operation(summary = "Get a published news article")
    @GetMapping("/{id}")
    public ResponseEntity<PublicNewsResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(publicNewsService.getById(id));
    }
}