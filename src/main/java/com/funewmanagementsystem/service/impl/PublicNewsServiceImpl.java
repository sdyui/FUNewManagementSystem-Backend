package com.funewmanagementsystem.service.impl;

import com.funewmanagementsystem.dto.respone.PageResponse;
import com.funewmanagementsystem.dto.respone.PublicNewsResponse;
import com.funewmanagementsystem.entity.ArticleStatus;
import com.funewmanagementsystem.entity.NewsArticle;
import com.funewmanagementsystem.exception.InvalidRequestException;
import com.funewmanagementsystem.exception.ResourceNotFoundException;
import com.funewmanagementsystem.repository.NewsArticleRepository;
import com.funewmanagementsystem.service.PublicNewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class PublicNewsServiceImpl implements PublicNewsService {
    private static final Set<String> SORT_FIELDS = Set.of("publishDate","newsTitle");
    private final NewsArticleRepository newsArticleRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PublicNewsResponse> search(String title, Long categoryId, Pageable pageable) {
        for(Sort.Order order : pageable.getSort()) {
            if(!SORT_FIELDS.contains(order.getProperty())) {
                throw  new InvalidRequestException("Sort fields are not supported");
            }
        }
        String keyword = (title == null || title.isEmpty()) ? null : title.trim();
        return PageResponse.of(newsArticleRepository.searchByStatus(ArticleStatus.PUBLISHED,keyword,categoryId,pageable).map(this::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PublicNewsResponse getById(Long id) {
        NewsArticle article = newsArticleRepository.findByIdAndArticleStatus(id,ArticleStatus.PUBLISHED).orElseThrow(()->new ResourceNotFoundException("Article Not Found"));
        return toResponse(article);
    }
    private PublicNewsResponse toResponse(NewsArticle a) {
        return PublicNewsResponse.builder()
                .id(a.getId())
                .title(a.getNewsTitle())
                .headline(a.getHeadline())
                .thumbnail(a.getThumbnail())
                .content(a.getNewsContent())
                .newsSource(a.getNewsSource())
                .categoryId(a.getCategory() == null ? null : a.getCategory().getId())
                .categoryName(a.getCategory() == null ? null : a.getCategory().getCategoryName())
                .authorName(a.getCreatedBy() == null ? null : a.getCreatedBy().getAccountName())
                .publishDate(a.getPublishDate())
                .build();
    }

}
