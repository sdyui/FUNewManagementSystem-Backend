package com.funewmanagementsystem.repository;

import com.funewmanagementsystem.entity.ArticleStatus;
import com.funewmanagementsystem.entity.ArticleWorkflowLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleWorkflowLogRepository extends JpaRepository<ArticleWorkflowLog, Long> {
    List<ArticleWorkflowLog> findByArticleIdOrderByActionAtAscIdAsc(Long articleId);

    Optional<ArticleWorkflowLog> findFirstByArticleIdAndToStatusInAndNoteIsNotNullOrderByActionAtDescIdDesc(
            Long articleId, List<ArticleStatus> statuses);
}