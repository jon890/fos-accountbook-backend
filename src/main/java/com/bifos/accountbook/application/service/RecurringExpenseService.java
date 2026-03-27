package com.bifos.accountbook.application.service;

import com.bifos.accountbook.application.dto.expense.CreateRecurringExpenseRequest;
import com.bifos.accountbook.application.dto.expense.RecurringExpenseResponse;
import com.bifos.accountbook.application.dto.expense.UpdateRecurringExpenseRequest;
import com.bifos.accountbook.application.exception.BusinessException;
import com.bifos.accountbook.application.exception.ErrorCode;
import com.bifos.accountbook.domain.entity.RecurringExpense;
import com.bifos.accountbook.domain.repository.RecurringExpenseRepository;
import com.bifos.accountbook.domain.value.CustomUuid;
import com.bifos.accountbook.presentation.annotation.FamilyUuid;
import com.bifos.accountbook.presentation.annotation.UserUuid;
import com.bifos.accountbook.presentation.annotation.ValidateFamilyAccess;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 고정지출 서비스
 * 고정지출 템플릿 CRUD를 담당합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecurringExpenseService {

  private final RecurringExpenseRepository recurringExpenseRepository;
  private final CategoryService categoryService;

  /**
   * 고정지출 등록
   */
  @ValidateFamilyAccess
  @Transactional
  public RecurringExpenseResponse createRecurringExpense(@UserUuid CustomUuid userUuid,
                                                         @FamilyUuid CustomUuid familyUuid,
                                                         CreateRecurringExpenseRequest request) {
    CustomUuid categoryUuid = CustomUuid.from(request.getCategoryUuid());

    // 카테고리 유효성 및 가족 소속 검증 (캐시 활용)
    categoryService.validateAndFindCached(familyUuid, categoryUuid);

    RecurringExpense recurringExpense = RecurringExpense.builder()
                                                        .familyUuid(familyUuid)
                                                        .categoryUuid(categoryUuid)
                                                        .userUuid(userUuid)
                                                        .amount(request.getAmount())
                                                        .description(request.getDescription())
                                                        .dayOfMonth(request.getDayOfMonth())
                                                        .excludeFromBudget(
                                                            request.getExcludeFromBudget() != null
                                                                && request.getExcludeFromBudget())
                                                        .build();

    recurringExpense = recurringExpenseRepository.save(recurringExpense);
    log.info("Created recurring expense: {} for family: {}", recurringExpense.getUuid().getValue(),
        familyUuid.getValue());

    return RecurringExpenseResponse.from(recurringExpense);
  }

  /**
   * 가족의 고정지출 목록 조회
   */
  @ValidateFamilyAccess
  public List<RecurringExpenseResponse> getRecurringExpenses(@UserUuid CustomUuid userUuid,
                                                             @FamilyUuid CustomUuid familyUuid) {
    return recurringExpenseRepository.findAllActiveByFamilyUuid(familyUuid)
                                     .stream()
                                     .map(RecurringExpenseResponse::from)
                                     .toList();
  }

  /**
   * 고정지출 수정
   */
  @ValidateFamilyAccess
  @Transactional
  public RecurringExpenseResponse updateRecurringExpense(@UserUuid CustomUuid userUuid,
                                                         @FamilyUuid CustomUuid familyUuid,
                                                         CustomUuid recurringExpenseUuid,
                                                         UpdateRecurringExpenseRequest request) {
    RecurringExpense recurringExpense = recurringExpenseRepository.findActiveByUuid(recurringExpenseUuid)
                                                                  .orElseThrow(() -> new BusinessException(
                                                                      ErrorCode.RECURRING_EXPENSE_NOT_FOUND)
                                                                      .addParameter("uuid",
                                                                          recurringExpenseUuid.getValue()));

    if (!recurringExpense.getFamilyUuid().equals(familyUuid)) {
      throw new BusinessException(ErrorCode.RECURRING_EXPENSE_NOT_FOUND)
          .addParameter("uuid", recurringExpenseUuid.getValue());
    }

    CustomUuid categoryUuid = request.getCategoryUuid() != null
        ? CustomUuid.from(request.getCategoryUuid())
        : null;

    if (categoryUuid != null) {
      categoryService.validateAndFindCached(familyUuid, categoryUuid);
    }

    recurringExpense.update(categoryUuid, request.getAmount(), request.getDescription(),
        request.getDayOfMonth(), request.getExcludeFromBudget());

    return RecurringExpenseResponse.from(recurringExpense);
  }

  /**
   * 고정지출 삭제 (Soft Delete)
   */
  @ValidateFamilyAccess
  @Transactional
  public void deleteRecurringExpense(@UserUuid CustomUuid userUuid,
                                     @FamilyUuid CustomUuid familyUuid,
                                     CustomUuid recurringExpenseUuid) {
    RecurringExpense recurringExpense = recurringExpenseRepository.findActiveByUuid(recurringExpenseUuid)
                                                                  .orElseThrow(() -> new BusinessException(
                                                                      ErrorCode.RECURRING_EXPENSE_NOT_FOUND)
                                                                      .addParameter("uuid",
                                                                          recurringExpenseUuid.getValue()));

    if (!recurringExpense.getFamilyUuid().equals(familyUuid)) {
      throw new BusinessException(ErrorCode.RECURRING_EXPENSE_NOT_FOUND)
          .addParameter("uuid", recurringExpenseUuid.getValue());
    }

    recurringExpense.delete();
    log.info("Deleted recurring expense: {} in family: {}", recurringExpenseUuid.getValue(),
        familyUuid.getValue());
  }
}
