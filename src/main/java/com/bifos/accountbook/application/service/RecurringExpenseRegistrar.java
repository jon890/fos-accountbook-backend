package com.bifos.accountbook.application.service;

import com.bifos.accountbook.application.event.ExpenseCreatedEvent;
import com.bifos.accountbook.application.exception.BusinessException;
import com.bifos.accountbook.application.exception.ErrorCode;
import com.bifos.accountbook.domain.entity.Expense;
import com.bifos.accountbook.domain.entity.Family;
import com.bifos.accountbook.domain.entity.RecurringExpense;
import com.bifos.accountbook.domain.repository.ExpenseRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 고정지출 등록기
 * 각 고정지출 항목을 독립적인 트랜잭션으로 처리합니다.
 * REQUIRES_NEW: 한 항목 실패가 다른 항목에 영향을 주지 않도록 트랜잭션을 분리합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RecurringExpenseRegistrar {

  private final ExpenseRepository expenseRepository;
  private final ApplicationEventPublisher eventPublisher;

  /**
   * 해당 월에 이미 등록된 경우 스킵, 아니면 지출 생성
   *
   * @param family 스케줄러에서 일괄 조회한 가족 엔티티 (null이면 FAMILY_NOT_FOUND 예외)
   * @return true: 새로 등록, false: 이미 존재하여 스킵
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public boolean registerIfNotExists(RecurringExpense recurring, LocalDate today, Family family) {
    if (family == null) {
      throw new BusinessException(ErrorCode.FAMILY_NOT_FOUND)
          .addParameter("familyUuid", recurring.getFamilyUuid().getValue());
    }

    boolean alreadyExists = expenseRepository.existsByRecurringExpenseUuidAndYearMonth(
        recurring.getUuid(), today.getYear(), today.getMonthValue());

    if (alreadyExists) {
      log.debug("[RecurringExpenseRegistrar] 이미 등록된 고정지출 스킵 - uuid: {}, 연월: {}-{}",
          recurring.getUuid().getValue(), today.getYear(), today.getMonthValue());
      return false;
    }

    LocalDateTime expenseDate = LocalDateTime.of(today, LocalTime.MIDNIGHT);

    Expense expense = family.addExpense(
        recurring.getAmount(),
        recurring.getCategoryUuid(),
        recurring.getUserUuid(),
        recurring.getDescription(),
        expenseDate
    );
    expense.setExcludeFromBudget(recurring.isExcludeFromBudget());
    expense.assignRecurringExpense(recurring.getUuid());

    expense = expenseRepository.save(expense);

    eventPublisher.publishEvent(new ExpenseCreatedEvent(
        expense.getUuid(),
        expense.getFamilyUuid(),
        expense.getUserUuid(),
        expense.getAmount(),
        expense.getDate()
    ));

    log.info("[RecurringExpenseRegistrar] 고정지출 등록 완료 - recurringUuid: {}, expenseUuid: {}",
        recurring.getUuid().getValue(), expense.getUuid().getValue());

    return true;
  }
}
