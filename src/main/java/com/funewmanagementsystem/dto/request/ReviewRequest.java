package com.funewmanagementsystem.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class ReviewRequest {
    @Size(max = 1000, message = "Note must be at most 1000 characters")
    private String note;
    private LocalDateTime publishDate;
}
