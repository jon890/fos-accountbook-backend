package com.bifos.accountbook.domain.repository;

import com.bifos.accountbook.domain.entity.RecurringExpense;
import com.bifos.accountbook.domain.value.CustomUuid;
import java.util.List;
import java.util.Optional;

/**
 * 고정지출 Repository 인터페이스
 */
public interface RecurringExpenseRepository {

  RecurringExpense save(RecurringExpense recurringExpense);

  Optional<RecurringExpense> findActiveByUuid(CustomUuid uuid);

  /**
   * 가족의 모든 활성 고정지출 조회
   */
  List<RecurringExpense> findAllActiveByFamilyUuid(CustomUuid familyUuid);

  /**
   * 스케줄러용: 특정 날짜(1~28)에 해당하는 모든 활성 고정지출 조회
   */
  List<RecurringExpense> findAllActiveByDayOfMonth(int dayOfMonth);

  /**
   * 스케줄러용: 월 마지막 날 처리 - dayOfMonth 이상인 모든 활성 고정지출 조회
   * (예: 2월 28일에 dayOfMonth=29,30,31인 항목도 함께 처리)
   */
  List<RecurringExpense> findAllActiveByDayOfMonthGreaterThanEqual(int dayOfMonth);
}
