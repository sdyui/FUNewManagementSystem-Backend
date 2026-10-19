package com.funewmanagementsystem.service;

import com.funewmanagementsystem.dto.request.CategoryRequest;
import com.funewmanagementsystem.dto.respone.CategoryResponse;
import com.funewmanagementsystem.dto.respone.PageResponse;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface CategoryService {
    CategoryResponse createCategory(CategoryRequest request);
    CategoryResponse updateCategory(Long id, CategoryRequest request);
    CategoryResponse getCategoryById(Long id);
    PageResponse<CategoryResponse> searchCategories(String name, Boolean isActive, Pageable pageable);
    List<CategoryResponse> getAllActiveCategories();
    void deleteCategory(Long id);
}
