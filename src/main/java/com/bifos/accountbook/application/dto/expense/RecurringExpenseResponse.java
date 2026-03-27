package com.bifos.accountbook.application.dto.expense;

import com.bifos.accountbook.domain.entity.RecurringExpense;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurringExpenseResponse {

  private String uuid;
  private String familyUuid;
  private String categoryUuid;
  private String userUuid;
  private BigDecimal amount;
  private String description;
  private int dayOfMonth;
  private boolean excludeFromBudget;
  private String status;

  public static RecurringExpenseResponse from(RecurringExpense recurringExpense) {
    return RecurringExpenseResponse.builder()
                                   .uuid(recurringExpense.getUuid().getValue())
                                   .familyUuid(recurringExpense.getFamilyUuid().getValue())
                                   .categoryUuid(recurringExpense.getCategoryUuid().getValue())
                                   .userUuid(recurringExpense.getUserUuid().getValue())
                                   .amount(recurringExpense.getAmount())
                                   .description(recurringExpense.getDescription())
                                   .dayOfMonth(recurringExpense.getDayOfMonth())
                                   .excludeFromBudget(recurringExpense.isExcludeFromBudget())
                                   .status(recurringExpense.getStatus().name())
                                   .build();
  }
}
