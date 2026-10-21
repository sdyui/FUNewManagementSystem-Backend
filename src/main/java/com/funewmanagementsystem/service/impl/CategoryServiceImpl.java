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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    private static final Set<String> SORTABLE_FIELDS = Set.of("id", "categoryName", "isActive");

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request){
        if(categoryRepository.existsByCategoryNameIgnoreCase(request.getCategoryName().trim())){
            throw new DuplicateResourceException("Category already exists");
        }


        Category category = new Category();
        category.setCategoryName(request.getCategoryName().trim());
        category.setCategoryDescription(request.getCategoryDescription());
        category.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        if (request.getParentCategoryId() != null) {
            Category parent = categoryRepository.findById(request.getParentCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with id: " + request.getParentCategoryId()));
            category.setParentCategory(parent);
        }

        Category saved = categoryRepository.save(category);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request){
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        // Check if category name already exists for another category
        if (categoryRepository.existsByCategoryNameIgnoreCaseAndIdNot(request.getCategoryName().trim(), id)) {
            throw new DuplicateResourceException("Category name '" + request.getCategoryName() + "' is already in use!");
        }

        if (request.getParentCategoryId() != null) {
            if (request.getParentCategoryId().equals(id)) {
                throw new InvalidRequestException("A category cannot be its own parent");
            }
            Category parent = categoryRepository.findById(request.getParentCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with id: " + request.getParentCategoryId()));
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
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return mapToResponse(category);
    }


    @Override
    @Transactional(readOnly = true)
    public PageResponse<CategoryResponse> searchCategories(String name, Boolean isActive, Pageable pageable) {
        for (Sort.Order order : pageable.getSort()) {
            if (!SORTABLE_FIELDS.contains(order.getProperty())) {
                throw new InvalidRequestException("Cannot sort by '" + order.getProperty() + "'. Allowed fields: " + SORTABLE_FIELDS);
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
        List<Category> categories = categoryRepository.findByIsActiveTrue();

        List<CategoryResponse> responses = new ArrayList<>();

        for (Category category : categories) {
            CategoryResponse response = mapToResponse(category);
            responses.add(response);
        }

        return responses;
    }


    @Override
    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }


        if (categoryRepository.existsByParentCategory_Id(id)) {
            throw new InvalidRequestException("Cannot delete this category because it has sub-categories!");
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
