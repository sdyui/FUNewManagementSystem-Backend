package com.funewmanagementsystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {
    @NotBlank(message = "Tên chuyên mục không được để trống")
    @Size(max = 100, message = "Tên chuyên mục tối đa 100 ký tự")
    private String categoryName;

    @Size(max = 1000, message = "Mô tả tối đa 1000 ký tự")
    private String categoryDescription;

    private Long parentCategoryId;

    private Boolean isActive = true;
}
