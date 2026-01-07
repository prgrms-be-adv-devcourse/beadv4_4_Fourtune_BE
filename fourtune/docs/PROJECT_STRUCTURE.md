# 🏗️ Fourtune 프로젝트 구조 (Hexagonal Architecture)

> **아키텍처**: Hexagonal (Ports & Adapters) - MSA 완벽 준비  
> **설계 원칙**: DDD (Domain-Driven Design) + Clean Architecture

---

## 📋 목차

1. [전체 구조 개요](#1-전체-구조-개요)
2. [BoundedContext (도메인 경계)](#2-boundedcontext-도메인-경계)
3. [Shared (도메인 간 공유)](#3-shared-도메인-간-공유)
4. [Global (전역 설정)](#4-global-전역-설정)
5. [Infrastructure (외부 인프라)](#5-infrastructure-외부-인프라)
6. [헥사고날 아키텍처 원칙](#6-헥사고날-아키텍처-원칙)
7. [MSA 전환 전략](#7-msa-전환-전략)

---

## 1. 전체 구조 개요

```
src/main/java/com/fourtune/auction/
│
├── boundedContext/                  # 도메인 경계 (Bounded Context)
│   ├── user/                        # 사용자 도메인
│   ├── auction/                     # 경매 도메인
│   ├── bid/                         # 입찰 도메인
│   ├── payment/                     # 결제 도메인
│   ├── refund/                      # 환불 도메인
│   ├── settlement/                  # 정산 도메인
│   ├── notification/                # 알림 도메인
│   └── watchlist/                   # 관심상품 도메인
│
├── shared/                          # 도메인 간 공유 (Anti-Corruption Layer)
│   ├── user/
│   ├── auction/
│   ├── bid/
│   ├── payment/
│   ├── settlement/
│   └── notification/
│
├── global/                          # 전역 설정 및 공통 기능
│   ├── config/
│   ├── security/
│   ├── exception/
│   ├── util/
│   └── common/
│
└── infrastructure/                  # 외부 인프라 연동 (Shared Infrastructure)
    ├── s3/
    ├── elasticsearch/
    ├── pg/
    ├── redis/
    └── kafka/
```

---

## 2. BoundedContext (도메인 경계)

### 2.1 전체 구조 (모든 도메인 동일)

```
boundedContext/{domain}/
│
├── domain/                          # 도메인 계층 (핵심 비즈니스 로직)
│   ├── entity/                      # 엔티티
│   ├── vo/                          # 값 객체 (Value Object)
│   ├── event/                       # 도메인 이벤트
│   └── service/                     # 도메인 서비스
│
├── application/                     # 애플리케이션 계층 (Use Case)
│   ├── service/                     # 애플리케이션 서비스
│   └── dto/                         # 내부 DTO
│
├── adapter/                         # 어댑터 계층 (외부 세계와 연결)
│   ├── in/                          # Inbound Adapter
│   │   ├── web/                     # REST API Controller
│   │   └── event/                   # Event Listener
│   └── out/                         # Outbound Adapter
│       ├── persistence/             # JPA Repository 구현체
│       └── external/                # 외부 API 호출
│
└── port/                            # 포트 계층 (인터페이스)
    ├── in/                          # Inbound Port (Use Case Interface)
    └── out/                         # Outbound Port (Repository Interface)
```

---

### 2.2 User 도메인 (상세 예시)

```
boundedContext/user/
│
├── domain/                          # 도메인 계층
│   ├── entity/
│   │   └── User.java                # 사용자 엔티티
│   ├── vo/
│   │   ├── Email.java               # 이메일 값 객체
│   │   ├── Password.java            # 비밀번호 값 객체
│   │   └── UserRole.java            # 역할 Enum
│   ├── event/
│   │   ├── UserCreatedEvent.java
│   │   └── UserDeletedEvent.java
│   └── service/
│       └── UserDomainService.java   # 도메인 서비스 (복잡한 비즈니스 로직)
│
├── application/                     # 애플리케이션 계층
│   ├── service/
│   │   ├── UserCommandService.java  # 명령 처리 (CQS)
│   │   └── UserQueryService.java    # 조회 처리 (CQS)
│   └── dto/
│       ├── UserCreateCommand.java
│       ├── UserUpdateCommand.java
│       └── UserInfo.java
│
├── adapter/                         # 어댑터 계층
│   ├── in/
│   │   ├── web/
│   │   │   ├── UserController.java          # REST API
│   │   │   ├── UserRequest.java             # Request DTO
│   │   │   └── UserResponse.java            # Response DTO
│   │   └── event/
│   │       └── UserEventListener.java       # 외부 이벤트 리스너
│   └── out/
│       ├── persistence/
│       │   ├── UserJpaRepository.java       # Spring Data JPA
│       │   ├── UserRepositoryImpl.java      # 포트 구현체
│       │   └── UserEntity.java              # JPA 엔티티 (Mapper 필요)
│       └── external/
│           └── EmailServiceAdapter.java     # 외부 이메일 서비스
│
└── port/                            # 포트 계층
    ├── in/
    │   ├── UserCommandUseCase.java          # 명령 Use Case
    │   └── UserQueryUseCase.java            # 조회 Use Case
    └── out/
        ├── UserRepository.java              # Repository 인터페이스
        └── EmailPort.java                   # 외부 이메일 포트
```

---

### 2.3 Auction 도메인 (경매)

```
boundedContext/auction/
│
├── domain/
│   ├── entity/
│   │   ├── AuctionItem.java
│   │   ├── ItemImage.java
│   │   └── Category.java (Enum)
│   ├── vo/
│   │   ├── Money.java               # 금액 값 객체
│   │   ├── AuctionPeriod.java       # 기간 값 객체
│   │   └── AuctionStatus.java       # 상태 Enum
│   ├── event/
│   │   ├── AuctionCreatedEvent.java
│   │   ├── AuctionClosedEvent.java
│   │   └── AuctionExtendedEvent.java
│   └── service/
│       └── AuctionDomainService.java
│
├── application/
│   ├── service/
│   │   ├── AuctionCommandService.java
│   │   └── AuctionQueryService.java
│   └── dto/
│       ├── AuctionCreateCommand.java
│       └── AuctionInfo.java
│
├── adapter/
│   ├── in/
│   │   ├── web/
│   │   │   ├── AuctionController.java
│   │   │   ├── AuctionRequest.java
│   │   │   └── AuctionResponse.java
│   │   └── scheduler/
│   │       └── AuctionScheduler.java        # 경매 종료 스케줄러
│   └── out/
│       ├── persistence/
│       │   ├── AuctionJpaRepository.java
│       │   └── AuctionRepositoryImpl.java
│       └── search/
│           └── AuctionSearchAdapter.java    # Elasticsearch
│
└── port/
    ├── in/
    │   ├── AuctionCommandUseCase.java
    │   └── AuctionQueryUseCase.java
    └── out/
        ├── AuctionRepository.java
        └── AuctionSearchPort.java
```

---

### 2.4 Bid 도메인 (입찰)

```
boundedContext/bid/
│
├── domain/
│   ├── entity/
│   │   └── Bid.java
│   ├── vo/
│   │   ├── BidAmount.java           # 입찰가 값 객체
│   │   └── BidStatus.java           # 입찰 상태
│   ├── event/
│   │   ├── BidPlacedEvent.java
│   │   └── BidCanceledEvent.java
│   └── service/
│       └── BidDomainService.java    # 입찰 검증 로직
│
├── application/
│   ├── service/
│   │   ├── BidCommandService.java   # 분산 락 처리
│   │   └── BidQueryService.java
│   └── dto/
│       ├── BidPlaceCommand.java
│       └── BidInfo.java
│
├── adapter/
│   ├── in/
│   │   ├── web/
│   │   │   ├── BidController.java
│   │   │   └── BidWebSocketHandler.java  # WebSocket 실시간 입찰
│   │   └── event/
│   │       └── AuctionEventListener.java  # 경매 이벤트 수신
│   └── out/
│       ├── persistence/
│       │   ├── BidJpaRepository.java
│       │   └── BidRepositoryImpl.java
│       └── cache/
│           └── BidCacheAdapter.java       # Redis 분산 락
│
└── port/
    ├── in/
    │   ├── BidCommandUseCase.java
    │   └── BidQueryUseCase.java
    └── out/
        ├── BidRepository.java
        └── DistributedLockPort.java
```

---

### 2.5 Payment 도메인 (결제)

```
boundedContext/payment/
│
├── domain/
│   ├── entity/
│   │   └── Payment.java
│   ├── vo/
│   │   ├── PaymentMethod.java       # 결제 수단 Enum
│   │   └── PaymentStatus.java       # 결제 상태
│   ├── event/
│   │   ├── PaymentCompletedEvent.java
│   │   └── PaymentFailedEvent.java
│   └── service/
│       └── PaymentDomainService.java
│
├── application/
│   ├── service/
│   │   ├── PaymentCommandService.java
│   │   └── PaymentQueryService.java
│   └── dto/
│       ├── PaymentProcessCommand.java
│       └── PaymentInfo.java
│
├── adapter/
│   ├── in/
│   │   ├── web/
│   │   │   ├── PaymentController.java
│   │   │   └── PaymentWebhookController.java  # PG 웹훅
│   │   └── event/
│   │       └── BidEventListener.java          # 입찰 완료 이벤트 수신
│   └── out/
│       ├── persistence/
│       │   ├── PaymentJpaRepository.java
│       │   └── PaymentRepositoryImpl.java
│       └── pg/
│           ├── TossPaymentAdapter.java        # Toss Payments
│           └── KakaoPayAdapter.java           # Kakao Pay
│
└── port/
    ├── in/
    │   ├── PaymentCommandUseCase.java
    │   └── PaymentQueryUseCase.java
    └── out/
        ├── PaymentRepository.java
        └── PaymentGatewayPort.java           # PG 포트
```

---

### 2.6 Settlement 도메인 (정산)

```
boundedContext/settlement/
│
├── domain/
│   ├── entity/
│   │   └── Settlement.java
│   ├── vo/
│   │   ├── SettlementAmount.java    # 정산 금액 (수수료 포함)
│   │   └── SettlementStatus.java
│   ├── event/
│   │   └── SettlementCompletedEvent.java
│   └── service/
│       └── SettlementDomainService.java  # 정산 금액 계산
│
├── application/
│   ├── service/
│   │   ├── SettlementCommandService.java
│   │   └── SettlementQueryService.java
│   └── dto/
│       └── SettlementInfo.java
│
├── adapter/
│   ├── in/
│   │   ├── web/
│   │   │   └── SettlementController.java
│   │   ├── scheduler/
│   │   │   └── SettlementScheduler.java   # 자동 정산
│   │   └── event/
│   │       └── PaymentEventListener.java  # 결제 완료 이벤트
│   └── out/
│       └── persistence/
│           ├── SettlementJpaRepository.java
│           └── SettlementRepositoryImpl.java
│
└── port/
    ├── in/
    │   ├── SettlementCommandUseCase.java
    │   └── SettlementQueryUseCase.java
    └── out/
        └── SettlementRepository.java
```

---

### 2.7 Notification 도메인 (알림)

```
boundedContext/notification/
│
├── domain/
│   ├── entity/
│   │   └── Notification.java
│   ├── vo/
│   │   ├── NotificationType.java    # 알림 유형
│   │   └── NotificationStatus.java
│   └── event/
│       └── NotificationSentEvent.java
│
├── application/
│   ├── service/
│   │   ├── NotificationCommandService.java
│   │   └── NotificationQueryService.java
│   └── dto/
│       └── NotificationInfo.java
│
├── adapter/
│   ├── in/
│   │   ├── web/
│   │   │   ├── NotificationController.java
│   │   │   └── NotificationWebSocketHandler.java  # SSE/WebSocket
│   │   └── event/
│   │       ├── BidEventListener.java
│   │       ├── AuctionEventListener.java
│   │       └── PaymentEventListener.java
│   └── out/
│       ├── persistence/
│       │   ├── NotificationJpaRepository.java
│       │   └── NotificationRepositoryImpl.java
│       └── push/
│           ├── FcmAdapter.java               # Firebase Cloud Messaging
│           └── EmailAdapter.java             # Email 알림
│
└── port/
    ├── in/
    │   ├── NotificationCommandUseCase.java
    │   └── NotificationQueryUseCase.java
    └── out/
        ├── NotificationRepository.java
        └── PushNotificationPort.java
```

---

### 2.8 Watchlist 도메인 (관심상품)

```
boundedContext/watchlist/
│
├── domain/
│   ├── entity/
│   │   └── Watchlist.java
│   └── event/
│       └── WatchlistAddedEvent.java
│
├── application/
│   ├── service/
│   │   └── WatchlistCommandService.java
│   └── dto/
│       └── WatchlistInfo.java
│
├── adapter/
│   ├── in/
│   │   └── web/
│   │       └── WatchlistController.java
│   └── out/
│       └── persistence/
│           ├── WatchlistJpaRepository.java
│           └── WatchlistRepositoryImpl.java
│
└── port/
    ├── in/
    │   └── WatchlistCommandUseCase.java
    └── out/
        └── WatchlistRepository.java
```

---

## 3. Shared (도메인 간 공유)

> **Anti-Corruption Layer**: 도메인 간 결합도를 낮추고 독립성 보장

```
shared/
│
├── user/                            # User 도메인 공유
│   ├── dto/
│   │   ├── UserDto.java             # 외부 노출용 DTO
│   │   └── SellerDto.java
│   ├── event/
│   │   ├── UserCreatedEvent.java    # 도메인 이벤트 (복제)
│   │   └── UserDeletedEvent.java
│   └── port/
│       └── UserReadPort.java        # 조회 전용 포트
│
├── auction/
│   ├── dto/
│   │   ├── AuctionDto.java
│   │   └── AuctionSummaryDto.java
│   ├── event/
│   │   ├── AuctionCreatedEvent.java
│   │   ├── AuctionClosedEvent.java
│   │   └── AuctionExtendedEvent.java
│   └── port/
│       └── AuctionReadPort.java
│
├── bid/
│   ├── dto/
│   │   └── BidDto.java
│   ├── event/
│   │   ├── BidPlacedEvent.java
│   │   └── BidCanceledEvent.java
│   └── port/
│       └── BidReadPort.java
│
├── payment/
│   ├── dto/
│   │   └── PaymentDto.java
│   ├── event/
│   │   ├── PaymentCompletedEvent.java
│   │   └── PaymentFailedEvent.java
│   └── port/
│       └── PaymentReadPort.java
│
├── settlement/
│   ├── dto/
│   │   └── SettlementDto.java
│   └── event/
│       └── SettlementCompletedEvent.java
│
└── notification/
    ├── dto/
    │   └── NotificationDto.java
    └── event/
        └── NotificationSentEvent.java
```

---

## 4. Global (전역 설정)

```
global/
│
├── config/                          # Spring 설정
│   ├── JpaConfig.java
│   ├── RedisConfig.java
│   ├── ElasticsearchConfig.java
│   ├── KafkaConfig.java
│   ├── WebMvcConfig.java
│   ├── SecurityConfig.java
│   └── AsyncConfig.java             # @Async 설정
│
├── security/                        # 인증/인가
│   ├── jwt/
│   │   ├── JwtTokenProvider.java
│   │   ├── JwtAuthenticationFilter.java
│   │   └── JwtProperties.java
│   ├── CustomUserDetailsService.java
│   └── SecurityUtils.java
│
├── exception/                       # 전역 예외 처리
│   ├── GlobalExceptionHandler.java
│   ├── ErrorCode.java
│   ├── ErrorResponse.java
│   └── custom/
│       ├── BusinessException.java
│       ├── EntityNotFoundException.java
│       ├── UnauthorizedException.java
│       ├── InvalidValueException.java
│       └── DomainException.java     # 도메인 예외 Base
│
├── util/                            # 공통 유틸리티
│   ├── EncryptionUtil.java          # AES-256-GCM
│   ├── DateTimeUtil.java
│   ├── StringUtil.java
│   └── FileUtil.java
│
├── event/                           # 전역 이벤트 처리
│   ├── EventPublisher.java          # Spring Event Publisher
│   └── EventConfig.java
│
└── common/                          # 공통 클래스
    ├── BaseEntity.java              # createdAt, updatedAt, createdBy, updatedBy
    ├── BaseTimeEntity.java          # createdAt, updatedAt
    └── ApiResponse.java             # 공통 API 응답 <T>
```

---

## 5. Infrastructure (외부 인프라)

> **Shared Infrastructure**: 여러 도메인이 공유하는 인프라

```
infrastructure/
│
├── s3/                              # AWS S3 파일 저장
│   ├── S3Service.java
│   ├── S3Config.java
│   └── dto/
│       └── ImageUploadResponse.java
│
├── elasticsearch/                   # 검색 엔진
│   ├── document/
│   │   └── AuctionDocument.java     # 검색용 문서
│   ├── repository/
│   │   └── AuctionSearchRepository.java
│   └── service/
│       └── SearchService.java
│
├── pg/                              # PG 결제 연동
│   ├── toss/
│   │   ├── TossPaymentClient.java
│   │   ├── TossPaymentProperties.java
│   │   └── dto/
│   │       ├── TossPaymentRequest.java
│   │       └── TossPaymentResponse.java
│   └── kakao/
│       ├── KakaoPayClient.java
│       ├── KakaoPayProperties.java
│       └── dto/
│           ├── KakaoPayRequest.java
│           └── KakaoPayResponse.java
│
├── redis/                           # Redis 캐싱 및 분산 락
│   ├── RedisService.java
│   ├── DistributedLock.java         # Redisson
│   └── CacheKeyGenerator.java
│
└── kafka/                           # Kafka 이벤트 스트리밍
    ├── producer/
    │   ├── EventProducer.java
    │   └── KafkaProducerConfig.java
    └── consumer/
        ├── EventConsumer.java
        └── KafkaConsumerConfig.java
```

---

## 6. 헥사고날 아키텍처 원칙

### 6.1 의존성 방향

```
외부 세계 (Web, DB, External API)
    ↓
어댑터 (Adapter)
    ↓
포트 (Port - Interface)
    ↓
애플리케이션 (Application - Use Case)
    ↓
도메인 (Domain - 핵심 비즈니스 로직)
```

**핵심 원칙:**
- ✅ **도메인은 외부에 의존하지 않음**
- ✅ **모든 의존성은 내부(도메인)를 향함**
- ✅ **포트(인터페이스)로 추상화**

---

### 6.2 계층별 역할

#### **Domain (도메인)**
- **Entity**: 비즈니스 규칙을 가진 객체
- **VO (Value Object)**: 불변 값 객체
- **Domain Service**: 여러 엔티티를 협업하는 로직
- **Domain Event**: 도메인 내 중요한 사건

**특징:**
- ✅ 외부 의존성 없음 (Pure Java)
- ✅ 비즈니스 로직만 포함
- ✅ 프레임워크 독립적

---

#### **Application (애플리케이션)**
- **Use Case**: 비즈니스 시나리오 (서비스)
- **Command/Query**: CQRS 패턴
- **DTO**: 계층 간 데이터 전달

**특징:**
- ✅ 트랜잭션 관리
- ✅ 포트 호출
- ✅ 도메인 서비스 조합

---

#### **Adapter (어댑터)**
- **Inbound Adapter**: 외부 → 내부 (Controller, EventListener)
- **Outbound Adapter**: 내부 → 외부 (Repository, External API)

**특징:**
- ✅ 프레임워크 의존성 허용
- ✅ 포트 인터페이스 구현
- ✅ 외부 세계와 변환

---

#### **Port (포트)**
- **Inbound Port**: Use Case 인터페이스
- **Outbound Port**: Repository/External 인터페이스

**특징:**
- ✅ 인터페이스만 정의
- ✅ 의존성 역전 (DIP)
- ✅ 테스트 용이 (Mocking)

---

### 6.3 데이터 흐름 예시 (입찰 처리)

```
1. Client
   ↓ HTTP POST /api/bids
2. BidController (Adapter In)
   ↓ BidRequest → BidPlaceCommand 변환
3. BidCommandUseCase (Port In)
   ↓ 인터페이스 호출
4. BidCommandService (Application)
   ↓ 비즈니스 로직 수행
5. BidRepository (Port Out)
   ↓ 인터페이스 호출
6. BidRepositoryImpl (Adapter Out)
   ↓ JPA로 DB 저장
7. Database
```

---

## 7. MSA 전환 전략

### 7.1 현재 (Modular Monolith)

```
fourtune-app.jar
├── boundedContext/user/
├── boundedContext/auction/
├── boundedContext/bid/
└── boundedContext/payment/
```

**장점:**
- ✅ 빠른 개발
- ✅ 단순한 배포
- ✅ 트랜잭션 관리 쉬움

---

### 7.2 Phase 1: 모듈 완전 분리

```
각 BoundedContext 독립성 강화
- Shared 계층 통한 통신만 허용
- 직접 참조 금지
- Event로만 통신
```

---

### 7.3 Phase 2: 멀티 모듈 프로젝트

```
fourtune/
├── fourtune-user/
│   └── src/main/java/.../boundedContext/user/
├── fourtune-auction/
│   └── src/main/java/.../boundedContext/auction/
├── fourtune-bid/
│   └── src/main/java/.../boundedContext/bid/
├── fourtune-payment/
│   └── src/main/java/.../boundedContext/payment/
└── fourtune-common/
    └── src/main/java/.../shared/
```

---

### 7.4 Phase 3: 완전한 MSA

```
fourtune-user-service/       (독립 프로젝트)
fourtune-auction-service/    (독립 프로젝트)
fourtune-bid-service/        (독립 프로젝트)
fourtune-payment-service/    (독립 프로젝트)
```

**추가 구성:**
- Spring Cloud Gateway (API Gateway)
- Eureka Server (Service Discovery)
- Kafka (Event Bus)
- Redis (Distributed Cache/Lock)

---

## 8. 테스트 전략

### 8.1 단위 테스트
- Domain 계층 테스트 (외부 의존성 없음)
- Pure Java 테스트로 빠른 피드백

### 8.2 통합 테스트
- Application Service 테스트
- Port Mocking으로 격리된 테스트
- 비즈니스 로직 검증

### 8.3 E2E 테스트
- Controller부터 Database까지
- Testcontainers 활용
- 실제 시나리오 검증

---

## 9. 실행 방법

### 로컬 환경

```bash
cd fourtune
docker-compose up -d
./gradlew bootRun --args='--spring.profiles.active=local'
```

### 빌드

```bash
./gradlew clean build -x test
```

## 📚 참고 자료

- **DDD**: Eric Evans - Domain-Driven Design
- **Hexagonal Architecture**: Alistair Cockburn
- **Clean Architecture**: Robert C. Martin
- **MSA 전환**: Sam Newman - Building Microservices

---

**✨ 헥사고날 아키텍처로 완벽한 MSA 준비 완료!**

