# 🏗️ Fourtune 인프라 구축 가이드

> 로컬, 개발, 프로덕션 환경 전체 인프라 설계 및 구축 상세 가이드

**작성일**: 2026-01-08  
**작성자**: Fourtune Team

---

## 📋 목차

1. [전체 아키텍처 개요](#1-전체-아키텍처-개요)
2. [환경별 구성 비교](#2-환경별-구성-비교)
3. [로컬 개발 환경 (Local)](#3-로컬-개발-환경-local)
4. [개발 서버 환경 (Dev)](#4-개발-서버-환경-dev)
5. [프로덕션 환경 (Prod)](#5-프로덕션-환경-prod)
6. [기술 스택 선정 이유](#6-기술-스택-선정-이유)
7. [보안 설계](#7-보안-설계)
8. [성능 최적화](#8-성능-최적화)
9. [모니터링 및 로깅](#9-모니터링-및-로깅)
10. [트러블슈팅](#10-트러블슈팅)

---

## 1. 전체 아키텍처 개요

### 1.1 아키텍처 설계 철학

#### **현재: Monolithic Architecture**
- ✅ 빠른 초기 개발 속도
- ✅ 단순한 배포 프로세스
- ✅ 낮은 운영 복잡도
- ✅ 팀 규모에 적합 (3-5명)

#### **미래: MSA 전환 준비**
- 🎯 **도메인 기반 패키지 구조**: 각 도메인을 독립적인 마이크로서비스로 전환 가능
- 🎯 **이벤트 기반 아키텍처**: Spring Event → Kafka로 전환 가능
- 🎯 **API Gateway 준비**: Nginx가 향후 Spring Cloud Gateway로 전환 가능
- 🎯 **독립적인 데이터베이스**: 도메인별 스키마 분리 준비

### 1.2 전체 시스템 구성도

```
┌─────────────────────────────────────────────────────────────┐
│                        사용자 (Client)                        │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     Nginx (Reverse Proxy)                    │
│           - 로드 밸런싱 / SSL 종료 / Rate Limiting            │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                   Spring Boot Application                    │
│         (Java 25 + Spring Boot 4.0.1 + Spring Security)     │
│                                                              │
│  ┌──────────────────────────────────────────────────┐      │
│  │           Domain Layer (비즈니스 로직)             │      │
│  │  - User / Auction / Bid / Payment / Settlement  │      │
│  └──────────────────────────────────────────────────┘      │
└─────────────────────────────────────────────────────────────┘
          │              │              │              │
          ▼              ▼              ▼              ▼
┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│ PostgreSQL   │ │    Redis     │ │Elasticsearch │ │    Kafka     │
│  (Main DB)   │ │   (Cache)    │ │   (Search)   │ │  (Event)     │
└──────────────┘ └──────────────┘ └──────────────┘ └──────────────┘
```

---

## 2. 환경별 구성 비교

### 2.1 환경별 차이점 요약

| 항목 | 로컬 (Local) | 개발 (Dev) | 프로덕션 (Prod) |
|------|-------------|-----------|----------------|
| **목적** | 팀원 개발 환경 | 통합 테스트/QA | 실제 서비스 운영 |
| **위치** | 개발자 PC (Docker) | NCP 마이크로 서버 | AWS EC2/ECS |
| **Nginx** | ❌ 불필요 | ✅ HTTP only | ✅ HTTPS + SSL |
| **환경변수** | 하드코딩 (즉시 실행) | `.env.dev` | `.env.prod` |
| **DB ddl-auto** | `update` (편의성) | `validate` (안전) | `validate` (필수) |
| **로그 레벨** | DEBUG | DEBUG | WARN |
| **재시작 정책** | `no` (개발 편의) | `unless-stopped` | `always` |
| **리소스 제한** | ❌ 없음 | ❌ 없음 | ✅ CPU/Memory 제한 |
| **Rate Limiting** | ❌ 없음 | 50 req/s | 10 req/s |
| **Health Check** | 간단 | 상세 | 엄격 |
| **도메인** | localhost | IP 또는 dev.domain.com | domain.com |
| **HTTPS** | ❌ HTTP only | ❌ HTTP only | ✅ SSL 인증서 |
| **팀 공유** | ✅ 동일 환경 보장 | ✅ 통합 테스트 | ❌ 외부 노출 |

### 2.2 왜 환경을 3개로 분리했나?

#### **로컬 (Local)**
- **목적**: 개발자 개인 PC에서 빠르게 개발/테스트
- **특징**: "내 PC에선 되는데?" 문제 해결
- **장점**: 
  - 인터넷 없이도 개발 가능
  - 빠른 피드백 루프
  - 팀원 간 환경 통일

#### **개발 서버 (Dev)**
- **목적**: 팀 전체가 공유하는 통합 테스트 환경
- **특징**: 프로덕션과 유사하지만 덜 엄격
- **장점**: 
  - 실제 서버 환경에서 테스트
  - 팀원 간 기능 통합 확인
  - PM/디자이너 검증

#### **프로덕션 (Prod)**
- **목적**: 실제 사용자 서비스
- **특징**: 최고 수준의 보안/안정성
- **장점**: 
  - 실제 트래픽 처리
  - 고가용성 보장
  - 모니터링 강화

---

## 3. 로컬 개발 환경 (Local)

### 3.1 설계 목표

> **"팀원 누구나 `docker-compose up -d` 한 줄로 동일한 환경 구축"**

#### 핵심 원칙
1. **Zero Configuration**: 환경변수 파일 없이도 실행
2. **Fast Feedback**: 빠른 컨테이너 재시작
3. **Developer Friendly**: 개발 편의성 최우선
4. **Team Consistency**: 모든 팀원 동일한 환경

### 3.2 Docker Compose 구성 (`docker-compose.yml`)

#### **서비스 구성**

```yaml
services:
  postgres:    # 메인 데이터베이스
  redis:       # 캐시 + 분산 락
  elasticsearch: # 상품 검색
  zookeeper:   # Kafka 의존성
  kafka:       # 이벤트 스트리밍 (준비)
  app:         # Spring Boot 애플리케이션
```

#### **왜 이렇게 구성했나?**

##### 1. **PostgreSQL 16**
```yaml
postgres:
  image: postgres:16-alpine
  environment:
    POSTGRES_DB: fourtune_db
    POSTGRES_USER: fourtune_user
    POSTGRES_PASSWORD: fourtune  # 로컬용 간단한 비밀번호
  ports:
    - "5432:5432"
```

**선택 이유:**
- ✅ **Alpine 이미지**: 용량 작음 (150MB vs 500MB)
- ✅ **PostgreSQL 16**: 최신 LTS, 성능 향상
- ✅ **단순 비밀번호**: 로컬 개발용, 보안 불필요
- ✅ **5432 포트 노출**: DBeaver 직접 연결 가능

**비즈니스 요구사항 연결:**
- 경매 입찰 데이터: ACID 보장 필수
- 결제 정보: 트랜잭션 무결성 중요
- 정산 시스템: 복잡한 JOIN 쿼리

##### 2. **Redis 7**
```yaml
redis:
  image: redis:7-alpine
  ports:
    - "6379:6379"
  command: redis-server --appendonly yes
```

**선택 이유:**
- ✅ **분산 락**: 동시 입찰 처리 (Redisson)
- ✅ **세션 저장**: JWT Refresh Token
- ✅ **캐시**: 상품 조회 성능 향상
- ✅ **Pub/Sub**: 실시간 알림 (WebSocket 연동)

**비즈니스 요구사항 연결:**
- **실시간 입찰**: 동시성 제어 (분산 락)
- **입찰 순위**: Sorted Set으로 실시간 순위
- **조회수 카운팅**: Atomic Increment

##### 3. **Elasticsearch 9.2.3**
```yaml
elasticsearch:
  image: docker.elastic.co/elasticsearch/elasticsearch:9.2.3
  environment:
    - discovery.type=single-node
    - xpack.security.enabled=false  # 로컬용
    - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
```

**선택 이유:**
- ✅ **9.2.3 LTS**: Spring Data Elasticsearch 6.0.x 호환
- ✅ **형태소 분석**: 한글 검색 최적화 (nori)
- ✅ **자동완성**: Completion Suggester
- ✅ **전문 검색**: 제목/설명 검색

**비즈니스 요구사항 연결:**
- 상품명 검색: "맥북", "맥 북" 모두 검색
- 카테고리 필터링
- 가격 범위 검색
- 검색어 자동완성

##### 4. **Kafka + Zookeeper**
```yaml
kafka:
  image: confluentinc/cp-kafka:7.5.0
  environment:
    KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
```

**선택 이유:**
- ✅ **이벤트 기반 아키텍처**: 도메인 간 느슨한 결합
- ✅ **비동기 처리**: 알림, 로그, 분석
- ✅ **MSA 준비**: 향후 마이크로서비스 간 통신

**현재 상태:**
- 🟡 **준비 완료, 당장 사용 안 함**
- 초기엔 Spring Event로 대체
- MSA 전환 시 바로 활용 가능

**비즈니스 요구사항 연결 (미래):**
- 입찰 성공 → 알림 발송 (비동기)
- 결제 완료 → 정산 시스템 트리거
- 사용자 행동 → 분석 시스템

##### 5. **Spring Boot App**
```yaml
app:
  build:
    context: .
    dockerfile: Dockerfile
  environment:
    - SPRING_PROFILES_ACTIVE=local
    - DB_URL=jdbc:postgresql://postgres:5432/fourtune_db
    # ... 모든 환경변수 하드코딩
  ports:
    - "8080:8080"  # API
    - "5005:5005"  # 디버깅
  depends_on:
    postgres: { condition: service_healthy }
    redis: { condition: service_healthy }
    elasticsearch: { condition: service_healthy }
  restart: "no"  # 개발 중 자동 재시작 방지
```

**설계 포인트:**

1. **환경변수 하드코딩**
   - ✅ `.env` 파일 불필요
   - ✅ 팀원 즉시 실행 가능
   - ✅ "환경변수 설정 안 했어요" 문제 제거

2. **Health Check 의존성**
   - ✅ DB 준비 후 앱 시작
   - ✅ "Connection refused" 에러 방지

3. **디버깅 포트 노출**
   - ✅ IntelliJ/VSCode Remote Debug 가능
   - ✅ 중단점(Breakpoint) 사용 가능

4. **restart: "no"**
   - ✅ 개발 중 코드 수정 → 수동 재시작
   - ✅ 의도치 않은 재시작 방지

### 3.3 application.yml (Local Profile)

```yaml
spring:
  profiles:
    active: local
  
  jpa:
    hibernate:
      ddl-auto: update  # 🔑 개발 편의성
```

#### **왜 `ddl-auto: update`인가?**

**장점:**
- ✅ 엔티티 수정 시 자동 스키마 변경
- ✅ 빠른 프로토타이핑
- ✅ 초기 개발 단계에 적합

**주의사항:**
- ⚠️ **프로덕션에서는 절대 사용 금지**
- ⚠️ 데이터 손실 가능성
- ⚠️ 나중에 Flyway/Liquibase로 전환 필요

**권장 사용 시기:**
- ✅ 초기 개발 단계 (현재)
- ✅ 엔티티 설계 변경 빈번
- ❌ 실제 데이터 쌓인 후

### 3.4 로컬 환경 실행 흐름

```bash
# 1. 저장소 클론
git clone <repository>
cd fourtune

# 2. 한 줄로 전체 실행
docker-compose up -d --build

# 3. 로그 확인
docker-compose logs -f app

# 4. 헬스 체크
curl http://localhost:8080/actuator/health
```

**전체 시작 시간: 약 2-3분**

1. 이미지 다운로드 (최초 1회): ~30초
2. PostgreSQL 시작: ~10초
3. Redis 시작: ~5초
4. Elasticsearch 시작: ~30초
5. Kafka 시작: ~20초
6. Spring Boot 빌드 + 시작: ~60초

### 3.5 로컬 환경 장점

#### **1. 팀 협업 효율 극대화**
```
Before (환경 불일치):
- A: "내 PC에선 되는데?"
- B: "PostgreSQL 버전이 달라서 안 돼요"
- C: "Java 17이라 안 되네요"

After (Docker 통일):
- 모두: "docker-compose up -d" → 동일한 환경!
```

#### **2. 신입 팀원 온보딩 5분 완성**
```
1. Docker Desktop 설치
2. 저장소 클론
3. docker-compose up -d
4. 끝!
```

#### **3. 로컬 데이터베이스 충돌 방지**
- ✅ PostgreSQL 로컬 설치 불필요
- ✅ 포트 충돌 시 컨테이너만 중지
- ✅ 여러 프로젝트 동시 개발 가능

---

## 4. 개발 서버 환경 (Dev)

### 4.1 설계 목표

> **"프로덕션과 유사하지만 실험 가능한 안전한 통합 테스트 환경"**

#### 핵심 원칙
1. **Production-like**: 프로덕션과 최대한 유사
2. **Team Shared**: 팀 전체가 공유
3. **Integration Testing**: 통합 테스트 환경
4. **Flexible**: 실험 가능, 장애 허용

### 4.2 Docker Compose 구성 (`docker-compose.dev.yml`)

#### **로컬과의 주요 차이점**

| 항목 | 로컬 | 개발 서버 |
|------|------|----------|
| **Nginx** | ❌ 없음 | ✅ 포함 (HTTP) |
| **환경변수** | 하드코딩 | `.env.dev` 파일 |
| **재시작 정책** | `no` | `unless-stopped` |
| **리소스 제한** | 없음 | 없음 (서버 스펙 여유) |
| **컨테이너명** | `fourtune-*` | `fourtune-*-dev` |
| **볼륨명** | `*_data` | `*_data_dev` |

#### **Nginx 추가 이유**

```yaml
nginx:
  image: nginx:alpine
  ports:
    - "80:80"
  volumes:
    - ./nginx/nginx.dev.conf:/etc/nginx/nginx.conf:ro
```

**왜 개발 서버부터 Nginx를 사용하나?**

1. **프로덕션 환경 시뮬레이션**
   - 로컬: 개발자 편의
   - 개발 서버: 실제 환경 테스트
   - 프로덕션: 실제 운영

2. **Reverse Proxy 테스트**
   - API 라우팅 (`/api/`)
   - WebSocket 프록시 (`/ws/`)
   - Health check 엔드포인트

3. **Rate Limiting 검증**
   ```nginx
   limit_req_zone $binary_remote_addr zone=api_limit:10m rate=50r/s;
   ```
   - 프로덕션: 10 req/s
   - 개발: 50 req/s (테스트 편의)

4. **CORS/보안 헤더 테스트**
   - 프론트엔드 팀과 통합 테스트
   - CORS 정책 검증

#### **Nginx Dev 설정 특징**

```nginx
# nginx.dev.conf
server {
    listen 80;  # HTTPS 없음 (SSL 인증서 비용 절감)
    server_name _;  # 모든 도메인 허용 (IP 접속)
    
    # 개발용 느슨한 Rate Limiting
    limit_req zone=api_limit burst=100 nodelay;
    
    location /api/ {
        proxy_pass http://backend;  # app:8080
        # 헤더 설정...
    }
}
```

**왜 HTTP만?**
- ✅ SSL 인증서 비용 절약
- ✅ Let's Encrypt는 도메인 필요 (IP는 불가)
- ✅ 개발 단계에서 HTTPS 불필요
- ✅ 프로덕션 전환 시 SSL만 추가

### 4.3 환경변수 관리 (`.env.dev`)

#### **왜 개발 서버부터 환경변수 파일?**

**로컬:**
```yaml
environment:
  - DB_PASSWORD=fourtune  # 하드코딩 OK
```

**개발 서버:**
```yaml
environment:
  - DB_PASSWORD=${DB_PASSWORD}  # 파일에서 로드
```

**이유:**
1. **보안**: 개발 서버는 팀 외부에 노출 가능
2. **비밀번호 강도**: 로컬(간단) vs 개발(강력)
3. **Git 커밋 방지**: `.env.dev`는 `.gitignore`에 등록

#### **환경변수 생성 예시**

```bash
# 개발 서버 접속
ssh ubuntu@dev-server

# 템플릿 복사
cd fourtune
cp env.template .env.dev

# 강력한 비밀번호 생성
openssl rand -base64 32  # DB_PASSWORD
openssl rand -base64 64  # JWT_SECRET
openssl rand -base64 32 | cut -c1-32  # ENCRYPTION_KEY

# .env.dev 편집
nano .env.dev
```

### 4.4 개발 서버 인프라 (NCP)

#### **왜 네이버 클라우드 플랫폼(NCP)?**

| 클라우드 | 장점 | 단점 | 선택 이유 |
|----------|------|------|----------|
| **NCP** | 한국 리전, 빠름 | 유료 (저렴) | ✅ **선택** |
| Oracle Cloud | 무료 | 느림, 불안정 | ❌ 개발 중 중단 |
| AWS | 강력, 안정 | 비쌈 | 프로덕션용 |

**권장 스펙:**
- **서버**: Compact 또는 Micro
- **CPU**: 2 vCPU
- **RAM**: 4GB
- **스토리지**: 50GB SSD
- **비용**: ~월 10,000원

#### **포트 설정 (ACG - Access Control Group)**

| 포트 | 서비스 | 외부 노출 | 용도 |
|------|--------|----------|------|
| 22 | SSH | ✅ (개발팀만) | 서버 관리 |
| 80 | Nginx | ✅ (전체) | HTTP API |
| 5432 | PostgreSQL | ✅ (개발팀만) | DBeaver 연결 |
| 8080 | Spring Boot | ❌ (내부만) | Nginx 프록시 |
| 6379 | Redis | ❌ (내부만) | 보안 |
| 9200 | Elasticsearch | ❌ (내부만) | 보안 |

**보안 설정:**
```
Source CIDR:
- SSH (22): 회사 IP only (예: 123.45.67.0/24)
- HTTP (80): 0.0.0.0/0 (전체 허용)
- PostgreSQL (5432): 개발팀 IP only
```

### 4.5 개발 서버 배포 프로세스

```bash
# 1. 서버 접속
ssh ubuntu@<dev-server-ip>

# 2. 코드 업데이트 (Git 사용)
cd fourtune
git pull origin develop

# 3. 환경변수 확인
cat .env.dev  # 민감정보 확인

# 4. Docker Compose 재배포
docker-compose -f docker-compose.dev.yml down
docker-compose -f docker-compose.dev.yml up -d --build

# 5. 로그 확인
docker-compose -f docker-compose.dev.yml logs -f app

# 6. 헬스 체크
curl http://localhost/actuator/health
```

### 4.6 개발 서버 사용 시나리오

#### **1. 프론트엔드 팀 연동**
```javascript
// 프론트엔드 .env
REACT_APP_API_URL=http://<dev-server-ip>/api
```

#### **2. 모바일 앱 개발**
```kotlin
// Android
const val BASE_URL = "http://<dev-server-ip>/api"
```

#### **3. PM/디자이너 검증**
- 브라우저로 직접 접속
- API 테스트 (Postman/Insomnia)

#### **4. 통합 테스트**
- 결제 API 테스트 (Toss Payments 테스트 모드)
- WebSocket 연결 테스트
- 파일 업로드 테스트

---

## 5. 프로덕션 환경 (Prod)

### 5.1 설계 목표

> **"고가용성, 고성능, 고보안의 안정적인 운영 환경"**

#### 핵심 원칙
1. **High Availability**: 무중단 서비스
2. **Security First**: 보안 최우선
3. **Performance**: 최적화된 성능
4. **Monitoring**: 실시간 모니터링
5. **Auto Scaling**: 자동 확장

### 5.2 Docker Compose 구성 (`docker-compose.prod.yml`)

#### **개발 서버와의 주요 차이점**

| 항목 | 개발 서버 | 프로덕션 |
|------|----------|----------|
| **Nginx** | HTTP only | HTTPS + SSL |
| **Rate Limiting** | 50 req/s | 10 req/s |
| **재시작 정책** | `unless-stopped` | `always` |
| **리소스 제한** | ❌ 없음 | ✅ CPU/Memory 제한 |
| **로그 레벨** | DEBUG | WARN |
| **Health Check** | 간단 | 엄격 |
| **DB Connection Pool** | 20 | 30 |
| **Elasticsearch Memory** | 1GB | 2GB |

#### **리소스 제한 설정**

```yaml
app:
  deploy:
    resources:
      limits:
        cpus: '2.0'      # 최대 2 CPU
        memory: 2048M    # 최대 2GB RAM
      reservations:
        cpus: '1.0'      # 최소 1 CPU
        memory: 1024M    # 최소 1GB RAM
```

**왜 리소스 제한?**
1. **비용 관리**: 클라우드 비용 예측 가능
2. **안정성**: 한 서비스가 전체 서버 리소스 독점 방지
3. **Auto Scaling 기준**: 리소스 사용률 기반 스케일링

#### **Nginx 프로덕션 설정**

```nginx
# nginx.prod.conf
server {
    # HTTP → HTTPS 리다이렉트
    listen 80;
    server_name fourtune.com www.fourtune.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name fourtune.com www.fourtune.com;
    
    # SSL 인증서
    ssl_certificate /etc/nginx/ssl/fullchain.pem;
    ssl_certificate_key /etc/nginx/ssl/privkey.pem;
    
    # SSL 최적화
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 10m;
    
    # 보안 헤더
    add_header Strict-Transport-Security "max-age=31536000" always;
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    
    # Rate Limiting (엄격)
    limit_req zone=api_limit burst=20 nodelay;
    
    location /api/ {
        proxy_pass http://backend;
        # ...
    }
}
```

**프로덕션 Nginx 특징:**

1. **HTTPS 강제**
   - HTTP 접속 → HTTPS 리다이렉트
   - HSTS 헤더로 브라우저 강제

2. **SSL 최적화**
   - TLS 1.2/1.3만 허용 (1.0/1.1 차단)
   - 강력한 암호화 알고리즘
   - Session Cache로 성능 향상

3. **보안 헤더**
   - Clickjacking 방지 (`X-Frame-Options`)
   - MIME Sniffing 방지 (`X-Content-Type-Options`)
   - XSS 방지 (`X-XSS-Protection`)

4. **엄격한 Rate Limiting**
   - 10 req/s (개발 서버의 1/5)
   - burst=20 (순간 트래픽 허용)
   - DDoS 공격 방지

### 5.3 SSL 인증서 설정

#### **Let's Encrypt (무료 SSL)**

```bash
# Certbot 설치
sudo apt update
sudo apt install certbot python3-certbot-nginx

# 인증서 발급
sudo certbot --nginx -d fourtune.com -d www.fourtune.com

# 자동 갱신 (90일마다)
sudo certbot renew --dry-run
```

**인증서 위치:**
```
/etc/letsencrypt/live/fourtune.com/
├── fullchain.pem   # 인증서 체인
└── privkey.pem     # 개인 키
```

**Docker Volume 마운트:**
```yaml
nginx:
  volumes:
    - /etc/letsencrypt:/etc/nginx/ssl:ro
```

### 5.4 데이터베이스 최적화

#### **Connection Pool 튜닝**

```yaml
# application.yml (prod profile)
spring:
  datasource:
    hikari:
      maximum-pool-size: 30        # 동시 접속자 수 고려
      connection-timeout: 30000    # 30초
      idle-timeout: 600000         # 10분
      max-lifetime: 1800000        # 30분
```

**계산 공식:**
```
Connection Pool Size = (CPU 코어 수 × 2) + Disk 개수
예: 4 코어 + 1 SSD = (4 × 2) + 1 = 9개 (최소)
실제: 30개 (여유 확보)
```

#### **쿼리 최적화**

```yaml
spring:
  jpa:
    properties:
      hibernate:
        default_batch_fetch_size: 100  # N+1 문제 해결
        jdbc.batch_size: 20           # Batch Insert 성능
        order_inserts: true           # Insert 순서 최적화
        order_updates: true           # Update 순서 최적화
```

#### **인덱스 전략** (추후 적용)

```sql
-- 경매 조회 최적화
CREATE INDEX idx_auction_status_created 
  ON auction(status, created_at DESC);

-- 입찰 조회 최적화
CREATE INDEX idx_bid_auction_created 
  ON bid(auction_id, created_at DESC);

-- 사용자 조회 최적화
CREATE INDEX idx_user_email ON user(email);
```

### 5.5 Redis 캐시 전략

#### **캐시 레이어**

```
┌─────────────┐
│   Client    │
└─────────────┘
       │
       ▼
┌─────────────┐
│  Controller │
└─────────────┘
       │
       ▼
┌─────────────────────────────┐
│  Cache (Redis)              │
│  - TTL: 5분~1시간           │
│  - 상품 정보, 사용자 프로필  │
└─────────────────────────────┘
       │ (Cache Miss)
       ▼
┌─────────────┐
│  Database   │
└─────────────┘
```

**캐시 대상:**
1. **상품 상세 정보**: TTL 10분
2. **사용자 프로필**: TTL 1시간
3. **카테고리 목록**: TTL 1일
4. **인기 검색어**: TTL 1시간

**캐시 회피 대상:**
1. **실시간 입찰 정보**: 분산 락만 사용
2. **결제 정보**: 캐시 금지 (보안)
3. **정산 데이터**: 정확성 우선

### 5.6 Elasticsearch 최적화

#### **프로덕션 설정**

```yaml
elasticsearch:
  environment:
    - "ES_JAVA_OPTS=-Xms2g -Xmx2g"  # Heap 2GB (서버 RAM의 50%)
    - cluster.name=fourtune-prod
    - bootstrap.memory_lock=true
```

**인덱스 설정 (추후 적용):**

```json
{
  "settings": {
    "number_of_shards": 2,
    "number_of_replicas": 1,
    "analysis": {
      "analyzer": {
        "nori": {
          "type": "custom",
          "tokenizer": "nori_tokenizer"
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "title": { "type": "text", "analyzer": "nori" },
      "price": { "type": "integer" },
      "category": { "type": "keyword" }
    }
  }
}
```

### 5.7 모니터링 및 알림 (추후 구축)

#### **Prometheus + Grafana**

```yaml
# docker-compose.prod.yml (추가 예정)
prometheus:
  image: prom/prometheus
  volumes:
    - ./prometheus.yml:/etc/prometheus/prometheus.yml

grafana:
  image: grafana/grafana
  ports:
    - "3000:3000"
```

**모니터링 지표:**
- CPU/메모리 사용률
- API 응답 시간
- 데이터베이스 쿼리 시간
- Redis 히트율
- 에러율 (4xx, 5xx)

#### **알림 설정**

```yaml
# Slack/Discord 웹훅 연동
alerts:
  - name: HighErrorRate
    condition: error_rate > 5%
    action: Send Slack notification
  
  - name: SlowResponse
    condition: response_time > 3s
    action: Send Discord notification
```

---

## 6. 기술 스택 선정 이유

### 6.1 언어 및 프레임워크

#### **Java 25**
- ✅ **최신 LTS**: 2024년 9월 출시
- ✅ **성능 향상**: Virtual Threads (Project Loom)
- ✅ **Pattern Matching**: 코드 간결성
- ✅ **Records**: DTO 작성 편의

#### **Spring Boot 4.0.1**
- ✅ **Jakarta EE 호환**: 표준 준수
- ✅ **Native Image**: GraalVM 지원
- ✅ **성능 개선**: 부팅 속도 향상
- ✅ **의존성 관리**: Spring 6.x 기반

### 6.2 데이터베이스

#### **PostgreSQL 16 vs MySQL 8**

| 항목 | PostgreSQL | MySQL | 선택 |
|------|-----------|-------|------|
| **ACID** | 완벽 지원 | 제한적 (InnoDB) | ✅ PostgreSQL |
| **JSON** | 강력 (JSONB) | 약함 | ✅ PostgreSQL |
| **GIS** | PostGIS | 약함 | ✅ PostgreSQL |
| **Full-Text** | 강력 | 약함 | ✅ PostgreSQL |
| **라이선스** | MIT (오픈소스) | GPL (제한적) | ✅ PostgreSQL |

**비즈니스 요구사항 연결:**
- **트랜잭션 무결성**: 결제/정산 시스템
- **JSON 저장**: 결제 응답, 메타데이터
- **복잡한 쿼리**: 정산 계산, 통계

### 6.3 캐시 및 검색

#### **Redis 7 vs Memcached**

| 항목 | Redis | Memcached | 선택 |
|------|-------|-----------|------|
| **데이터 구조** | List, Set, Sorted Set | Key-Value만 | ✅ Redis |
| **지속성** | AOF, RDB | 없음 | ✅ Redis |
| **분산 락** | RedLock | 불가 | ✅ Redis |
| **Pub/Sub** | 지원 | 불가 | ✅ Redis |

**비즈니스 요구사항 연결:**
- **실시간 입찰**: Sorted Set으로 순위
- **동시성 제어**: 분산 락
- **실시간 알림**: Pub/Sub

#### **Elasticsearch 9 vs Solr**

| 항목 | Elasticsearch | Solr | 선택 |
|------|--------------|------|------|
| **학습 곡선** | 쉬움 | 어려움 | ✅ Elasticsearch |
| **RESTful API** | 완벽 | 제한적 | ✅ Elasticsearch |
| **한글 분석** | Nori 플러그인 | 복잡 | ✅ Elasticsearch |
| **커뮤니티** | 활발 | 약함 | ✅ Elasticsearch |

### 6.4 메시징

#### **Kafka vs RabbitMQ**

| 항목 | Kafka | RabbitMQ | 선택 |
|------|-------|----------|------|
| **처리량** | 초당 100만 | 초당 1만 | ✅ Kafka |
| **지속성** | 디스크 저장 | 메모리 | ✅ Kafka |
| **스케일링** | 수평 확장 우수 | 제한적 | ✅ Kafka |
| **MSA** | 표준 | 가능 | ✅ Kafka |

**현재 상태:**
- 🟡 **준비 완료, 사용 안 함**
- 초기엔 Spring Event
- MSA 전환 시 활용

---

## 7. 보안 설계

### 7.1 인증 및 인가

#### **JWT 토큰 전략**

```
┌─────────────┐
│   Client    │
└─────────────┘
       │
       │ POST /api/auth/login
       ▼
┌─────────────┐
│   Server    │
│             │
│ 1. 비밀번호 검증 (BCrypt)
│ 2. Access Token 발급 (1시간)
│ 3. Refresh Token 발급 (2주)
│    └─> Redis 저장
└─────────────┘
       │
       ▼
┌─────────────┐
│ Redis       │
│ refresh:    │
│ {token_id}  │
└─────────────┘
```

**Access Token (1시간):**
```json
{
  "sub": "user_id",
  "roles": ["USER", "SELLER"],
  "exp": 1704067200
}
```

**Refresh Token (2주):**
- Redis에 저장 (휘발성)
- 로그아웃 시 즉시 삭제
- Rotation: 갱신 시 새 토큰 발급

### 7.2 데이터 암호화

#### **민감정보 암호화 (AES-256-GCM)**

```java
@Entity
public class Payment {
    @Convert(converter = CreditCardEncryptor.class)
    private String cardNumber;  // DB에는 암호화된 값 저장
}
```

**암호화 대상:**
- ✅ 신용카드 번호
- ✅ 계좌번호
- ✅ 주민등호 뒷자리 (선택사항)
- ✅ 개인정보 (주소, 연락처)

**암호화 제외:**
- ❌ 이메일 (검색 필요)
- ❌ 사용자명 (조회 필요)
- ❌ 비밀번호 (해싱으로 처리)

### 7.3 API 보안

#### **Rate Limiting**

```nginx
# 프로덕션
limit_req_zone $binary_remote_addr zone=api_limit:10m rate=10r/s;
limit_req_zone $binary_remote_addr zone=login_limit:10m rate=5r/m;

location /api/auth/login {
    limit_req zone=login_limit burst=3 nodelay;
}
```

**로그인 Brute Force 방지:**
- 1분에 5번 시도 제한
- 3번 연속 실패 시 5분 차단 (Redis)
- 10번 실패 시 계정 잠금 (DB)

#### **CORS 설정**

```java
@Configuration
public class SecurityConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
            "https://fourtune.com",
            "https://www.fourtune.com"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        config.setAllowCredentials(true);
        return source;
    }
}
```

### 7.4 데이터베이스 보안

#### **SQL Injection 방지**

```java
// ❌ 위험: 직접 쿼리 작성
String query = "SELECT * FROM user WHERE email = '" + email + "'";

// ✅ 안전: PreparedStatement 또는 JPA
User user = userRepository.findByEmail(email);

// ✅ 안전: QueryDSL
QUser user = QUser.user;
queryFactory.selectFrom(user)
    .where(user.email.eq(email))
    .fetchOne();
```

#### **Database 권한 분리**

```sql
-- 애플리케이션용 계정 (제한된 권한)
CREATE USER fourtune_user WITH PASSWORD 'strong-password';
GRANT SELECT, INSERT, UPDATE, DELETE 
  ON ALL TABLES IN SCHEMA public TO fourtune_user;

-- DDL 권한 제외 (DROP, TRUNCATE 금지)
```

---

## 8. 성능 최적화

### 8.1 데이터베이스 쿼리 최적화

#### **N+1 문제 해결**

```java
// ❌ N+1 발생
List<Auction> auctions = auctionRepository.findAll();
auctions.forEach(auction -> {
    auction.getSeller().getName();  // N번 쿼리
});

// ✅ Fetch Join
@Query("SELECT a FROM Auction a JOIN FETCH a.seller")
List<Auction> findAllWithSeller();

// ✅ Batch Fetch
@BatchSize(size = 100)
@OneToMany(mappedBy = "auction")
private List<Bid> bids;
```

#### **Pagination**

```java
// ❌ 전체 조회
List<Auction> all = auctionRepository.findAll();

// ✅ Pageable
Page<Auction> page = auctionRepository.findAll(
    PageRequest.of(0, 20, Sort.by("createdAt").descending())
);
```

### 8.2 캐시 전략

#### **Look-Aside Cache**

```java
@Service
public class AuctionService {
    @Cacheable(value = "auction", key = "#id")
    public Auction getAuction(Long id) {
        return auctionRepository.findById(id)
            .orElseThrow();
    }
    
    @CacheEvict(value = "auction", key = "#auction.id")
    public void updateAuction(Auction auction) {
        auctionRepository.save(auction);
    }
}
```

**캐시 TTL 전략:**
- 자주 변경: 5분
- 가끔 변경: 1시간
- 거의 변경 없음: 1일

### 8.3 비동기 처리

#### **Spring Event (현재)**

```java
// 입찰 성공 → 알림 발송 (비동기)
@Async
@EventListener
public void handleBidSuccess(BidSuccessEvent event) {
    notificationService.sendBidNotification(event);
}
```

#### **Kafka (미래)**

```java
// Producer
kafkaTemplate.send("bid-success", bidSuccessEvent);

// Consumer
@KafkaListener(topics = "bid-success")
public void handleBidSuccess(BidSuccessEvent event) {
    notificationService.sendBidNotification(event);
}
```

---

## 9. 모니터링 및 로깅

### 9.1 로그 전략

#### **로그 레벨 설정**

| 환경 | Root | Application | SQL |
|------|------|-------------|-----|
| Local | INFO | DEBUG | DEBUG |
| Dev | INFO | DEBUG | DEBUG |
| Prod | WARN | INFO | OFF |

#### **구조화된 로깅 (JSON)**

```json
{
  "timestamp": "2026-01-08T10:30:00Z",
  "level": "ERROR",
  "logger": "com.fourtune.auction.BidService",
  "message": "입찰 처리 실패",
  "context": {
    "userId": 123,
    "auctionId": 456,
    "bidAmount": 100000
  },
  "exception": "..."
}
```

### 9.2 Health Check

#### **Spring Actuator**

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  health:
    defaults:
      enabled: true
    db:
      enabled: true
    redis:
      enabled: true
```

**엔드포인트:**
- `/actuator/health`: 전체 상태
- `/actuator/health/liveness`: K8s liveness probe
- `/actuator/health/readiness`: K8s readiness probe

---

## 10. 트러블슈팅

### 10.1 흔한 문제 해결

#### **문제 1: PostgreSQL 연결 실패**

```
Connection refused: localhost:5432
```

**원인:**
- Docker 네트워크 이슈
- 서비스명 vs localhost

**해결:**
```yaml
# ❌ 틀림
DB_URL=jdbc:postgresql://localhost:5432/fourtune_db

# ✅ 정답
DB_URL=jdbc:postgresql://postgres:5432/fourtune_db
```

#### **문제 2: Elasticsearch 호환성**

```
Invalid media-type value on headers
```

**원인:**
- Spring Data Elasticsearch 버전 불일치

**해결:**
- Spring Boot 4.0.1 → Spring Data Elasticsearch 6.0.x
- Elasticsearch 9.2.3 LTS 사용

#### **문제 3: Out Of Memory (Docker)**

```
java.lang.OutOfMemoryError: Java heap space
```

**원인:**
- Docker 컨테이너 메모리 부족

**해결:**
```dockerfile
ENTRYPOINT ["java", \
  "-XX:MaxRAMPercentage=75.0", \  # 컨테이너 메모리의 75%
  "-jar", "app.jar"]
```

### 10.2 성능 디버깅

#### **느린 쿼리 찾기**

```yaml
# application.yml
logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

#### **Redis 캐시 히트율 확인**

```bash
docker exec fourtune-redis redis-cli INFO stats
# keyspace_hits / (keyspace_hits + keyspace_misses)
```

---

## 📊 환경별 체크리스트

### ✅ 로컬 환경 준비
- [ ] Docker Desktop 설치
- [ ] Git 저장소 클론
- [ ] `docker-compose up -d` 실행
- [ ] `http://localhost:8080/actuator/health` 확인

### ✅ 개발 서버 준비
- [ ] NCP/AWS 서버 생성
- [ ] SSH 키 설정
- [ ] Docker, Docker Compose 설치
- [ ] `.env.dev` 파일 생성
- [ ] ACG 포트 설정 (22, 80, 5432)
- [ ] `docker-compose -f docker-compose.dev.yml up -d` 실행

### ✅ 프로덕션 준비
- [ ] 도메인 구매 및 DNS 설정
- [ ] SSL 인증서 발급 (Let's Encrypt)
- [ ] `.env.prod` 파일 생성 (강력한 비밀번호)
- [ ] 모니터링 시스템 구축 (Prometheus + Grafana)
- [ ] 백업 전략 수립 (DB, Redis)
- [ ] CI/CD 파이프라인 구축 (GitHub Actions)
- [ ] 로드 테스트 수행

---

## 🎯 결론

### 핵심 설계 원칙 정리

1. **환경 분리**: 로컬 ≠ 개발 ≠ 프로덕션
2. **팀 협업**: Docker로 환경 통일
3. **점진적 복잡도**: 간단 → 복잡 (Monolithic → MSA)
4. **보안 우선**: 환경변수, 암호화, HTTPS
5. **성능 최적화**: 캐시, 인덱스, Connection Pool
6. **모니터링**: 문제 조기 발견

### 다음 단계

1. **코드 작성 시작** ✅
2. **엔티티 설계** (User, Auction, Bid)
3. **API 개발** (REST + WebSocket)
4. **CI/CD 구축** (GitHub Actions)
5. **MSA 전환 검토** (트래픽 증가 시)

---

**작성자**: Fourtune Backend Team  
**문의**: fourtune-dev@example.com  
**최종 수정**: 2026-01-08


