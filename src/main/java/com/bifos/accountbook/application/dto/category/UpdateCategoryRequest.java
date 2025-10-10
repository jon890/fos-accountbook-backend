package com.bifos.accountbook.application.dto.category;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCategoryRequest {
    
    @Size(min = 1, max = 50, message = "카테고리 이름은 1-50자 사이여야 합니다")
    private String name;
    
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "색상은 #RRGGBB 형식이어야 합니다")
    private String color;
    
    @Size(max = 50, message = "아이콘은 최대 50자까지 가능합니다")
    private String icon;
}

