package com.bifos.accountbook.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Scheduling 활성화 설정
 * @Scheduled 어노테이션을 사용하는 스케줄러 빈을 활성화합니다.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
