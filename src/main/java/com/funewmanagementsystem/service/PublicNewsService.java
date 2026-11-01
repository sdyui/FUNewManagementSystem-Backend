package com.funewmanagementsystem.service;

import com.funewmanagementsystem.dto.respone.PageResponse;
import com.funewmanagementsystem.dto.respone.PublicNewsResponse;
import org.springframework.data.domain.Pageable;

public interface PublicNewsService {
    PageResponse<PublicNewsResponse> search(String title, Long categoryId, Pageable pageable);
    PublicNewsResponse getById(Long id);
}