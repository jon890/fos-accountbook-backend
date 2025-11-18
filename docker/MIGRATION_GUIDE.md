# Docker Compose 마이그레이션 가이드

## 📋 개요

기존 MySQL 8.0 컨테이너를 종료하고 새로운 MySQL 9.4 구성으로 재시작하는 가이드입니다.

## 🔄 마이그레이션 절차

### 1단계: 기존 컨테이너 확인

```bash
# 실행 중인 컨테이너 확인
docker ps | grep fos-accountbook-mysql

# 또는 모든 컨테이너 확인 (중지된 것 포함)
docker ps -a | grep fos-accountbook-mysql
```

### 2단계: 기존 컨테이너 종료 및 삭제

#### 옵션 A: 데이터 유지 (권장)

```bash
# docker 폴더로 이동
cd docker

# 컨테이너만 종료 및 삭제 (볼륨은 유지)
docker compose down
```

**주의**: 이 방법은 데이터를 유지하지만, MySQL 버전이 다르면 호환성 문제가 발생할 수 있습니다.

#### 옵션 B: 완전 삭제 (깨끗한 시작)

```bash
# docker 폴더로 이동
cd docker

# 컨테이너 및 볼륨 모두 삭제 (데이터 손실!)
docker compose down -v
```

**권장**: MySQL 8.0 → 9.4 업그레이드는 메이저 버전 변경이므로, **옵션 B를 권장**합니다.

### 3단계: 새 구성으로 컨테이너 시작

```bash
# docker 폴더에서 실행
cd docker

# 새 구성으로 컨테이너 시작 (백그라운드)
docker compose up -d

# 또는 로그를 보면서 시작
docker compose up
```

### 4단계: MySQL 준비 확인

```bash
# MySQL 로그 확인
docker compose logs -f mysql

# "ready for connections" 메시지가 나타날 때까지 대기
# 일반적으로 30초~1분 정도 소요됩니다.
```

**성공 메시지 예시:**
```
[Server] /usr/sbin/mysqld: ready for connections. Version: '9.4.x'  socket: '/var/run/mysqld/mysqld.sock'  port: 3306  MySQL Community Server
```

### 5단계: 컨테이너 상태 확인

```bash
# 컨테이너 상태 확인
docker compose ps

# 또는
docker ps | grep fos-accountbook-mysql
```

**예상 결과:**
```
NAME                      IMAGE       COMMAND                  SERVICE   CREATED         STATUS         PORTS
fos-accountbook-mysql     mysql:9.4   "docker-entrypoint.s…"   mysql     2 minutes ago   Up 2 minutes   0.0.0.0:13306->3306/tcp
```

### 6단계: MySQL 연결 테스트

```bash
# 컨테이너 내부에서 MySQL 접속 테스트
docker compose exec mysql mysql -u accountbook_user -p accountbook
# 비밀번호: accountbook_password

# 또는 로컬 MySQL 클라이언트로 접속
mysql -h localhost -P 13306 -u accountbook_user -p accountbook
```

### 7단계: 애플리케이션 테스트

```bash
# 루트 폴더로 이동
cd ..

# 애플리케이션 시작
./gradlew bootRun --args='--spring.profiles.active=local'

# 또는 테스트 실행
./gradlew test
```

## 🔧 문제 해결

### 기존 컨테이너가 다른 위치에 있는 경우

기존 `docker-compose.yml`이 루트 폴더나 다른 위치에 있다면:

```bash
# 기존 컨테이너 직접 종료
docker stop fos-accountbook-mysql
docker rm fos-accountbook-mysql

# 볼륨도 삭제하려면
docker volume rm fos-accountbook-mysql-data

# 새 구성으로 시작
cd docker
docker compose up -d
```

### 포트 충돌 문제

기존 컨테이너가 포트 3306을 사용 중이라면:

```bash
# 포트 3306을 사용하는 프로세스 확인
lsof -i :3306

# 기존 컨테이너 종료
docker stop fos-accountbook-mysql
docker rm fos-accountbook-mysql

# 새 구성은 포트 13306을 사용하므로 충돌 없음
cd docker
docker compose up -d
```

### 볼륨 이름 확인

```bash
# 모든 볼륨 확인
docker volume ls | grep fos-accountbook

# 특정 볼륨 상세 정보
docker volume inspect fos-accountbook-mysql-data
```

## 📝 주요 변경사항

### 파일 위치 변경
- **이전**: `docker-compose.yml` (루트 폴더)
- **현재**: `docker/compose.yml` (docker 폴더)

### MySQL 버전 변경
- **이전**: MySQL 8.0
- **현재**: MySQL 9.4

### 포트 변경
- **이전**: 3306 (호스트) → 3306 (컨테이너)
- **현재**: 13306 (호스트) → 3306 (컨테이너)

### 인증 플러그인 변경
- **이전**: `mysql_native_password` (명시적 설정)
- **현재**: `caching_sha2_password` (기본값, MySQL 9.4)

## ✅ 체크리스트

마이그레이션 후 확인 사항:

- [ ] 기존 컨테이너가 완전히 종료되었는지 확인
- [ ] 새 컨테이너가 정상적으로 시작되었는지 확인
- [ ] MySQL이 "ready for connections" 상태인지 확인
- [ ] 포트 13306으로 접속 가능한지 확인
- [ ] 애플리케이션이 정상적으로 연결되는지 확인
- [ ] Flyway 마이그레이션이 정상적으로 실행되는지 확인

## 🚨 주의사항

1. **데이터 백업**: 프로덕션 데이터가 있다면 반드시 백업 후 진행
2. **애플리케이션 중지**: 마이그레이션 전 애플리케이션을 중지하는 것이 좋습니다
3. **포트 충돌**: 기존 MySQL이 포트 3306을 사용 중이면 새 구성(13306)과 충돌하지 않습니다

## 📚 참고 자료

- [Docker Compose 공식 문서](https://docs.docker.com/compose/)
- [MySQL 9.4 업그레이드 가이드](../docs/MYSQL_9_UPGRADE_REVIEW.md)
- [Docker README](./README.md)

