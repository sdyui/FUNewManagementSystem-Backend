package com.funewmanagementsystem.service.impl;

import com.funewmanagementsystem.dto.request.CreateArticleRequest;
import com.funewmanagementsystem.dto.request.UpdateArticleRequest;
import com.funewmanagementsystem.dto.respone.ArticleDetailResponse;
import com.funewmanagementsystem.dto.respone.PageResponse;
import com.funewmanagementsystem.dto.respone.SubmitArticleResponse;
import com.funewmanagementsystem.dto.respone.TagResponse;
import com.funewmanagementsystem.entity.*;
import com.funewmanagementsystem.exception.ForbiddenException;
import com.funewmanagementsystem.exception.InvalidRequestException;
import com.funewmanagementsystem.exception.ResourceNotFoundException;
import com.funewmanagementsystem.repository.ArticleWorkflowLogRepository;
import com.funewmanagementsystem.repository.CategoryRepository;
import com.funewmanagementsystem.repository.NewsArticleRepository;
import com.funewmanagementsystem.repository.TagRepository;
import com.funewmanagementsystem.security.CurrentUserService;
import com.funewmanagementsystem.service.ArticleService;
import com.funewmanagementsystem.service.ArticleWorkFlowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final NewsArticleRepository newsArticleRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final CurrentUserService currentUserService;
    private final ArticleWorkFlowService articleWorkFlowService;
    private final ArticleWorkflowLogRepository articleWorkflowLogRepository;
    /**
     * Chức năng 1: Tạo bài viết mới dành cho Contributor
     * Quy tắc 1 & 2: Trạng thái ban đầu luôn là DRAFT, tác giả là tài khoản đang đăng nhập.
     */
    @Transactional
    @Override
    public ArticleDetailResponse createArticle(CreateArticleRequest request) {
        SystemAccount currentAuthor = currentUserService.getCurrentAccount();

        NewsArticle article = new NewsArticle();
        article.setNewsTitle(request.getTitle());
        article.setNewsContent(request.getContent());
        article.setThumbnail(request.getThumbnail());

        // Mặc định bài viết mới tạo có status = DRAFT
        article.setArticleStatus(ArticleStatus.DRAFT);
        article.setNewsStatus(false);

        article.setCreatedBy(currentAuthor);
        article.setCreatedDate(LocalDateTime.now());

        // Xử lý danh mục (Category) bằng if-else và vòng lặp for cơ bản
        if (request.getCategoryId() != null) {
            Optional<Category> categoryOpt = categoryRepository.findById(request.getCategoryId());
            if (categoryOpt.isEmpty()) {
                throw new InvalidRequestException("Không tìm thấy danh mục với id: " + request.getCategoryId());
            }
            article.setCategory(categoryOpt.get());
        } else {
            List<Category> allCategories = categoryRepository.findAll();
            Category defaultCategory = null;
            for (Category c : allCategories) {
                if (Boolean.TRUE.equals(c.getIsActive())) {
                    defaultCategory = c;
                    break;
                }
            }
            article.setCategory(defaultCategory);
        }

        // Xử lý danh sách thẻ (Tags) nếu có
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            List<Tag> foundTags = tagRepository.findAllById(request.getTagIds());
            Set<Tag> tagSet = new HashSet<>();
            for (Tag tag : foundTags) {
                tagSet.add(tag);
            }
            article.setTags(tagSet);
        }

        NewsArticle savedArticle = newsArticleRepository.save(article);
        return mapToDetailResponse(savedArticle);
    }

    /**
     * Chức năng 2: Chỉnh sửa bài viết
     * Quy tắc 3: Chỉ được sửa bài viết của chính mình.
     * Quy tắc 4: Chỉ được sửa khi bài viết ở trạng thái DRAFT hoặc CHANGES_REQUESTED.
     */
    @Transactional
    @Override
    public ArticleDetailResponse updateArticle(Long id, UpdateArticleRequest request) {
        SystemAccount currentAuthor = currentUserService.getCurrentAccount();

        Optional<NewsArticle> articleOpt = newsArticleRepository.findById(id);
        if (articleOpt.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy bài viết với id: " + id);
        }
        NewsArticle article = articleOpt.get();

        // Kiểm tra quyền tác giả (Quy tắc 3)
        if (!article.getCreatedBy().getId().equals(currentAuthor.getId())) {
            throw new ForbiddenException("Bạn không có quyền chỉnh sửa bài viết của người khác!");
        }

        // Kiểm tra trạng thái bài viết (Quy tắc 4)
        ArticleStatus currentStatus = article.getArticleStatus();
        if (currentStatus != ArticleStatus.DRAFT && currentStatus != ArticleStatus.CHANGES_REQUESTED) {
            throw new InvalidRequestException("Chỉ được chỉnh sửa bài viết ở trạng thái DRAFT hoặc CHANGES_REQUESTED. Trạng thái hiện tại: " + currentStatus);
        }

        // Cập nhật các trường thông tin
        article.setNewsTitle(request.getTitle());
        article.setNewsContent(request.getContent());
        article.setThumbnail(request.getThumbnail());
        article.setModifiedDate(LocalDateTime.now());
        article.setUpdatedBy(currentAuthor);

        if (request.getCategoryId() != null) {
            Optional<Category> categoryOpt = categoryRepository.findById(request.getCategoryId());
            if (categoryOpt.isEmpty()) {
                throw new InvalidRequestException("Không tìm thấy danh mục với id: " + request.getCategoryId());
            }
            article.setCategory(categoryOpt.get());
        }

        if (request.getTagIds() != null) {
            List<Tag> foundTags = tagRepository.findAllById(request.getTagIds());
            Set<Tag> tagSet = new HashSet<>();
            for (Tag tag : foundTags) {
                tagSet.add(tag);
            }
            article.setTags(tagSet);
        }

        NewsArticle updatedArticle = newsArticleRepository.save(article);
        return mapToDetailResponse(updatedArticle);
    }

    /**
     * Chức năng 3: Xem chi tiết bài viết
     * Quy tắc 5: Contributor có thể xem chi tiết bài viết của mình.
     */
    @Override
    public ArticleDetailResponse getArticleDetail(Long id) {
        SystemAccount currentAuthor = currentUserService.getCurrentAccount();

        Optional<NewsArticle> articleOpt = newsArticleRepository.findById(id);
        if (articleOpt.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy bài viết với id: " + id);
        }
        NewsArticle article = articleOpt.get();

        boolean isAuthor = article.getCreatedBy().getId().equals(currentAuthor.getId());
        boolean isStaffOrAdmin = currentAuthor.getAccountRole() == AccountRole.ADMIN
                || currentAuthor.getAccountRole() == AccountRole.EDITOR
                || currentAuthor.getAccountRole() == AccountRole.STAFF;

        if (!isAuthor && !isStaffOrAdmin) {
            throw new ForbiddenException("Bạn chỉ có quyền xem chi tiết bài viết của chính mình!");
        }

        return mapToDetailResponse(article);
    }

    /**
     * Chức năng 4: Lấy danh sách bài viết của Contributor hiện tại
     */
    @Override
    public PageResponse<ArticleDetailResponse> getMyArticles(ArticleStatus status, Pageable pageable) {
        SystemAccount currentAuthor = currentUserService.getCurrentAccount();

        Page<NewsArticle> pageResult = newsArticleRepository.findMyArticles(currentAuthor.getId(), status, pageable);

        // Dùng vòng lặp for truyền thống để chuyển đổi danh sách Entity sang DTO
        List<ArticleDetailResponse> dtoList = new ArrayList<>();
        for (NewsArticle article : pageResult.getContent()) {
            ArticleDetailResponse dto = mapToDetailResponse(article);
            dtoList.add(dto);
        }

        return new PageResponse<ArticleDetailResponse>(
                dtoList,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isLast()
        );
    }

    /**
     * Chức năng 5: Gửi bài viết để xét duyệt (Submit For Review)
     * Quy tắc 6, 7 & 8: DRAFT -> PENDING_REVIEW, kiểm tra tiêu đề và nội dung không được rỗng.
     */
    @Transactional
    @Override
    public SubmitArticleResponse submitForReview(Long id) {
        SystemAccount currentAuthor = currentUserService.getCurrentAccount();

        Optional<NewsArticle> articleOpt = newsArticleRepository.findById(id);
        if (articleOpt.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy bài viết với id: " + id);
        }
        NewsArticle article = articleOpt.get();

        if (!article.getCreatedBy().getId().equals(currentAuthor.getId())) {
            throw new ForbiddenException("You can only submit your own articles for review");
        }

        if (article.getNewsTitle() == null || article.getNewsTitle().trim().isEmpty()) {
            throw new InvalidRequestException("Cannot submit for review: title must not be empty");
        }
        if (article.getNewsContent() == null || article.getNewsContent().trim().isEmpty()) {
            throw new InvalidRequestException("Cannot submit for review: content must not be empty");
        }

        ArticleStatus currentStatus = article.getArticleStatus();
        if (currentStatus != ArticleStatus.DRAFT && currentStatus != ArticleStatus.CHANGES_REQUESTED) {
            throw new InvalidRequestException("Only DRAFT or CHANGES_REQUESTED articles can be submitted for review. Current status: " + currentStatus);
        }

        NewsArticle transitionedArticle = articleWorkFlowService.transition(
                article,
                ArticleStatus.PENDING_REVIEW,
                currentAuthor,
                "Submitted for review by author"
        );

        return new SubmitArticleResponse(
                transitionedArticle.getId(),
                transitionedArticle.getArticleStatus(),
                "Article submitted for review successfully"
        );
    }

   
    private ArticleDetailResponse mapToDetailResponse(NewsArticle article) {
        ArticleDetailResponse response = new ArticleDetailResponse();
        response.setId(article.getId());
        response.setTitle(article.getNewsTitle());
        response.setThumbnail(article.getThumbnail());
        response.setContent(article.getNewsContent());
        response.setStatus(article.getArticleStatus());
        response.setCreatedAt(article.getCreatedDate());
        response.setUpdatedAt(article.getModifiedDate());

        if (article.getCategory() != null) {
            response.setCategoryId(article.getCategory().getId());
            response.setCategoryName(article.getCategory().getCategoryName());
        }

        List<Long> tagIds = new ArrayList<>();
        List<String> tagNames = new ArrayList<>();
        if (article.getTags() != null) {
            for (Tag tag : article.getTags()) {
                tagIds.add(tag.getId());
                tagNames.add(tag.getTagName());
            }
        }
        response.setTagIds(tagIds);
        response.setTagNames(tagNames);

        if (article.getCreatedBy() != null) {
            ArticleDetailResponse.AuthorDto authorDto = new ArticleDetailResponse.AuthorDto();
            authorDto.setId(article.getCreatedBy().getId());
            authorDto.setName(article.getCreatedBy().getAccountName());
                 response.setAuthor(authorDto);
        }

        return response;
    }
    private String findLatestFeedback(NewsArticle article) {
        ArticleStatus status = article.getArticleStatus();
        if (status != ArticleStatus.CHANGES_REQUESTED && status != ArticleStatus.ARCHIVED) {
            return null;
        }
        return articleWorkflowLogRepository
                .findFirstByArticleIdAndToStatusInAndNoteIsNotNullOrderByActionAtDescIdDesc(
                        article.getId(), List.of(ArticleStatus.CHANGES_REQUESTED, ArticleStatus.ARCHIVED))
                .map(ArticleWorkflowLog::getNote)
                .orElse(null);
    }
}
