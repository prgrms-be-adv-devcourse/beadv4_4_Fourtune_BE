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
- **Messaging**: Spring Event (Kafka 추후 확장 예정)
- **File Storage**: AWS S3

### Infrastructure
- **Container**: Docker + Docker Compose
- **CI/CD**: GitHub Actions
- **Web Server**: Nginx (예정)

### Architecture
- **현재**: Monolithic (도메인별 모듈화)
- **향후**: MSA 전환 고려

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
├── src/main/java/com/fourtune/auction/
│   ├── domain/           # 도메인 계층 (비즈니스 로직)
│   │   ├── user/         # 사용자
│   │   ├── auction/      # 경매
│   │   ├── bid/          # 입찰
│   │   ├── payment/      # 결제
│   │   ├── refund/       # 환불
│   │   ├── settlement/   # 정산
│   │   ├── notification/ # 알림
│   │   └── watchlist/    # 관심상품
│   ├── api/              # API 계층 (컨트롤러)
│   ├── global/           # 전역 설정
│   ├── infrastructure/   # 외부 인프라 연동
│   └── scheduler/        # 스케줄러
├── docker-compose.yml    # Docker 구성 (로컬)
├── docker-compose.dev.yml   # Docker 구성 (개발 서버)
├── docker-compose.prod.yml  # Docker 구성 (프로덕션)
├── Dockerfile           # Docker 이미지
└── build.gradle         # Gradle 빌드 스크립트
```

### 📖 상세 문서
- ⭐ **인프라 구축 가이드**: [INFRASTRUCTURE_GUIDE.md](fourtune/docs/INFRASTRUCTURE_GUIDE.md) - **필독!**
- 🚀 **빠른 시작**: [QUICK_START.md](fourtune/docs/QUICK_START.md) - 5분 안에 시작
- 📂 **프로젝트 구조**: [PROJECT_STRUCTURE.md](fourtune/docs/PROJECT_STRUCTURE.md) - 코드 구조
- 🎯 **다음 단계**: [NEXT_STEPS.md](fourtune/docs/NEXT_STEPS.md) - 개발 로드맵

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
# 전체 테스트
./gradlew test

# 커버리지 리포트 생성
./gradlew jacocoTestReport

# 결과 확인
open build/reports/tests/test/index.html
```

## 📊 주요 API 엔드포인트

| 기능 | Method | Endpoint |
|------|--------|----------|
| 회원가입 | POST | `/api/auth/signup` |
| 로그인 | POST | `/api/auth/login` |
| 경매 목록 | GET | `/api/auctions` |
| 경매 상세 | GET | `/api/auctions/{id}` |
| 경매 등록 | POST | `/api/auctions` |
| 입찰 | POST | `/api/bids` |
| 결제 | POST | `/api/payments` |
| 검색 | GET | `/api/search?q={keyword}` |

자세한 API 명세는 추후 Swagger 또는 별도 문서로 제공 예정

## 🔄 CI/CD

GitHub Actions를 통한 자동화된 배포 파이프라인:

- ✅ **Pull Request**: 자동 테스트 실행
- 🚀 **develop 브랜치**: 개발 서버 자동 배포
- 🎯 **main 브랜치**: 프로덕션 서버 자동 배포

## 📈 로드맵

### Phase 1 (현재)
- [x] 기본 환경 설정
- [ ] 사용자 인증/인가
- [ ] 경매 상품 관리
- [ ] 입찰 시스템

### Phase 2
- [ ] 결제 시스템
- [ ] 알림 시스템
- [ ] 검색 기능
- [ ] 관심상품

### Phase 3
- [ ] 정산 시스템
- [ ] 환불 처리
- [ ] 성능 최적화
- [ ] 모니터링 구축

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
