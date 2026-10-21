package com.funewmanagementsystem.repository;

import com.funewmanagementsystem.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByCategoryNameIgnoreCase(String categoryName);

    boolean existsByCategoryNameIgnoreCaseAndIdNot(String categoryName, Long id);

    boolean existsByParentCategory_Id(Long parentCategoryId);

    List<Category> findByIsActiveTrue();

    @Query(value = """
        SELECT c FROM Category c
        LEFT JOIN FETCH c.parentCategory
        WHERE (:name IS NULL OR LOWER(c.categoryName) LIKE LOWER(CONCAT('%', :name, '%')))
          AND (:isActive IS NULL OR c.isActive = :isActive)
        """,
            countQuery = """
        SELECT COUNT(c) FROM Category c
        WHERE (:name IS NULL OR LOWER(c.categoryName) LIKE LOWER(CONCAT('%', :name, '%')))
          AND (:isActive IS NULL OR c.isActive = :isActive)
        """)
    Page<Category> search(@Param("name") String name,
                          @Param("isActive") Boolean isActive,
                          Pageable pageable);
}
