package com.funewmanagementsystem.service.impl;

import com.funewmanagementsystem.dto.request.UpdateNewsArticleRequest;
import com.funewmanagementsystem.dto.respone.NewsArticleResponse;
import com.funewmanagementsystem.dto.respone.PageResponse;
import com.funewmanagementsystem.entity.Category;
import com.funewmanagementsystem.entity.NewsArticle;
import com.funewmanagementsystem.entity.SystemAccount;
import com.funewmanagementsystem.entity.Tag;
import com.funewmanagementsystem.exception.InvalidRequestException;
import com.funewmanagementsystem.exception.ResourceNotFoundException;
import com.funewmanagementsystem.repository.CategoryRepository;
import com.funewmanagementsystem.repository.NewsArticleRepository;
import com.funewmanagementsystem.repository.SystemAccountRepository;
import com.funewmanagementsystem.repository.TagRepository;
import com.funewmanagementsystem.service.NewsArticleService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NewsArticleServiceImpl implements NewsArticleService {
    private final NewsArticleRepository newsArticleRepository;
    private final CategoryRepository categoryRepository;
    private final SystemAccountRepository systemAccountRepository;
    private final TagRepository tagRepository;
    private static final Set<String> SORTABLE_FIELDS = Set.of("id", "newsTitle", "newTilte", "createdDate", "modifiedDate", "newsStatus");
    private NewsArticleResponse toResponse(NewsArticle n) {
        return new NewsArticleResponse(
                n.getId(),
                n.getNewsTitle(),
                n.getHeadline(),
                n.getNewsContent(),
                n.getNewsSource(),
                n.getNewsStatus(),
                n.getCreatedDate(),
                n.getModifiedDate(),
                n.getCategory().getId(),
                n.getCreatedBy().getAccountName(),
                n.getUpdatedBy() == null ? null : n.getUpdatedBy().getId(),
                n.getTags().stream().map(Tag::getTagName).toList());
    }
    @Override
    public NewsArticle getById(long id) {
        return newsArticleRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Not found news article with id:"+id));
    }

    @Override
    public NewsArticleResponse getNewsById(Long id) {
        NewsArticle newsArticle = newsArticleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found news article with id: " + id));
        return toResponse(newsArticle);
    }

    @Transactional
    @Override
    public NewsArticleResponse createNewsArticle(UpdateNewsArticleRequest newsArticleRequest) {
        NewsArticle newsArticle = new NewsArticle();
        newsArticle.setNewsTitle(newsArticleRequest.getNewsTitle());
        newsArticle.setHeadline(newsArticleRequest.getNewsHeadline());
        newsArticle.setNewsContent(newsArticleRequest.getNewContent());
        newsArticle.setNewsSource(newsArticleRequest.getNewsSource());
        Category category = categoryRepository.findById(newsArticleRequest.getCategoryId()).orElseThrow(()->new InvalidRequestException("Not found category with id:"+newsArticleRequest.getCategoryId()));
        if(!Boolean.TRUE.equals(category.getIsActive())){
            throw new InvalidRequestException("Category has been inactivated");
        }
        newsArticle.setCategory(category);
        newsArticle.setNewsStatus(true);
        SystemAccount account = systemAccountRepository.findById(newsArticleRequest.getAccountId()).orElseThrow(()->new InvalidRequestException("Not found Id: "+newsArticleRequest.getAccountId()));
        newsArticle.setCreatedBy(account);
        newsArticle.setCreatedDate(LocalDateTime.now());
        Set<Tag> tags = new HashSet<>();
        if(newsArticleRequest.getTagIds()!=null && !newsArticleRequest.getTagIds().isEmpty()){
            List<Tag> foundTags = tagRepository.findAllById(newsArticleRequest.getTagIds());
            if(foundTags.size() != newsArticleRequest.getTagIds().size()){
                throw new ResourceNotFoundException("Tag Ids are not matching");
            }
            tags.addAll(foundTags);
        }
        newsArticle.setTags(tags);

        return toResponse(newsArticleRepository.save(newsArticle));
    }
    @Transactional
    @Override
    public NewsArticleResponse updateNewsArticle(UpdateNewsArticleRequest newsArticleRequest, Long id) {
        NewsArticle newsArticle = newsArticleRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Not found news article with id:"+id));
        newsArticle.setNewsTitle(newsArticleRequest.getNewsTitle());
        newsArticle.setHeadline(newsArticleRequest.getNewsHeadline());
        newsArticle.setNewsContent(newsArticleRequest.getNewContent());
        newsArticle.setNewsSource(newsArticleRequest.getNewsSource());
        Category category = categoryRepository.findById(newsArticleRequest.getCategoryId()).orElseThrow(()->new InvalidRequestException("Not found category with id:"+newsArticleRequest.getCategoryId()));
        if(!Boolean.TRUE.equals(category.getIsActive())){
            throw new InvalidRequestException("Category has been inactivated");
        }
        newsArticle.setCategory(category);
        if (newsArticleRequest.getNewsStatus() != null) {
            newsArticle.setNewsStatus(newsArticleRequest.getNewsStatus());
        }
        SystemAccount account = systemAccountRepository.findById(newsArticleRequest.getAccountId()).orElseThrow(()->new InvalidRequestException("Not found Id: "+newsArticleRequest.getAccountId()));
        newsArticle.setUpdatedBy(account);
        newsArticle.setModifiedDate(LocalDateTime.now());
        Set<Tag> tags = new HashSet<>();
        if (newsArticleRequest.getTagIds() != null && !newsArticleRequest.getTagIds().isEmpty()) {
            List<Tag> foundTags = tagRepository.findAllById(newsArticleRequest.getTagIds());
            if (foundTags.size() != newsArticleRequest.getTagIds().size()) {
                throw new ResourceNotFoundException("Tag Ids are not matching");
            }
            tags.addAll(foundTags);
        }
        newsArticle.setTags(tags);


        return toResponse(newsArticleRepository.save(newsArticle));

    }
    @Transactional
    @Override
    public void deleteNewsArticle(Long id) {
        if(!newsArticleRepository.existsById(id)){
            throw new ResourceNotFoundException("News article with id:"+id+" not found");
        }
        newsArticleRepository.deleteById(id);
    }
    @Override
    public PageResponse<NewsArticleResponse> search(String title, Long categoryId, Boolean status, Pageable pageable) {
        for(Sort.Order order : pageable.getSort()){
            if(!SORTABLE_FIELDS.contains(order.getProperty())){
                throw new InvalidRequestException("Sorted fields are not matching");
            }
        }
        return PageResponse.of(newsArticleRepository.search(blankToNull(title), categoryId, status, pageable).map(this::toResponse));
    }
    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
