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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Category Management", description = "Các API quản lý chuyên mục tin tức")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Tạo mới chuyên mục", description = "Tạo một chuyên mục tin tức mới. Tên chuyên mục không được trùng.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tạo thành công",
                    content = @Content(schema = @Schema(implementation = CategoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Tên chuyên mục đã tồn tại",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Tìm kiếm & Phân trang chuyên mục", description = "Lấy danh sách chuyên mục có phân trang, hỗ trợ tìm theo tên và lọc theo trạng thái isActive.")
    @GetMapping
    public ResponseEntity<PageResponse<CategoryResponse>> searchCategories(
            @Parameter(description = "Tìm kiếm theo tên chuyên mục", example = "Công nghệ")
            @RequestParam(required = false) String name,
            @Parameter(description = "Lọc theo trạng thái hoạt động (true/false)")
            @RequestParam(required = false) Boolean isActive,
            @ParameterObject
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {

        return ResponseEntity.ok(categoryService.searchCategories(name, isActive, pageable));
    }

    @Operation(summary = "Lấy danh sách tất cả chuyên mục đang hoạt động", description = "Dùng cho dropdown lựa chọn danh mục cha hoặc chọn danh mục khi đăng bài viết (không phân trang).")
    @GetMapping("/active")
    public ResponseEntity<List<CategoryResponse>> getAllActiveCategories() {
        return ResponseEntity.ok(categoryService.getAllActiveCategories());
    }

    @Operation(summary = "Lấy chi tiết chuyên mục theo ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tìm thấy chuyên mục"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy chuyên mục")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @Operation(summary = "Cập nhật chuyên mục", description = "Cập nhật thông tin chuyên mục theo ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc chọn chính nó làm cha"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy chuyên mục"),
            @ApiResponse(responseCode = "409", description = "Tên chuyên mục bị trùng với chuyên mục khác")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }

    @Operation(summary = "Xóa chuyên mục", description = "Xóa chuyên mục theo ID. Sẽ thất bại nếu đang có chuyên mục con hoặc đang có bài viết tham chiếu tới.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Xóa thành công"),
            @ApiResponse(responseCode = "400", description = "Chuyên mục đang có chuyên mục con"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy chuyên mục"),
            @ApiResponse(responseCode = "409", description = "Chuyên mục đang được sử dụng trong bài viết")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
