# 🎯 다음 단계 가이드

> 기본 환경 설정이 완료되었습니다. 이제 실제 개발을 시작할 차례입니다!

## ✅ 완료된 작업

### 환경 설정
- [x] `.gitignore` 설정 (민감정보, 빌드 파일, 로그 등)
- [x] `build.gradle` 의존성 추가 (JWT, QueryDSL, Elasticsearch 등)
- [x] `application.yml` 프로파일 설정 (local, dev, prod)
- [x] `env.template` 환경변수 템플릿

### Docker 인프라
- [x] `docker-compose.yml` - 로컬 개발 환경 (전체 팀 통일)
- [x] `docker-compose.dev.yml` - 개발 서버 환경 (Nginx 포함)
- [x] `docker-compose.prod.yml` - 프로덕션 환경 (SSL, 리소스 제한)
- [x] `Dockerfile` - Multi-stage build
- [x] Nginx 설정 (dev, prod)

### 테스트 완료
- [x] 로컬 Docker 실행 확인 (PostgreSQL, Redis, Elasticsearch, Kafka)
- [x] Spring Boot Health Check 정상 확인
- [x] DBeaver PostgreSQL 연결 확인

### 문서화
- [x] `README.md` - 프로젝트 개요
- [x] `INFRASTRUCTURE_GUIDE.md` - 인프라 상세 가이드 (필독!)
- [x] `PROJECT_STRUCTURE.md` - 코드 구조 가이드
- [x] `QUICK_START.md` - 5분 빠른 시작
- [x] `NEXT_STEPS.md` - 개발 로드맵 (현재 문서)

## 📋 개발 우선순위

### 1순위: 경매 상품 관리 (2.2)

#### 단계별 작업

##### Step 1: Entity 설계 및 구현
```
fourtune/src/main/java/com/fourtune/auction/domain/
├── auction/
│   ├── entity/
│   │   ├── AuctionItem.java          ✏️ 작성 필요
│   │   ├── ItemImage.java            ✏️ 작성 필요
│   │   └── Category.java             ✏️ 작성 필요 (Enum)
│   └── repository/
│       ├── AuctionItemRepository.java    ✏️ 작성 필요
│       └── ItemImageRepository.java      ✏️ 작성 필요
```

**AuctionItem.java 주요 필드:**
```java
- id (Long)
- title (String)
- description (String)
- categoryId (Long/Enum)
- sellerId (Long)
- startPrice (BigDecimal)          // 시작가
- bidUnit (Integer = 1000)         // 입찰 단위
- buyNowPrice (BigDecimal)         // 즉시구매가 (nullable) 🆕
- buyNowEnabled (Boolean = false)  // 즉시구매 가능 여부 🆕
- auctionStartTime (LocalDateTime)
- auctionEndTime (LocalDateTime)
- status (AuctionStatus)           // SCHEDULED, ACTIVE, ENDED, SOLD, SOLD_BY_BUY_NOW
- viewCount (Long)
- watchlistCount (Integer)
- bidCount (Integer)
- currentPrice (BigDecimal)        // 현재가
```

**Category Enum:**
```java
ELECTRONICS,     // 전자기기
CLOTHING,        // 의류
POTTERY,         // 도자기
APPLIANCES,      // 가전제품
BEDDING,         // 침구
BOOKS,           // 도서
COLLECTIBLES,    // 수집품
ETC              // 기타
```

##### Step 2: DTO 설계
```
domain/auction/dto/
├── AuctionItemCreateRequest.java     ✏️ 작성 필요
├── AuctionItemUpdateRequest.java     ✏️ 작성 필요
├── AuctionItemResponse.java          ✏️ 작성 필요
├── AuctionItemDetailResponse.java    ✏️ 작성 필요
└── AuctionItemSearchCondition.java   ✏️ 작성 필요
```

##### Step 3: Service 구현
```
domain/auction/service/
├── AuctionItemService.java           ✏️ 작성 필요
└── AuctionItemQueryService.java      ✏️ 작성 필요 (조회 전용)
```

**주요 메서드:**
- `createAuctionItem()` - 경매 등록
- `updateAuctionItem()` - 경매 수정
- `deleteAuctionItem()` - 경매 삭제
- `getAuctionItems()` - 경매 목록 조회 (페이징, 필터링)
- `getAuctionItemDetail()` - 경매 상세 조회
- `increaseViewCount()` - 조회수 증가 (Redis 활용)

##### Step 4: Controller 구현
```
api/auction/
└── AuctionController.java            ✏️ 작성 필요
```

**API 엔드포인트:**
```
POST   /api/auctions              - 경매 등록
GET    /api/auctions              - 경매 목록 조회
GET    /api/auctions/{id}         - 경매 상세 조회
PUT    /api/auctions/{id}         - 경매 수정
DELETE /api/auctions/{id}         - 경매 삭제
PATCH  /api/auctions/{id}/view    - 조회수 증가
```

##### Step 5: S3 이미지 업로드 구현
```
infrastructure/s3/
├── S3Service.java                    ✏️ 작성 필요
└── dto/
    └── ImageUploadResponse.java      ✏️ 작성 필요
```

##### Step 6: Elasticsearch 검색 구현 (선택사항 - 나중에)
```
infrastructure/elasticsearch/
├── document/
│   └── AuctionDocument.java          ✏️ 나중에 작성
└── repository/
    └── AuctionSearchRepository.java  ✏️ 나중에 작성
```

---

### 2순위: 사용자 관리 및 인증 (2.1)

#### Step 1: User Entity 및 인증 구현
```
domain/user/
├── entity/
│   └── User.java                     ✏️ 작성 필요
├── repository/
│   └── UserRepository.java           ✏️ 작성 필요
├── service/
│   └── UserService.java              ✏️ 작성 필요
└── dto/
    ├── SignupRequest.java            ✏️ 작성 필요
    ├── LoginRequest.java             ✏️ 작성 필요
    └── TokenResponse.java            ✏️ 작성 필요
```

#### Step 2: JWT 인증 구현
```
global/security/jwt/
├── JwtTokenProvider.java             ✏️ 작성 필요
├── JwtAuthenticationFilter.java      ✏️ 작성 필요
└── JwtProperties.java                ✏️ 작성 필요
```

#### Step 3: Spring Security 설정
```
global/config/
└── SecurityConfig.java               ✏️ 작성 필요
```

---

### 3순위: 입찰 + 즉시구매 + 장바구니 시스템 (경매 도메인 내 통합)

> **참고**: 입찰, 즉시구매, 장바구니는 경매 도메인 안에 통합되었습니다 (DDD Aggregate Root)

```
boundedContext/auction/
├── domain/entity/
│   ├── Bid.java                      ✏️ 작성 필요
│   ├── Cart.java                     ✏️ 작성 필요 🆕
│   └── CartItem.java                 ✏️ 작성 필요 🆕
├── port/out/
│   ├── BidRepository.java            ✏️ 작성 필요
│   ├── CartRepository.java           ✏️ 작성 필요 🆕
│   └── CartItemRepository.java       ✏️ 작성 필요 🆕
├── application/service/
│   ├── BidFacade.java                ✏️ 작성 필요
│   ├── BidPlaceUseCase.java          ✏️ 작성 필요 (분산 락 구현)
│   ├── BidCancelUseCase.java         ✏️ 작성 필요
│   ├── BidSupport.java               ✏️ 작성 필요
│   │
│   ├── AuctionBuyNowUseCase.java     ✏️ 작성 필요 🆕
│   │
│   ├── CartFacade.java               ✏️ 작성 필요 🆕
│   ├── CartAddItemUseCase.java       ✏️ 작성 필요 🆕
│   ├── CartRemoveItemUseCase.java    ✏️ 작성 필요 🆕
│   ├── CartQueryUseCase.java         ✏️ 작성 필요 🆕
│   ├── CartBuyNowUseCase.java        ✏️ 작성 필요 🆕
│   └── CartSupport.java              ✏️ 작성 필요 🆕
├── adapter/in/web/
│   ├── ApiV1BidController.java       ✏️ 작성 필요
│   └── ApiV1CartController.java      ✏️ 작성 필요 🆕
└── adapter/out/
    └── BidCacheAdapter.java          ✏️ 작성 필요 (Redis 분산 락)
```

**핵심 기능:**

**입찰 시스템:**
- 분산 락을 이용한 동시 입찰 처리 (Redis)
- 입찰가 검증 (현재가 + 입찰단위 이상)
- 자동 연장 처리 (종료 5분 전 입찰 시 3분 연장)
- WebSocket 실시간 입찰 알림

**즉시구매 시스템:** 🆕
- 즉시구매 가능 여부 검증 (buyNowEnabled = true)
- 즉시구매 시 경매 즉시 종료 (SOLD_BY_BUY_NOW)
- Order 자동 생성
- 이벤트 발행 (AuctionBuyNowEvent)

**장바구니 시스템:** 🆕
- 즉시구매 가능한 경매 상품만 추가 가능
- 경매 종료 시 자동 만료 처리
- 장바구니에서 즉시구매 가능
- 담았을 때 가격 추적 (가격 변동 확인)

**왜 경매 도메인에 통합?**
- 경매와 입찰은 트랜잭션 일관성 필요 (ACID)
- 즉시구매는 경매의 다른 구매 방식 (eBay Buy It Now)
- 장바구니는 즉시구매 전용 관심 목록
- DDD Aggregate 원칙: 하나의 일관성 경계
- 경매 상태와 강하게 결합 (즉시구매 시 경매 종료)

---

## 🛠️ 공통 인프라 구현

### Global Config
```
global/config/
├── JpaConfig.java                    ✏️ 작성 필요
├── RedisConfig.java                  ✏️ 작성 필요
├── ElasticsearchConfig.java          ✏️ 작성 필요
├── KafkaConfig.java                  ✏️ 작성 필요 (나중에)
└── WebMvcConfig.java                 ✏️ 작성 필요
```

### Exception Handling
```
global/exception/
├── GlobalExceptionHandler.java       ✏️ 작성 필요
├── ErrorCode.java                    ✏️ 작성 필요
├── ErrorResponse.java                ✏️ 작성 필요
└── custom/
    ├── BusinessException.java        ✏️ 작성 필요
    ├── EntityNotFoundException.java  ✏️ 작성 필요
    └── UnauthorizedException.java    ✏️ 작성 필요
```

### Utility Classes
```
global/util/
├── EncryptionUtil.java               ✏️ 작성 필요 (AES-256)
├── DateTimeUtil.java                 ✏️ 작성 필요
└── FileUtil.java                     ✏️ 작성 필요
```

### Base Entity
```
global/common/
├── BaseEntity.java                   ✏️ 작성 필요
├── BaseTimeEntity.java               ✏️ 작성 필요
└── ApiResponse.java                  ✏️ 작성 필요
```

---

## 📊 데이터베이스 설계

### ERD 작성 (우선순위 높음!)
- **도구**: dbdiagram.io, ERDCloud, draw.io 등
- **필수 테이블**:
  - users (사용자)
  - auction_items (경매 상품)
  - item_images (상품 이미지)
  - categories (카테고리)
  - bids (입찰)
  - payments (결제)
  - refunds (환불)
  - settlements (정산)
  - notifications (알림)
  - watchlists (관심상품)

### DB Migration (Flyway 또는 Liquibase)
```
src/main/resources/db/migration/
├── V1__init_schema.sql               ✏️ 작성 필요
├── V2__add_auction_tables.sql        ✏️ 작성 필요
└── V3__add_indexes.sql               ✏️ 작성 필요
```

---

## 🧪 테스트 전략

### 단위 테스트
- Service 계층 테스트 작성
- Repository 테스트 작성
- Utility 클래스 테스트

### 통합 테스트
- Controller 통합 테스트
- DB 트랜잭션 테스트
- 외부 API 모킹 테스트

### E2E 테스트 (선택사항)
- 경매 등록 → 입찰 → 낙찰 → 결제 전체 플로우

---

## 🚀 빠른 시작 체크리스트

### 오늘 할 일
- [x] ~~Docker 컨테이너 실행 확인~~ ✅ 완료
- [x] ~~환경변수 설정~~ ✅ 완료 (docker-compose.yml에 기본값 내장)
- [x] ~~애플리케이션 실행 테스트~~ ✅ 완료
- [x] ~~Health Check 확인~~ ✅ 완료

**다음 작업:**
- [ ] ERD 설계 시작
- [ ] User Entity 구현
- [ ] Base Entity 및 공통 설정 구현

### 이번 주 목표
- [ ] ERD 설계 완료
- [ ] User Entity 구현
- [ ] JWT 인증 구현
- [ ] AuctionItem Entity 구현
- [ ] 경매 CRUD API 구현

---

## 📚 참고 자료

### 코드 레퍼런스
- https://github.com/codestates-seb/seb45_main_003
- https://github.com/psihyeong/Zumgo-react-with-springboot

### 기술 문서
- [Spring Boot 4.x](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [QueryDSL](http://querydsl.com/static/querydsl/latest/reference/html/)
- [Elasticsearch Java Client](https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/current/index.html)
- [Redis Lettuce](https://lettuce.io/core/release/reference/)

### PG 연동
- [Toss Payments API](https://docs.tosspayments.com/)
- [Kakao Pay API](https://developers.kakao.com/docs/latest/ko/kakaopay/common)

---

## 💡 개발 팁

### 1. Entity 설계 시 주의사항
- `@CreatedDate`, `@LastModifiedDate` 활용 (BaseTimeEntity)
- 양방향 연관관계는 신중하게 (N+1 문제)
- Enum 타입 적극 활용 (상태 관리)
- 인덱스 설계 필수 (자주 조회되는 컬럼)

### 2. API 설계 원칙
- RESTful 원칙 준수
- 명확한 HTTP 상태 코드 사용
- 공통 응답 형식 (`ApiResponse<T>`)
- Validation 철저히 (Bean Validation)

### 3. 성능 최적화
- Redis 캐싱 전략 수립
- 페이징 처리 필수
- N+1 문제 해결 (Fetch Join, @EntityGraph)
- 조회수는 Redis로 처리 후 배치 업데이트

### 4. 보안
- 모든 민감정보는 암호화
- SQL Injection 방지 (Prepared Statement)
- XSS 방지 (입력값 검증)
- CORS 설정 올바르게

---

## 🎯 마일스톤

### Phase 1 (1-2주)
- [x] 기본 환경 설정
- [ ] ERD 설계
- [ ] 사용자 인증 구현
- [ ] 경매 CRUD 구현

### Phase 2 (2-3주)
- [ ] 입찰 시스템 구현
- [ ] 결제 시스템 구현
- [ ] 알림 시스템 구현

### Phase 3 (1-2주)
- [ ] 검색 기능 구현
- [ ] 정산 시스템 구현
- [ ] 환불 처리 구현

### Phase 4 (1주)
- [ ] 성능 최적화
- [ ] 테스트 코드 작성
- [ ] 문서화
- [ ] 배포

---

## 🆘 막힐 때 체크리스트

1. 에러 로그 확인
   ```bash
   docker-compose logs -f app
   ```
2. Docker 컨테이너 상태 확인
   ```bash
   docker-compose ps
   ```
3. 환경변수 설정 확인
   ```bash
   docker exec fourtune-app-local env | grep DB
   ```
4. DB 연결 상태 확인
   ```bash
   docker exec fourtune-postgres psql -U fourtune_user -d fourtune_db -c "SELECT 1;"
   ```
5. `INFRASTRUCTURE_GUIDE.md` 트러블슈팅 섹션 참고
6. 레퍼런스 프로젝트 코드 확인

---

**이제 개발을 시작하세요! 화이팅! 🚀**

