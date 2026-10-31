package com.funewmanagementsystem.repository;

import com.funewmanagementsystem.entity.ArticleStatus;
import com.funewmanagementsystem.entity.NewsArticle;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NewsArticleRepository extends JpaRepository<NewsArticle,Long> {
    @Query("""
    select n from NewsArticle n 
    where (:title is null or lower(n.newsTitle) like lower(concat('%', :title, '%')))
    and (:categoryId is null or n.category.id = :categoryId)
    and (:status is null or n.newsStatus = :status)
""")
    Page<NewsArticle> search(@Param("title") String title,
                             @Param("categoryId") Long categoryId,
                             @Param("status") Boolean status,
                             Pageable pageable);

    @Query("""
        SELECT a FROM NewsArticle a 
        WHERE a.createdBy.id = :authorId 
        AND (:status IS NULL OR a.articleStatus = :status)
        ORDER BY a.createdDate DESC
    """)
    Page<NewsArticle> findMyArticles(
            @Param("authorId") Long authorId,
            @Param("status") ArticleStatus status,
            Pageable pageable
    );

    Page<NewsArticle> findByArticleStatus(ArticleStatus status, Pageable pageable);

    // Scheduler: các bài SCHEDULED đã đến giờ đăng
    List<NewsArticle> findByArticleStatusAndPublishDateLessThanEqual(ArticleStatus status, LocalDateTime time);
}
