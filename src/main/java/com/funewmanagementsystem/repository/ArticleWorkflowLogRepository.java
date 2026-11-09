package com.funewmanagementsystem.repository;

import com.funewmanagementsystem.entity.ArticleStatus;
import com.funewmanagementsystem.entity.ArticleWorkflowLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleWorkflowLogRepository extends JpaRepository<ArticleWorkflowLog, Long> {
    List<ArticleWorkflowLog> findByArticleIdOrderByActionAtAscIdAsc(Long articleId);

    Optional<ArticleWorkflowLog> findFirstByArticleIdAndToStatusInAndNoteIsNotNullOrderByActionAtDescIdDesc(
            Long articleId, List<ArticleStatus> statuses);

    @Query("""
        SELECT l FROM ArticleWorkflowLog l
        LEFT JOIN l.actor a
        WHERE (:toStatus IS NULL OR l.toStatus = :toStatus)
        AND (:actor IS NULL OR LOWER(a.accountName) LIKE LOWER(CONCAT('%', :actor, '%')))
        AND (:keyword IS NULL OR LOWER(l.article.newsTitle) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:fromTime IS NULL OR l.actionAt >= :fromTime)
        AND (:toTime IS NULL OR l.actionAt < :toTime)
    """)
    Page<ArticleWorkflowLog> search(@Param("toStatus") ArticleStatus toStatus,
                                    @Param("actor") String actor,
                                    @Param("keyword") String keyword,
                                    @Param("fromTime") LocalDateTime fromTime,
                                    @Param("toTime") LocalDateTime toTime,
                                    Pageable pageable);
}