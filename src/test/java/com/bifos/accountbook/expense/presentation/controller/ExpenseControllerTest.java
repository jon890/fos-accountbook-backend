package com.bifos.accountbook.expense.presentation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bifos.accountbook.category.domain.entity.Category;
import com.bifos.accountbook.family.domain.entity.Family;
import com.bifos.accountbook.shared.AbstractControllerTest;
import com.bifos.accountbook.user.domain.entity.User;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

@DisplayName("지출 CSV 내보내기 API 통합 테스트")
class ExpenseControllerTest extends AbstractControllerTest {

  private User testUser;
  private Family testFamily;
  private Category testCategory;

  @BeforeEach
  void setUp() {
    doTransactionWithoutResult(() -> {
      testUser = fixtures.getDefaultUser();
      testFamily = fixtures.families.family()
          .owner(testUser)
          .budget(new BigDecimal("1000000.00"))
          .build();
      testCategory = fixtures.categories.category(testFamily)
          .name("식비")
          .build();
    });
  }

  @Test
  @DisplayName("해당 월 지출내역을 CSV로 다운로드할 수 있다")
  void exportExpensesCsv_Success() throws Exception {
    doTransactionWithoutResult(() -> {
      fixtures.expenses.expense(testFamily, testCategory)
          .amount(new BigDecimal("15000"))
          .description("점심식사")
          .date(LocalDateTime.of(2026, 5, 10, 12, 0))
          .build();
      fixtures.expenses.expense(testFamily, testCategory)
          .amount(new BigDecimal("25000"))
          .description("저녁식사")
          .date(LocalDateTime.of(2026, 5, 20, 18, 0))
          .build();
    });

    MvcResult result = mockMvc.perform(get(
            "/api/v1/families/{familyUuid}/expenses/export",
            testFamily.getUuid().getValue())
            .param("year", "2026")
            .param("month", "5")
            .header("X-User-UUID", testUser.getUuid().getValue()))
        .andExpect(status().isOk())
        .andExpect(header().string("Content-Type", containsString("text/csv")))
        .andReturn();

    byte[] body = result.getResponse().getContentAsByteArray();
    String content = new String(body, StandardCharsets.UTF_8);

    assertThat(content.charAt(0)).isEqualTo('﻿');
    assertThat(content).contains("날짜,카테고리,금액,설명");

    String[] lines = content.split("\r\n");
    assertThat(lines).hasSize(3);
  }

  @Test
  @DisplayName("해당 월에 지출이 없으면 헤더만 있는 CSV를 반환한다")
  void exportExpensesCsv_EmptyData() throws Exception {
    MvcResult result = mockMvc.perform(get(
            "/api/v1/families/{familyUuid}/expenses/export",
            testFamily.getUuid().getValue())
            .param("year", "2026")
            .param("month", "1")
            .header("X-User-UUID", testUser.getUuid().getValue()))
        .andExpect(status().isOk())
        .andExpect(header().string("Content-Type", containsString("text/csv")))
        .andReturn();

    byte[] body = result.getResponse().getContentAsByteArray();
    String content = new String(body, StandardCharsets.UTF_8);

    assertThat(content.charAt(0)).isEqualTo('﻿');
    assertThat(content).contains("날짜,카테고리,금액,설명");

    String[] lines = content.split("\r\n");
    assertThat(lines).hasSize(1);
  }

  @Test
  @DisplayName("description에 콤마가 있으면 큰따옴표로 감싸진다")
  void exportExpensesCsv_DescriptionWithComma() throws Exception {
    doTransactionWithoutResult(() -> {
      fixtures.expenses.expense(testFamily, testCategory)
          .amount(new BigDecimal("10000"))
          .description("식비, 음식")
          .date(LocalDateTime.of(2026, 5, 15, 12, 0))
          .build();
    });

    MvcResult result = mockMvc.perform(get(
            "/api/v1/families/{familyUuid}/expenses/export",
            testFamily.getUuid().getValue())
            .param("year", "2026")
            .param("month", "5")
            .header("X-User-UUID", testUser.getUuid().getValue()))
        .andExpect(status().isOk())
        .andReturn();

    byte[] body = result.getResponse().getContentAsByteArray();
    String content = new String(body, StandardCharsets.UTF_8);
    assertThat(content).contains("\"식비, 음식\"");
  }

  @Test
  @DisplayName("description에 큰따옴표가 있으면 RFC 4180에 따라 이스케이프된다")
  void exportExpensesCsv_DescriptionWithDoubleQuote() throws Exception {
    doTransactionWithoutResult(() -> {
      fixtures.expenses.expense(testFamily, testCategory)
          .amount(new BigDecimal("10000"))
          .description("\"특별\" 식사")
          .date(LocalDateTime.of(2026, 5, 15, 12, 0))
          .build();
    });

    MvcResult result = mockMvc.perform(get(
            "/api/v1/families/{familyUuid}/expenses/export",
            testFamily.getUuid().getValue())
            .param("year", "2026")
            .param("month", "5")
            .header("X-User-UUID", testUser.getUuid().getValue()))
        .andExpect(status().isOk())
        .andReturn();

    byte[] body = result.getResponse().getContentAsByteArray();
    String content = new String(body, StandardCharsets.UTF_8);
    assertThat(content).contains("\"\"\"특별\"\" 식사\"");
  }

  @Test
  @DisplayName("가족 구성원이 아닌 사용자는 403을 반환한다")
  void exportExpensesCsv_NotFamilyMember_Forbidden() throws Exception {
    User otherUser = doTransaction(() -> fixtures.users.getOtherUser());
    fixtures.users.setSecurityContext(otherUser);

    mockMvc.perform(get(
            "/api/v1/families/{familyUuid}/expenses/export",
            testFamily.getUuid().getValue())
            .param("year", "2026")
            .param("month", "5")
            .header("X-User-UUID", otherUser.getUuid().getValue()))
        .andExpect(status().isForbidden());
  }
}
