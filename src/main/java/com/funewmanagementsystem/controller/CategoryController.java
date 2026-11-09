package com.funewmanagementsystem.controller;

import com.funewmanagementsystem.dto.request.CategoryRequest;
import com.funewmanagementsystem.dto.respone.CategoryResponse;
import com.funewmanagementsystem.dto.respone.PageResponse;
import com.funewmanagementsystem.service.CategoryService;
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

// CORS được cấu hình tập trung trong config/CorsConfig.java nên không cần @CrossOrigin ở đây
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Category Management", description = "APIs for managing news categories")
public class CategoryController {

    private final CategoryService categoryService;

    // 1. Search and paginate categories
    @Operation(summary = "Search and paginate categories")
    @GetMapping
    public ResponseEntity<PageResponse<CategoryResponse>> searchCategories(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean isActive,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        PageResponse<CategoryResponse> result = categoryService.searchCategories(name, isActive, pageable);
        return ResponseEntity.ok(result);
    }

    // 2. Get active categories for dropdown
    @Operation(summary = "Get all active categories for dropdown")
    @GetMapping("/active")
    public ResponseEntity<List<CategoryResponse>> getAllActiveCategories() {
        return ResponseEntity.ok(categoryService.getAllActiveCategories());
    }

    // 3. Get category details by ID
    @Operation(summary = "Get category details by ID")
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    // 4. Create new category
    @Operation(summary = "Create a new category")
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Create multiple categories at once")
    @PostMapping("/bulk")
    public ResponseEntity<List<CategoryResponse>> createCategories(@Valid @RequestBody List<CategoryRequest> requests) {
        List<CategoryResponse> responses = categoryService.createCategoriesBulk(requests);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    // 5. Update category
    @Operation(summary = "Update an existing category")
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }

    // 6. Delete category
    @Operation(summary = "Delete a category by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
