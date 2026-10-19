package com.funewmanagementsystem.service.impl;

import com.funewmanagementsystem.dto.request.CategoryRequest;
import com.funewmanagementsystem.dto.respone.CategoryResponse;
import com.funewmanagementsystem.dto.respone.PageResponse;
import com.funewmanagementsystem.entity.Category;
import com.funewmanagementsystem.exception.DuplicateResourceException;
import com.funewmanagementsystem.exception.InvalidRequestException;
import com.funewmanagementsystem.exception.ResourceNotFoundException;
import com.funewmanagementsystem.repository.CategoryRepository;
import com.funewmanagementsystem.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    private static final Set<String> SORTABLE_FIELDS = Set.of("id", "categoryName", "isActive");

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request){
        if(categoryRepository.existsByCategoryNameIgnoreCase(request.getCategoryName().trim())){
            throw new DuplicateResourceException("Chuyên mục '" + request.getCategoryName() + "' đã tồn tại");
        }

        Category category = new Category();
        category.setCategoryName(request.getCategoryName().trim());
        category.setCategoryDescription(request.getCategoryDescription());
        category.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        if (request.getParentCategoryId() != null) {
            Category parent = categoryRepository.findById(request.getParentCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục cha với id: " + request.getParentCategoryId()));
            category.setParentCategory(parent);
        }

        Category saved = categoryRepository.save(category);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request){
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyên mục với ID: " + id));

        // Kiểm tra trùng tên với chuyên mục khác khi update
        if (categoryRepository.existsByCategoryNameIgnoreCaseAndIdNot(request.getCategoryName().trim(), id)) {
            throw new DuplicateResourceException("Tên chuyên mục '" + request.getCategoryName() + "' đã được sử dụng!");
        }

        if (request.getParentCategoryId() != null) {
            if (request.getParentCategoryId().equals(id)) {
                throw new InvalidRequestException("Một chuyên mục không thể chọn chính nó làm danh mục cha");
            }
            Category parent = categoryRepository.findById(request.getParentCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục cha với id: " + request.getParentCategoryId()));
            category.setParentCategory(parent);
        } else {
            category.setParentCategory(null);
        }

        category.setCategoryName(request.getCategoryName().trim());
        category.setCategoryDescription(request.getCategoryDescription());
        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
        }

        Category updated = categoryRepository.save(category);
        return mapToResponse(updated);
    }


    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyên mục với ID: " + id));
        return mapToResponse(category);
    }


    @Override
    @Transactional(readOnly = true)
    public PageResponse<CategoryResponse> searchCategories(String name, Boolean isActive, Pageable pageable) {
        for (Sort.Order order : pageable.getSort()) {
            if (!SORTABLE_FIELDS.contains(order.getProperty())) {
                throw new InvalidRequestException("Không thể sắp xếp theo '" + order.getProperty() + "'. Các trường cho phép: " + SORTABLE_FIELDS);
            }
        }

        Page<CategoryResponse> result = categoryRepository
                .search(blankToNull(name), isActive, pageable)
                .map(this::mapToResponse);

        return PageResponse.of(result);
    }


    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllActiveCategories() {
        return categoryRepository.findByIsActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    @Override
    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy chuyên mục với ID: " + id);
        }


        if (categoryRepository.existsByParentCategory_Id(id)) {
            throw new InvalidRequestException("Không thể xóa chuyên mục này vì đang có các chuyên mục con trực thuộc!");
        }


        categoryRepository.deleteById(id);
        categoryRepository.flush();
    }

    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .categoryName(category.getCategoryName())
                .categoryDescription(category.getCategoryDescription())
                .parentCategoryId(category.getParentCategory() != null ? category.getParentCategory().getId() : null)
                .parentCategoryName(category.getParentCategory() != null ? category.getParentCategory().getCategoryName() : null)
                .isActive(category.getIsActive())
                .build();
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
