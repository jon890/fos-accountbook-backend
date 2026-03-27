package com.bifos.accountbook.application.service;

import com.bifos.accountbook.application.event.ExpenseCreatedEvent;
import com.bifos.accountbook.domain.entity.Expense;
import com.bifos.accountbook.domain.entity.Family;
import com.bifos.accountbook.domain.entity.RecurringExpense;
import com.bifos.accountbook.domain.repository.ExpenseRepository;
import com.bifos.accountbook.domain.repository.FamilyRepository;
import com.bifos.accountbook.domain.repository.RecurringExpenseRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 고정지출 스케줄러
 * 매일 자정에 실행되어 오늘 날짜에 해당하는 고정지출을 자동으로 등록합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RecurringExpenseScheduler {

  private final RecurringExpenseRepository recurringExpenseRepository;
  private final ExpenseRepository expenseRepository;
  private final FamilyRepository familyRepository;
  private final ApplicationEventPublisher eventPublisher;

  /**
   * 매일 자정(00:00)에 실행
   * 오늘 날짜(dayOfMonth)에 해당하는 고정지출을 일괄 등록합니다.
   */
  @Scheduled(cron = "0 0 0 * * *")
  @Transactional
  public void registerRecurringExpenses() {
    LocalDate today = LocalDate.now();
    int dayOfMonth = today.getDayOfMonth();

    // 28일 초과 날짜에 등록된 고정지출은 해당 월의 마지막 날에 처리
    // (예: 30일 등록 → 2월에는 28일에 처리)
    int targetDay = Math.min(dayOfMonth, 28);

    log.info("[RecurringExpenseScheduler] 고정지출 등록 시작 - 날짜: {}, 대상 day: {}", today, targetDay);

    List<RecurringExpense> targets = recurringExpenseRepository.findAllActiveByDayOfMonth(targetDay);

    if (targets.isEmpty()) {
      log.info("[RecurringExpenseScheduler] 오늘 등록할 고정지출 없음");
      return;
    }

    int successCount = 0;
    int skipCount = 0;

    for (RecurringExpense recurring : targets) {
      try {
        boolean registered = registerIfNotExists(recurring, today);
        if (registered) {
          successCount++;
        } else {
          skipCount++;
        }
      } catch (Exception e) {
        log.error("[RecurringExpenseScheduler] 고정지출 등록 실패 - uuid: {}, error: {}",
            recurring.getUuid().getValue(), e.getMessage(), e);
      }
    }

    log.info("[RecurringExpenseScheduler] 완료 - 등록: {}건, 스킵(이미 등록): {}건", successCount, skipCount);
  }

  /**
   * 이미 해당 월에 등록된 경우 스킵, 아니면 지출 생성
   *
   * @return true: 새로 등록, false: 이미 존재하여 스킵
   */
  private boolean registerIfNotExists(RecurringExpense recurring, LocalDate today) {
    boolean alreadyExists = expenseRepository.existsByRecurringExpenseUuidAndYearMonth(
        recurring.getUuid(), today.getYear(), today.getMonthValue());

    if (alreadyExists) {
      log.debug("[RecurringExpenseScheduler] 이미 등록된 고정지출 스킵 - uuid: {}, 연월: {}-{}",
          recurring.getUuid().getValue(), today.getYear(), today.getMonthValue());
      return false;
    }

    Family family = familyRepository.findByUuid(recurring.getFamilyUuid())
                                    .orElseThrow(() -> new IllegalStateException(
                                        "가족을 찾을 수 없습니다: " + recurring.getFamilyUuid().getValue()));

    LocalDateTime expenseDate = LocalDateTime.of(today, LocalTime.MIDNIGHT);

    Expense expense = family.addExpense(
        recurring.getAmount(),
        recurring.getCategoryUuid(),
        recurring.getUserUuid(),
        recurring.getDescription(),
        expenseDate
    );
    expense.setExcludeFromBudget(recurring.isExcludeFromBudget());
    expense.setRecurringExpenseUuid(recurring.getUuid());

    expense = expenseRepository.save(expense);

    eventPublisher.publishEvent(new ExpenseCreatedEvent(
        expense.getUuid(),
        expense.getFamilyUuid(),
        expense.getUserUuid(),
        expense.getAmount(),
        expense.getDate()
    ));

    log.info("[RecurringExpenseScheduler] 고정지출 등록 완료 - recurringUuid: {}, expenseUuid: {}",
        recurring.getUuid().getValue(), expense.getUuid().getValue());

    return true;
  }
}
