package com.bifos.accountbook.application.dto.expense;

import com.bifos.accountbook.domain.entity.Expense;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponse {

    private String uuid;
    private String familyUuid;
    private String categoryUuid;
    private String categoryName;
    private String categoryColor;
    private BigDecimal amount;
    private String description;
    private LocalDateTime date;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ExpenseResponse from(Expense expense) {
        return ExpenseResponse.builder()
                .uuid(expense.getUuid().getValue())
                .familyUuid(expense.getFamilyUuid().getValue())
                .categoryUuid(expense.getCategoryUuid().getValue())
                .categoryName(expense.getCategory() != null ? expense.getCategory().getName() : null)
                .categoryColor(expense.getCategory() != null ? expense.getCategory().getColor() : null)
                .amount(expense.getAmount())
                .description(expense.getDescription())
                .date(expense.getDate())
                .createdAt(expense.getCreatedAt())
                .updatedAt(expense.getUpdatedAt())
                .build();
    }

    public static ExpenseResponse fromWithoutCategory(Expense expense) {
        return ExpenseResponse.builder()
                .uuid(expense.getUuid().toString())
                .familyUuid(expense.getFamilyUuid().getValue())
                .categoryUuid(expense.getCategoryUuid().getValue())
                .amount(expense.getAmount())
                .description(expense.getDescription())
                .date(expense.getDate())
                .createdAt(expense.getCreatedAt())
                .updatedAt(expense.getUpdatedAt())
                .build();
    }
}

