package com.bifos.accountbook.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Scheduling 활성화 설정
 * Scheduled 어노테이션을 사용하는 스케줄러 빈을 활성화합니다.
 * test 프로파일에서는 스케줄러가 실행되지 않도록 local/prod 프로파일에서만 활성화합니다.
 */
@Configuration
@EnableScheduling
@Profile({"local", "prod"})
public class SchedulingConfig {
}
