package com.funewmanagementsystem.service.impl;

import com.funewmanagementsystem.entity.ArticleStatus;
import com.funewmanagementsystem.entity.ArticleWorkflowLog;
import com.funewmanagementsystem.entity.NewsArticle;
import com.funewmanagementsystem.entity.SystemAccount;
import com.funewmanagementsystem.exception.InvalidRequestException;
import com.funewmanagementsystem.repository.ArticleWorkflowLogRepository;
import com.funewmanagementsystem.repository.NewsArticleRepository;
import com.funewmanagementsystem.service.ArticleWorkFlowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import static com.funewmanagementsystem.entity.ArticleStatus.*;

@Service
@RequiredArgsConstructor
public class ArticleWorkFlowServiceImpl implements ArticleWorkFlowService {
    private static final Map<ArticleStatus, Set<ArticleStatus>> ALLOWED = Map.of(
            DRAFT,             Set.of(PENDING_REVIEW, ARCHIVED),
            PENDING_REVIEW,    Set.of(CHANGES_REQUESTED, SCHEDULED, PUBLISHED, ARCHIVED),
            CHANGES_REQUESTED, Set.of(PENDING_REVIEW, ARCHIVED),
            SCHEDULED,         Set.of(PUBLISHED, CHANGES_REQUESTED, ARCHIVED),
            PUBLISHED,         Set.of(ARCHIVED),
            ARCHIVED,          Set.of(DRAFT));

    private final NewsArticleRepository newsArticleRepository;
    private final ArticleWorkflowLogRepository articleWorkflowLogRepository;
        @Transactional
        @Override
        public NewsArticle transition(NewsArticle newsArticle, ArticleStatus articleStatus, SystemAccount systemAccount, String note) {
        ArticleStatus from = newsArticle.getArticleStatus();
        boolean allowed = (from == null)? articleStatus == DRAFT : ALLOWED.getOrDefault(from, Set.of()).contains(articleStatus);
        if(!allowed) {
            throw new InvalidRequestException("Can not change article status from "+from+" to "+articleStatus);
        }
        LocalDateTime now = LocalDateTime.now();
        newsArticle.setArticleStatus(articleStatus);
        newsArticle.setNewsStatus(articleStatus == PUBLISHED);
        if( from != null ){
            newsArticle.setModifiedDate(now);
            if(systemAccount != null) {
                newsArticle.setUpdatedBy(systemAccount);
            }

        }
        if (articleStatus == PUBLISHED && (newsArticle.getPublishDate() == null || newsArticle.getPublishDate().isAfter(now))) {
            newsArticle.setPublishDate(now);
        }
        NewsArticle saved = newsArticleRepository.save(newsArticle);

        ArticleWorkflowLog log = new ArticleWorkflowLog();
        log.setArticle(saved);
        log.setFromStatus(from);
        log.setToStatus(articleStatus);
        log.setActor(systemAccount);
        log.setNote(note == null || note.isBlank() ? null : note.trim());
        log.setActionAt(now);
        articleWorkflowLogRepository.save(log);
        return saved;
    }
}
