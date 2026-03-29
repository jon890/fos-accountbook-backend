package com.bifos.accountbook.application.dto.expense;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateRecurringExpenseRequest {

  @NotBlank(message = "카테고리는 필수입니다")
  private String categoryUuid;

  @NotNull(message = "금액은 필수입니다")
  @DecimalMin(value = "0.0", inclusive = false, message = "금액은 0보다 커야 합니다")
  @Digits(integer = 10, fraction = 2, message = "금액은 최대 10자리 정수와 2자리 소수를 가질 수 있습니다")
  private BigDecimal amount;

  @Size(max = 1000, message = "설명은 최대 1000자까지 가능합니다")
  private String description;

  /**
   * 매월 지출이 등록될 날짜 (1~28)
   * 월마다 일수가 달라 최대 28일로 제한합니다.
   */
  @NotNull(message = "등록 날짜는 필수입니다")
  @Min(value = 1, message = "날짜는 1 이상이어야 합니다")
  @Max(value = 28, message = "날짜는 28 이하여야 합니다")
  private Integer dayOfMonth;

  private Boolean excludeFromBudget;
}
