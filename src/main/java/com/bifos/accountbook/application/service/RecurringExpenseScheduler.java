package com.bifos.accountbook.application.service;

import com.bifos.accountbook.domain.entity.Family;
import com.bifos.accountbook.domain.entity.RecurringExpense;
import com.bifos.accountbook.domain.repository.FamilyRepository;
import com.bifos.accountbook.domain.repository.RecurringExpenseRepository;
import com.bifos.accountbook.domain.value.CustomUuid;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 고정지출 스케줄러
 * 매일 자정에 실행되어 오늘 날짜에 해당하는 고정지출을 자동으로 등록합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RecurringExpenseScheduler {

  private final RecurringExpenseRepository recurringExpenseRepository;
  private final FamilyRepository familyRepository;
  private final RecurringExpenseRegistrar recurringExpenseRegistrar;

  /**
   * 매일 자정(00:00)에 실행
   * 오늘 날짜(dayOfMonth)에 해당하는 고정지출을 일괄 등록합니다.
   * 월 마지막 날에는 해당 월에 존재하지 않는 날짜(예: 2월의 29~31일)에 등록된 항목도 처리합니다.
   */
  @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
  public void registerRecurringExpenses() {
    LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
    int dayOfMonth = today.getDayOfMonth();
    boolean isLastDayOfMonth = dayOfMonth == today.lengthOfMonth();

    log.info("[RecurringExpenseScheduler] 고정지출 등록 시작 - 날짜: {}, 월 마지막 날: {}",
        today, isLastDayOfMonth);

    List<RecurringExpense> targets = isLastDayOfMonth
        ? recurringExpenseRepository.findAllActiveByDayOfMonthGreaterThanEqual(dayOfMonth)
        : recurringExpenseRepository.findAllActiveByDayOfMonth(dayOfMonth);

    if (targets.isEmpty()) {
      log.info("[RecurringExpenseScheduler] 오늘 등록할 고정지출 없음");
      return;
    }

    // 가족 정보 일괄 조회로 유효하지 않은 familyUuid 사전 필터링 (N+1 방지)
    List<CustomUuid> familyUuids = targets.stream()
                                          .map(RecurringExpense::getFamilyUuid)
                                          .distinct()
                                          .collect(Collectors.toList());
    Set<CustomUuid> validFamilyUuids = familyRepository.findAllByUuidIn(familyUuids).stream()
                                                        .map(Family::getUuid)
                                                        .collect(Collectors.toSet());

    int successCount = 0;
    int skipCount = 0;

    for (RecurringExpense recurring : targets) {
      if (!validFamilyUuids.contains(recurring.getFamilyUuid())) {
        log.warn("[RecurringExpenseScheduler] 유효하지 않은 가족 UUID - familyUuid: {}, recurringUuid: {}",
            recurring.getFamilyUuid().getValue(), recurring.getUuid().getValue());
        continue;
      }

      try {
        boolean registered = recurringExpenseRegistrar.registerIfNotExists(recurring, today);
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

    log.info("[RecurringExpenseScheduler] 완료 - 등록: {}건, 스킵(이미 등록): {}건",
        successCount, skipCount);
  }
}
