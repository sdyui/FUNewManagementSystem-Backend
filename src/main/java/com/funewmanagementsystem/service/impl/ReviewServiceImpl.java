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
import com.funewmanagementsystem.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private static final Set<String> SORT_FIELDS = Set.of("modifiedDate", "createdDate", "newsTitle");
    private final NewsArticleRepository newsArticleRepository;
    private final ArticleWorkflowLogRepository logRepository;
    private final CurrentUserService currentUserService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ArticleDetailResponse> getPendingQueue(Pageable pageable) {
        requireReviewer();
        for (Sort.Order order : pageable.getSort()) {
            if (!SORT_FIELDS.contains(order.getProperty())) {
                throw new InvalidRequestException("Không thể sort theo '" + order.getProperty() + "'. Cho phép: " + SORT_FIELDS);
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




    private SystemAccount requireReviewer() {
        SystemAccount me = currentUserService.getCurrentAccount();
        if (!currentUserService.isReviewer(me)) {
            throw new ForbiddenException("Chỉ EDITOR hoặc ADMIN mới được duyệt bài");
        }
        return me;
    }


    private NewsArticle findReviewable(Long id) {
        NewsArticle article = newsArticleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài viết với id: " + id));
        if (article.getArticleStatus() == ArticleStatus.DRAFT) {
            throw new ResourceNotFoundException("Không tìm thấy bài viết với id: " + id);
        }
        return article;
    }


    private String findLatestFeedback(NewsArticle article) {
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
}