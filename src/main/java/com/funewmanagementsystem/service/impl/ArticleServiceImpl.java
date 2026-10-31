package com.funewmanagementsystem.service.impl;

import com.funewmanagementsystem.dto.request.CreateArticleRequest;
import com.funewmanagementsystem.dto.request.UpdateArticleRequest;
import com.funewmanagementsystem.dto.respone.ArticleDetailResponse;
import com.funewmanagementsystem.dto.respone.PageResponse;
import com.funewmanagementsystem.dto.respone.SubmitArticleResponse;
import com.funewmanagementsystem.entity.AccountRole;
import com.funewmanagementsystem.entity.ArticleStatus;
import com.funewmanagementsystem.entity.Category;
import com.funewmanagementsystem.entity.NewsArticle;
import com.funewmanagementsystem.entity.SystemAccount;
import com.funewmanagementsystem.entity.Tag;
import com.funewmanagementsystem.exception.ForbiddenException;
import com.funewmanagementsystem.exception.InvalidRequestException;
import com.funewmanagementsystem.exception.ResourceNotFoundException;
import com.funewmanagementsystem.repository.CategoryRepository;
import com.funewmanagementsystem.repository.NewsArticleRepository;
import com.funewmanagementsystem.repository.TagRepository;
import com.funewmanagementsystem.security.CurrentUserService;
import com.funewmanagementsystem.service.ArticleService;
import com.funewmanagementsystem.service.ArticleWorkFlowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    // Khai báo các Repository và Service cần dùng
    private final NewsArticleRepository newsArticleRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final CurrentUserService currentUserService;
    private final ArticleWorkFlowService articleWorkFlowService;

    /**
     * Chức năng 1: Tạo bài viết mới dành cho Contributor
     * Quy tắc 1 & 2: Trạng thái ban đầu luôn là DRAFT, tác giả là tài khoản đang đăng nhập.
     */
    @Transactional
    @Override
    public ArticleDetailResponse createArticle(CreateArticleRequest request) {
        // 1. Lấy thông tin tài khoản đang đăng nhập từ token JWT
        SystemAccount currentAuthor = currentUserService.getCurrentAccount();

        // 2. Khởi tạo thực thể bài viết mới
        NewsArticle article = new NewsArticle();
        article.setNewsTitle(request.getTitle());
        article.setNewsContent(request.getContent());
        article.setThumbnail(request.getThumbnail());

        // Mặc định bài viết mới tạo có status = DRAFT
        article.setArticleStatus(ArticleStatus.DRAFT);
        article.setNewsStatus(false); // Chưa xuất bản ra trang chủ

        // Gán tác giả và ngày tạo
        article.setCreatedBy(currentAuthor);
        article.setCreatedDate(LocalDateTime.now());

        // 3. Xử lý danh mục (Category)
        // Nếu người dùng chọn categoryId thì gán, nếu không thì lấy danh mục hoạt động đầu tiên
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new InvalidRequestException("Không tìm thấy danh mục với id: " + request.getCategoryId()));
            article.setCategory(category);
        } else {
            Category defaultCategory = categoryRepository.findAll().stream()
                    .filter(c -> Boolean.TRUE.equals(c.getIsActive()))
                    .findFirst()
                    .orElse(null);
            article.setCategory(defaultCategory);
        }

        // 4. Xử lý danh sách thẻ (Tags) nếu có
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(request.getTagIds()));
            article.setTags(tags);
        }

        // 5. Lưu vào Database và chuyển đổi sang DTO trả về cho client
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
        // 1. Lấy tài khoản đang đăng nhập
        SystemAccount currentAuthor = currentUserService.getCurrentAccount();

        // 2. Tìm bài viết theo ID
        NewsArticle article = newsArticleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài viết với id: " + id));

        // 3. Kiểm tra quyền tác giả (Quy tắc 3)
        if (!article.getCreatedBy().getId().equals(currentAuthor.getId())) {
            throw new ForbiddenException("Bạn không có quyền chỉnh sửa bài viết của người khác!");
        }

        // 4. Kiểm tra trạng thái bài viết (Quy tắc 4)
        ArticleStatus currentStatus = article.getArticleStatus();
        if (currentStatus != ArticleStatus.DRAFT && currentStatus != ArticleStatus.CHANGES_REQUESTED) {
            throw new InvalidRequestException("Chỉ được chỉnh sửa bài viết ở trạng thái DRAFT hoặc CHANGES_REQUESTED. Trạng thái hiện tại: " + currentStatus);
        }

        // 5. Cập nhật các trường thông tin
        article.setNewsTitle(request.getTitle());
        article.setNewsContent(request.getContent());
        article.setThumbnail(request.getThumbnail());
        article.setModifiedDate(LocalDateTime.now());
        article.setUpdatedBy(currentAuthor);

        // Cập nhật category nếu có
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new InvalidRequestException("Không tìm thấy danh mục với id: " + request.getCategoryId()));
            article.setCategory(category);
        }

        // Cập nhật tags nếu có
        if (request.getTagIds() != null) {
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(request.getTagIds()));
            article.setTags(tags);
        }

        // 6. Lưu xuống Database
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

        NewsArticle article = newsArticleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài viết với id: " + id));

        // Kiểm tra quyền: Contributor chỉ xem được bài của chính mình (Admin/Editor có thể xem thêm)
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
     * Hỗ trợ lọc theo trạng thái (status) và phân trang, sắp xếp theo ngày tạo.
     */
    @Override
    public PageResponse<ArticleDetailResponse> getMyArticles(ArticleStatus status, Pageable pageable) {
        SystemAccount currentAuthor = currentUserService.getCurrentAccount();

        // Tìm danh sách bài viết theo authorId và status
        Page<NewsArticle> pageResult = newsArticleRepository.findMyArticles(currentAuthor.getId(), status, pageable);

        // Chuyển đổi Page<NewsArticle> sang PageResponse<ArticleDetailResponse>
        return PageResponse.of(pageResult.map(this::mapToDetailResponse));
    }

    /**
     * Chức năng 5: Gửi bài viết để xét duyệt (Submit For Review)
     * Quy tắc 6, 7 & 8: DRAFT -> PENDING_REVIEW, kiểm tra tiêu đề và nội dung không được rỗng.
     */
    @Transactional
    @Override
    public SubmitArticleResponse submitForReview(Long id) {
        SystemAccount currentAuthor = currentUserService.getCurrentAccount();

        NewsArticle article = newsArticleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("News article not found with id: " + id));

        // 1. Kiểm tra quyền tác giả
        if (!article.getCreatedBy().getId().equals(currentAuthor.getId())) {
            throw new ForbiddenException("You can only submit your own articles for review");
        }

        // 2. Quy tắc 8: Tiêu đề và nội dung không được để trống khi gửi xét duyệt
        if (article.getNewsTitle() == null || article.getNewsTitle().trim().isEmpty()) {
            throw new InvalidRequestException("Cannot submit for review: title must not be empty");
        }
        if (article.getNewsContent() == null || article.getNewsContent().trim().isEmpty()) {
            throw new InvalidRequestException("Cannot submit for review: content must not be empty");
        }

        // 3. Kiểm tra trạng thái hiện tại phải là DRAFT hoặc CHANGES_REQUESTED
        ArticleStatus currentStatus = article.getArticleStatus();
        if (currentStatus != ArticleStatus.DRAFT && currentStatus != ArticleStatus.CHANGES_REQUESTED) {
            throw new InvalidRequestException("Only DRAFT or CHANGES_REQUESTED articles can be submitted for review. Current status: " + currentStatus);
        }

        // 4. Quy tắc 7: Chuyển trạng thái sang PENDING_REVIEW thông qua WorkflowService để ghi lịch sử (Audit Log)
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

    /**
     * Hàm phụ trợ chuyển đổi từ Entity (NewsArticle) sang DTO (ArticleDetailResponse)
     */
    private ArticleDetailResponse mapToDetailResponse(NewsArticle article) {
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
                .build();
    }
}

