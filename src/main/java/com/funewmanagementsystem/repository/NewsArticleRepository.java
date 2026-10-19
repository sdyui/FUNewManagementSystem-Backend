package com.funewmanagementsystem.repository;

import com.funewmanagementsystem.entity.NewsArticle;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsArticleRepository extends JpaRepository<NewsArticle,Long> {
    @Query("""
    select n from NewsArticle n 
    where (:title is null or lower(n.newsTitle) like lower(concat('%',':title','%')))
    and (:categoryId is null or n.category.id = :categoryId)
    and (:status is null or n.newsStatus = :status)
""")
    Page<NewsArticle> search(@Param("title") String title,
                             @Param("categoryId") Long categoryId,
                             @Param("status") Boolean status,
                             Pageable pageable);
}
