# 🏆 Fourtune - 실시간 온라인 경매 플랫폼

> 안전하고 신뢰할 수 있는 C2C 경매 거래 플랫폼

[![Java](https://img.shields.io/badge/Java-25-red.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7-red.svg)](https://redis.io/)
[![Elasticsearch](https://img.shields.io/badge/Elasticsearch-9.2-yellow.svg)](https://www.elastic.co/)

## 📌 프로젝트 개요

Fourtune은 개인 간 물품을 경매 방식으로 거래할 수 있는 실시간 온라인 경매 플랫폼입니다.

### 핵심 기능

- 🔨 **실시간 입찰 시스템**: 분산 락 기반의 안전한 동시 입찰 처리
- 💳 **안전한 결제**: PG사 API 연동 및 민감정보 AES-256 암호화
- 🔍 **스마트 검색**: Elasticsearch 기반 형태소 분석 및 자동완성
- 🔔 **실시간 알림**: Spring Event 기반 비동기 알림 시스템
- 💰 **자동 정산**: 스케줄러 기반 판매자 정산 처리
- 🔐 **JWT 인증**: Spring Security + JWT 토큰 기반 인증

## 🛠️ 기술 스택

### Backend
- **Language**: Java 25
- **Framework**: Spring Boot 4.0.1
- **Security**: Spring Security + JWT
- **ORM**: Spring Data JPA + QueryDSL
- **Database**: PostgreSQL 16
- **Cache**: Redis 7
- **Search**: Elasticsearch 9.2
- **Messaging**: Spring Event + **Apache Kafka** (이벤트 드리븐 연동)
- **File Storage**: AWS S3

### Infrastructure
- **Container**: Docker + Docker Compose
- **CI/CD**: GitHub Actions
- **Web Server**: Nginx (예정)

### Architecture
- **현재**: 모노리스(fourtune-api) + **payment-service** 분리 완료. 도메인별 **Bounded Context** 구조 (Hexagonal).
- **진행 중**: 경매 도메인 MSA 분리 (auction-service). 통합 테스트·부하 테스트 환경 구축.

## 🚀 시작하기

### 필수 요구사항

- Java 25
- Docker & Docker Compose
- Gradle 8.x

### 로컬 환경 설정 (팀 개발)

1. **저장소 클론**
```bash
git clone https://github.com/your-org/fourtune.git
cd fourtune
```

2. **Docker로 전체 실행** (한 번에!)
```bash
cd fourtune
docker-compose up -d --build
```

**끝!** 환경변수 파일 불필요 (기본값이 docker-compose.yml에 설정됨)

> 💡 개인 설정이 필요한 경우: `cp env.template .env` 후 수정

3. **로그 확인**
```bash
docker-compose logs -f app
```

4. **접속 확인**
```bash
curl http://localhost:8080/actuator/health
# 또는 브라우저: http://localhost:8080
```

**멀티 모듈 로컬 실행** (Docker 없이): `cd fourtune && ./gradlew :fourtune-api:bootRun`

### 팀 개발 장점
- ✅ 모든 팀원 동일한 환경 (Java 25, PostgreSQL 16 등)
- ✅ "내 PC에선 되는데?" 문제 해결
- ✅ 새 팀원 온보딩: `docker-compose up -d` 끝!

### Docker로 실행

```bash
# Docker 이미지 빌드
docker build -t fourtune:latest ./fourtune

# 컨테이너 실행
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=local \
  fourtune:latest
```

## 📂 프로젝트 구조

```
fourtune/
├── fourtune-api/         # 메인 API (모노리스) — auth, user, auction, payment, settlement, search, watchList, notification
│   └── src/main/java/com/fourtune/auction/
│       └── boundedContext/
│           ├── auth/        # 인증·OAuth2
│           ├── user/        # 사용자
│           ├── auction/    # 경매·입찰·장바구니·주문 (→ auction-service 분리 예정)
│           ├── payment/     # 결제 (payment-service HTTP 연동)
│           ├── settlement/  # 정산
│           ├── search/      # Elasticsearch 검색
│           ├── watchList/   # 관심상품
│           └── notification/# 알림·FCM
├── payment-service/     # 결제 전용 서비스 (MSA 분리 완료)
├── common/               # 공유: 이벤트, DTO, Kafka 프로듀서/매퍼
├── docker-compose.yml
├── docker-compose.dev.yml
├── docker-compose.prod.yml
└── build.gradle / settings.gradle  # 멀티 모듈 (fourtune-api, payment-service, common)
```

### 📖 상세 문서
- ⭐ **경매 도메인 MSA 분리 가이드**: [fourtune/docs/MSA_AUCTION_DOMAIN_GUIDE.md](fourtune/docs/MSA_AUCTION_DOMAIN_GUIDE.md) — 경매 서비스 분리 작업 순서, 의존성, 이벤트 연동

## 🔐 보안

### 암호화 처리
- **비밀번호**: BCrypt 해싱
- **결제 정보**: AES-256-GCM 암호화
- **JWT Secret**: 환경변수로 관리
- **API Keys**: 환경변수로 관리

### 환경변수 관리
모든 민감정보는 환경변수로 관리하며, `.gitignore`에 등록되어 있습니다.

```bash
# 필수 환경변수
JWT_SECRET=your-secret-key
ENCRYPTION_KEY=your-32-char-encryption-key!!
DB_PASSWORD=your-db-password
REDIS_PASSWORD=your-redis-password
```

## 🧪 테스트

```bash
cd fourtune
# 전체 테스트 (멀티 모듈)
./gradlew test

# fourtune-api만 테스트
./gradlew :fourtune-api:test

# 커버리지 리포트 (해당 모듈)
./gradlew :fourtune-api:jacocoTestReport
```

- **통합 테스트**: 경매 → 입찰 → 결제 → 정산 플로우는 각 서비스 테스트 + 이벤트(Kafka) 연동으로 검증. 통합 테스트 환경 구축 진행 중.
- **부하 테스트**: 동시 입찰/결제, Kafka lag, RPS·지연시간·에러율 수집 목표.

## 📊 주요 API 엔드포인트

| 기능 | Method | Endpoint (fourtune-api) |
|------|--------|--------------------------|
| 회원가입/로그인 | POST | `/api/auth/*` |
| 경매 목록/상세/등록 | GET/POST | `/api/v1/auctions/*` |
| 입찰 | POST | `/api/v1/bids/*` |
| 장바구니·즉시구매 | GET/POST | `/api/v1/carts/*`, `/api/v1/orders/*` |
| 결제 | POST | `/api/payments/*` (또는 payment-service) |
| 검색 | GET | `/api/v1/search/*` |

자세한 API 명세: Swagger UI (`/swagger-ui.html`) 또는 OpenAPI (`/v3/api-docs`)

## 🔄 CI/CD

GitHub Actions를 통한 자동화된 배포 파이프라인:

- ✅ **Pull Request**: 자동 테스트 실행
- 🚀 **develop 브랜치**: 개발 서버 자동 배포
- 🎯 **main 브랜치**: 프로덕션 서버 자동 배포

## 📈 로드맵

### Phase 1 (완료/진행)
- [x] 기본 환경 설정, 멀티 모듈 (fourtune-api, payment-service, common)
- [x] 사용자 인증/인가 (JWT, OAuth2)
- [x] 경매·입찰·장바구니·주문
- [x] 결제 연동 (payment-service 분리)
- [x] 검색(Elasticsearch), 관심상품, 알림, 정산
- [x] Kafka 이벤트 연동 (User, Auction, Payment 등)

### Phase 2 (진행 중)
- [ ] **통합 테스트**: 경매 → 입찰 → 결제 → 정산 E2E, 이벤트 흐름·Kafka 소비/재처리 검증
- [ ] **경매 도메인 MSA 분리**: auction-service 모듈 분리 (문서: [MSA_AUCTION_DOMAIN_GUIDE.md](fourtune/docs/MSA_AUCTION_DOMAIN_GUIDE.md))
- [ ] **부하/성능 테스트**: RPS, p95/p99, 에러율, Kafka lag, DB/커넥션 풀 튜닝

### Phase 3 (예정)
- [ ] 환불 처리 고도화, 오토스케일(HPA) 검증
- [ ] 모니터링·운영 체계 정립

## 🤝 기여 가이드

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 라이선스

This project is licensed under the MIT License

## 👥 팀원

- Backend Developer: [Your Name]
- Frontend Developer: [Your Name]

## 📧 문의

프로젝트 관련 문의: fourtune@example.com

---

⭐️ 이 프로젝트가 도움이 되었다면 Star를 눌러주세요!
