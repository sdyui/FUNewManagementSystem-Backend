package com.funewmanagementsystem.service.impl;

import com.funewmanagementsystem.dto.respone.ArticleDetailResponse;
import com.funewmanagementsystem.dto.respone.PageResponse;
import com.funewmanagementsystem.dto.respone.WorkflowLogResponse;
import com.funewmanagementsystem.entity.ArticleStatus;
import com.funewmanagementsystem.entity.ArticleWorkflowLog;
import com.funewmanagementsystem.entity.NewsArticle;
import com.funewmanagementsystem.entity.SystemAccount;
import com.funewmanagementsystem.exception.ForbiddenException;
import com.funewmanagementsystem.exception.InvalidRequestException;
import com.funewmanagementsystem.exception.ResourceNotFoundException;
import com.funewmanagementsystem.repository.ArticleWorkflowLogRepository;
import com.funewmanagementsystem.repository.NewsArticleRepository;
import com.funewmanagementsystem.security.CurrentUserService;
import com.funewmanagementsystem.service.ArticleWorkFlowService;
import com.funewmanagementsystem.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private static final Set<String> SORT_FIELDS = Set.of("modifiedDate", "createdDate", "newsTitle");
    private final NewsArticleRepository newsArticleRepository;
    private final ArticleWorkflowLogRepository logRepository;
    private final CurrentUserService currentUserService;
    private final ArticleWorkFlowService articleWorkFlowService;
    @Override
    @Transactional(readOnly = true)
    public PageResponse<ArticleDetailResponse> getPendingQueue(Pageable pageable) {
        requireReviewer();
        for (Sort.Order order : pageable.getSort()) {
            if (!SORT_FIELDS.contains(order.getProperty())) {
                throw new InvalidRequestException("Cannot sort by '" + order.getProperty() + "'. Allowed: " + SORT_FIELDS);
            }
        }
        return PageResponse.of(newsArticleRepository.findByArticleStatus(ArticleStatus.PENDING_REVIEW, pageable)
                .map(a -> mapToDetailResponse(a, null)));
    }


    @Override
    @Transactional(readOnly = true)
    public ArticleDetailResponse getArticle(Long id) {
        requireReviewer();
        NewsArticle article = findReviewable(id);
        return mapToDetailResponse(article, findLatestFeedback(article));
    }


    @Override
    @Transactional(readOnly = true)
    public List<WorkflowLogResponse> history(Long id) {
        requireReviewer();
        NewsArticle article = findReviewable(id);
        return logRepository.findByArticleIdOrderByActionAtAscIdAsc(article.getId()).stream()
                .map(l -> new WorkflowLogResponse(
                        l.getFromStatus(),
                        l.getToStatus(),
                        l.getActor() == null ? "SYSTEM" : l.getActor().getAccountName(),
                        l.getNote(),
                        l.getActionAt()))
                .toList();
    }

    @Override
    @Transactional
    public ArticleDetailResponse requestChanges(Long id, String note) {
        SystemAccount me = requireReviewer();
        NewsArticle article = findReviewable(id);
        requireStatus(article, ArticleStatus.PENDING_REVIEW,ArticleStatus.SCHEDULED);
        NewsArticle saved = articleWorkFlowService.transition(article,ArticleStatus.CHANGES_REQUESTED,me,requireNote(note));
        return mapToDetailResponse(saved,findLatestFeedback(saved));
    }

    @Override
    @Transactional
    public ArticleDetailResponse reject(Long id, String note) {
        SystemAccount me = requireReviewer();
        NewsArticle article = findReviewable(id);
        requireStatus(article, ArticleStatus.PENDING_REVIEW);
        NewsArticle saved = articleWorkFlowService.transition(article,ArticleStatus.ARCHIVED,me,requireNote(note));
        return mapToDetailResponse(saved,findLatestFeedback(saved));
    }

    @Override
    @Transactional
    public ArticleDetailResponse approve(Long id, LocalDateTime publishDate, String note) {
        SystemAccount me = requireReviewer();
        NewsArticle article = findReviewable(id);
        requireStatus(article, ArticleStatus.PENDING_REVIEW);
        NewsArticle saved;

        if (publishDate != null && publishDate.isAfter(LocalDateTime.now())) {
            article.setPublishDate(publishDate);
            saved = articleWorkFlowService.transition(article, ArticleStatus.SCHEDULED, me, note);
        } else {
            article.setPublishDate(null);
            saved = articleWorkFlowService.transition(article, ArticleStatus.PUBLISHED, me, note);
        }
        return mapToDetailResponse(saved,null);
    }

    @Override
    @Transactional
    public ArticleDetailResponse publishNow(Long id) {
        SystemAccount me = requireReviewer();
        NewsArticle article = findReviewable(id);
        requireStatus(article, ArticleStatus.SCHEDULED);
        article.setPublishDate(null);
        NewsArticle saved = articleWorkFlowService.transition(article, ArticleStatus.PUBLISHED,me,null);
        return mapToDetailResponse(saved,null);
    }

    @Override
    @Transactional
    public ArticleDetailResponse archive(Long id, String note) {
        SystemAccount me = requireReviewer();
        NewsArticle article = findReviewable(id);
        requireStatus(article, ArticleStatus.SCHEDULED,ArticleStatus.PUBLISHED);
        NewsArticle saved = articleWorkFlowService.transition(article,ArticleStatus.ARCHIVED,me,note);
        return mapToDetailResponse(saved,findLatestFeedback(saved));
    }


    private SystemAccount requireReviewer() {
        SystemAccount me = currentUserService.getCurrentAccount();
        if (!currentUserService.isReviewer(me)) {
            throw new ForbiddenException("Only EDITOR or ADMIN can review articles");
        }
        return me;
    }


    private NewsArticle findReviewable(Long id) {
        NewsArticle article = newsArticleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("News article not found with id: " + id));
        if (article.getArticleStatus() == ArticleStatus.DRAFT) {
            throw new ResourceNotFoundException("News article not found with id: " + id);
        }
        return article;
    }


    private String findLatestFeedback(NewsArticle article) {
        ArticleStatus status = article.getArticleStatus();
        if (status != ArticleStatus.CHANGES_REQUESTED && status != ArticleStatus.ARCHIVED) {
            return null;
        }
        return logRepository
                .findFirstByArticleIdAndToStatusInAndNoteIsNotNullOrderByActionAtDescIdDesc(
                        article.getId(), List.of(ArticleStatus.CHANGES_REQUESTED, ArticleStatus.ARCHIVED))
                .map(ArticleWorkflowLog::getNote)
                .orElse(null);
    }


    private ArticleDetailResponse mapToDetailResponse(NewsArticle article, String latestFeedback) {
        ArticleDetailResponse.AuthorDto authorDto = null;
        if (article.getCreatedBy() != null) {
            authorDto = new ArticleDetailResponse.AuthorDto(
                    article.getCreatedBy().getId(),
                    article.getCreatedBy().getAccountName(),
                    article.getCreatedBy().getAccountEmail()
            );
        }

        return ArticleDetailResponse.builder()
                .id(article.getId())
                .title(article.getNewsTitle())
                .thumbnail(article.getThumbnail())
                .content(article.getNewsContent())
                .status(article.getArticleStatus())
                .author(authorDto)
                .createdAt(article.getCreatedDate())
                .updatedAt(article.getModifiedDate())
                .publishDate(article.getPublishDate())
                .latestFeedback(latestFeedback)
                .build();
    }

    private void requireStatus(NewsArticle article, ArticleStatus... allowed) {
        for (ArticleStatus s : allowed) {
            if (article.getArticleStatus() == s) {
                return;
            }
        }
        throw new InvalidRequestException("This action is not allowed when the article is " + article.getArticleStatus());
    }

    private String requireNote(String note) {
        if (note == null || note.isBlank()) {
            throw new InvalidRequestException("Note is required");
        }
        return note;
    }
}