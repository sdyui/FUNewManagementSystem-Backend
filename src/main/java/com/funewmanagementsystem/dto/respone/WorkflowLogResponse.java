package com.funewmanagementsystem.dto.respone;

import com.funewmanagementsystem.entity.ArticleStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowLogResponse {
    private ArticleStatus fromStatus;
    private ArticleStatus toStatus;
    private String actorName;
    private String note;
    private LocalDateTime actionAt;
}
