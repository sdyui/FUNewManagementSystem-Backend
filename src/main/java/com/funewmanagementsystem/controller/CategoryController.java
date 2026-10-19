package com.funewmanagementsystem.controller;

import com.funewmanagementsystem.dto.request.CategoryRequest;
import com.funewmanagementsystem.dto.respone.CategoryResponse;
import com.funewmanagementsystem.dto.respone.PageResponse;
import com.funewmanagementsystem.exception.ErrorResponse;
import com.funewmanagementsystem.service.CategoryService;
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
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Category Management", description = "APIs for managing news categories")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Search categories",
            description = "Returns a paginated list of categories, optionally filtered by name and isActive. "
                    + "Sort with sort=field,asc|desc; allowed fields: id, categoryName, isActive.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of categories",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid sort field",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping({"/category", "/categories"})
    public ResponseEntity<PageResponse<CategoryResponse>> searchCategories(
            @Parameter(description = "Part of category name", example = "Tin tức")
            @RequestParam(required = false) String name,
            @Parameter(description = "Filter by active status (true/false)")
            @RequestParam(required = false) Boolean isActive,
            @ParameterObject
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(categoryService.searchCategories(name, isActive, pageable));
    }

    @Operation(summary = "Get all active categories",
            description = "Returns all active categories as a list (non-paginated) for dropdown selection in UI.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of active categories")
    })
    @GetMapping({"/category/active", "/categories/active"})
    public ResponseEntity<List<CategoryResponse>> getAllActiveCategories() {
        return ResponseEntity.ok(categoryService.getAllActiveCategories());
    }

    @Operation(summary = "Get category by id", description = "Returns the category with the given id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category found",
                    content = @Content(schema = @Schema(implementation = CategoryResponse.class))),
            @ApiResponse(responseCode = "404", description = "Category not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping({"/category/{id}", "/categories/{id}"})
    public ResponseEntity<CategoryResponse> getCategoryById(
            @Parameter(description = "Id of the category to retrieve", example = "1", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @Operation(summary = "Create a new category", description = "Creates a new category. Category name must be unique.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Category created successfully",
                    content = @Content(schema = @Schema(implementation = CategoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Parent category not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Category name already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping({"/category", "/categories"})
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Update a category", description = "Updates an existing category by id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category updated successfully",
                    content = @Content(schema = @Schema(implementation = CategoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed or category selected as its own parent",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Category or parent category not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Category name already in use by another category",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping({"/category/{id}", "/categories/{id}"})
    public ResponseEntity<CategoryResponse> updateCategory(
            @Parameter(description = "Id of the category to update", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }

    @Operation(summary = "Delete a category",
            description = "Deletes a category by id. Fails with 400 if it has child categories, or 409 if it is used by news articles.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Cannot delete category with child categories",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Category not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Category is in use by news articles",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping({"/category/{id}", "/categories/{id}"})
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "Id of the category to delete", example = "1", required = true)
            @PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
