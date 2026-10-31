package com.funewmanagementsystem.scheduler;

import com.funewmanagementsystem.entity.ArticleStatus;
import com.funewmanagementsystem.entity.NewsArticle;
import com.funewmanagementsystem.repository.NewsArticleRepository;
import com.funewmanagementsystem.service.ArticleWorkFlowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Tự động đăng bài đã lên lịch (FR-ED-003 / AC-ED-003).
 * Mỗi lần chạy: tìm các bài SCHEDULED có publishDate <= bây giờ rồi chuyển sang PUBLISHED.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ArticlePublishScheduler {

    private final NewsArticleRepository newsArticleRepository;
    private final ArticleWorkFlowService articleWorkFlowService;

    // Chạy lại sau mỗi 60 giây (tính từ khi lần trước chạy xong), có thể đổi bằng workflow.publish-check-ms
    @Scheduled(fixedDelayString = "${workflow.publish-check-ms:60000}")
    public void publishDueArticles() {
        List<NewsArticle> dueArticles = newsArticleRepository
                .findByArticleStatusAndPublishDateLessThanEqual(ArticleStatus.SCHEDULED, LocalDateTime.now());

        for (NewsArticle article : dueArticles) {
            try {
                // actor = null -> audit log ghi là hệ thống tự đăng
                articleWorkFlowService.transition(article, ArticleStatus.PUBLISHED, null, "Tự động đăng theo lịch");
                log.info("Auto-published article {}", article.getId());
            } catch (Exception e) {
                // Mỗi bài một transaction riêng: 1 bài lỗi (vd: editor vừa gỡ bài) không chặn các bài khác
                log.error("Could not auto-publish article {}: {}", article.getId(), e.getMessage());
            }
        }
    }
}
