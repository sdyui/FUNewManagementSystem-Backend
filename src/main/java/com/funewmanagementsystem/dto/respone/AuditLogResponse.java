package com.funewmanagementsystem.dto.respone;

import com.funewmanagementsystem.entity.ArticleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {
    private Long id;
    private Long articleId;
    private String articleTitle;
    private ArticleStatus fromStatus;
    private ArticleStatus toStatus;
    private String actorName;      // "SYSTEM" khi scheduler tự đăng
    private String note;
    private LocalDateTime actionAt;
}