# 🏗️ Fourtune 프로젝트 구조 (Hexagonal Architecture + Facade Pattern)

> **아키텍처**: Hexagonal (Ports & Adapters) + Facade Pattern  
> **설계 원칙**: DDD (Domain-Driven Design) + Clean Architecture + CQRS

---

## 📋 목차

1. [전체 구조 개요](#1-전체-구조-개요)
2. [BoundedContext (도메인 경계)](#2-boundedcontext-도메인-경계)
3. [계층별 역할 설명](#3-계층별-역할-설명)
4. [Shared (도메인 간 공유)](#4-shared-도메인-간-공유)
5. [Global (전역 설정)](#5-global-전역-설정)
6. [Infrastructure (외부 인프라)](#6-infrastructure-외부-인프라)
7. [아키텍처 원칙](#7-아키텍처-원칙)
8. [MSA 전환 전략](#8-msa-전환-전략)

---

## 1. 전체 구조 개요

```
src/main/java/com/fourtune/auction/
│
├── boundedContext/                  # 도메인 경계 (Bounded Context)
│   ├── user/                        # 사용자 도메인
│   ├── auction/                     # 경매 + 입찰 + 주문 도메인 (Aggregate Root)
│   ├── payment/                     # 결제 도메인 (Toss Payments 연동)
│   ├── refund/                      # 환불 도메인
│   ├── settlement/                  # 정산 도메인
│   ├── notification/                # 알림 도메인
│   └── watchlist/                   # 관심상품 도메인
│
├── shared/                          # 도메인 간 공유 (Anti-Corruption Layer)
│   ├── user/
│   ├── auction/                     # 경매, 입찰, 주문 DTO & Event
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

### 2.1 전체 구조 (Facade 패턴 적용)

```
boundedContext/{domain}/
│
├── app/                                    # Application 계층 (Facade + UseCase)
│   ├── {Domain}Facade.java                 # 🎯 여러 UseCase 조합, 복잡한 비즈니스 플로우 조율
│   ├── {Domain}Support.java                # 공통 조회/검증 기능 (여러 UseCase에서 재사용)
│   ├── {Domain}CreateXxxUseCase.java       # 생성 UseCase (단일 책임)
│   ├── {Domain}UpdateXxxUseCase.java       # 수정 UseCase (단일 책임)
│   ├── {Domain}DeleteXxxUseCase.java       # 삭제 UseCase (단일 책임)
│   └── {Domain}QueryXxxUseCase.java        # 조회 UseCase (단일 책임)
│
├── domain/                                 # 도메인 계층 (핵심 비즈니스 로직)
│   ├── {Entity}.java                       # 엔티티 (비즈니스 규칙 포함)
│   ├── {Entity}Policy.java                 # 정책 클래스 (비즈니스 규칙, 상수)
│   └── {ValueObject}.java                  # 값 객체 (불변 객체)
│
├── in/                                     # Inbound Adapter (외부 → 내부)
│   ├── ApiV1{Domain}Controller.java        # REST API 컨트롤러 (Facade 호출)
│   ├── {Domain}EventListener.java          # 이벤트 리스너 (다른 도메인 이벤트 수신)
│   ├── {Domain}Scheduler.java              # 스케줄러 (정기 작업)
│   └── {Domain}DataInit.java               # 초기 데이터 설정 (개발/테스트용)
│
└── out/                                    # Outbound Adapter (내부 → 외부)
    ├── {Entity}Repository.java             # JPA Repository (데이터 저장/조회)
    └── {External}Adapter.java              # 외부 API 어댑터 (외부 서비스 호출)
```

---

### 2.2 User 도메인 (사용자 관리)

```
boundedContext/user/
│
├── app/
│   ├── UserFacade.java                     # 회원가입, 로그인 등 복잡한 사용자 플로우 조율
│   ├── UserSupport.java                    # 사용자 조회, 검증 공통 기능
│   ├── UserJoinUseCase.java                # 회원가입 처리
│   ├── UserLoginUseCase.java               # 로그인 처리
│   ├── UserUpdateProfileUseCase.java       # 프로필 수정
│   ├── UserDeleteAccountUseCase.java       # 계정 삭제
│   ├── UserQueryUseCase.java               # 사용자 조회
│   └── UserChangePasswordUseCase.java      # 비밀번호 변경
│
├── domain/
│   ├── User.java                           # 사용자 엔티티 (이메일, 비밀번호, 역할 등)
│   ├── UserPolicy.java                     # 비밀번호 규칙, 권한 정책
│   ├── Email.java                          # 이메일 값 객체 (유효성 검증 포함)
│   ├── Password.java                       # 비밀번호 값 객체 (암호화 로직 포함)
│   └── UserRole.java                       # 역할 Enum (USER, SELLER, ADMIN)
│
├── in/
│   ├── ApiV1UserController.java            # 사용자 REST API (회원가입, 로그인, 프로필)
│   ├── UserEventListener.java              # 외부 이벤트 수신 (예: 결제 완료 시 포인트 적립)
│   └── UserDataInit.java                   # 테스트용 사용자 데이터 생성
│
└── out/
    ├── UserRepository.java                 # 사용자 데이터 저장/조회
    └── EmailServiceAdapter.java            # 이메일 발송 어댑터 (환영 메일, 인증 메일)
```

---

### 2.3 Auction 도메인 (경매 + 입찰 + 주문 + 즉시구매 + 장바구니 관리)

```
boundedContext/auction/
│
├── application/service/                    # Application 계층
│   │
│   ├── AuctionFacade.java                  # 경매 생성, 종료, 연장 등 복잡한 플로우 조율
│   ├── AuctionSupport.java                 # 경매 조회, 검증 공통 기능
│   ├── AuctionCreateUseCase.java           # 경매 생성 (이미지 업로드 포함)
│   ├── AuctionUpdateUseCase.java           # 경매 정보 수정
│   ├── AuctionDeleteUseCase.java           # 경매 삭제
│   ├── AuctionCloseUseCase.java            # 경매 종료 처리 (낙찰자 결정 + Order 생성)
│   ├── AuctionExtendUseCase.java           # 경매 자동 연장
│   ├── AuctionQueryUseCase.java            # 경매 조회 (상세, 목록)
│   ├── AuctionSearchUseCase.java           # 경매 검색 (Elasticsearch)
│   ├── AuctionBuyNowUseCase.java           # 즉시구매 처리 (경매 즉시 종료)
│   │
│   ├── BidFacade.java                      # 입찰 처리, 취소 등 복잡한 플로우 조율
│   ├── BidSupport.java                     # 입찰 조회, 검증 공통 기능
│   ├── BidPlaceUseCase.java                # 입찰하기 (분산 락 적용, 동시성 제어)
│   ├── BidCancelUseCase.java               # 입찰 취소
│   ├── BidQueryUseCase.java                # 입찰 내역 조회
│   ├── BidValidateUseCase.java             # 입찰 검증 (최소 금액, 자격 등)
│   │
│   ├── OrderCreateUseCase.java             # 주문 생성 (낙찰 시 or 즉시구매 시)
│   ├── OrderQueryUseCase.java              # 주문 조회
│   ├── OrderCompleteUseCase.java           # 주문 완료 처리 (결제 완료 후)
│   ├── OrderSupport.java                   # 주문 조회, 검증 공통 기능
│   │
│   ├── CartFacade.java                     # 장바구니 추가, 제거, 즉시구매 플로우 조율
│   ├── CartSupport.java                    # 장바구니 조회, 검증 공통 기능
│   ├── CartAddItemUseCase.java             # 장바구니에 경매 상품 추가
│   ├── CartRemoveItemUseCase.java          # 장바구니에서 상품 제거
│   ├── CartQueryUseCase.java               # 장바구니 조회
│   └── CartBuyNowUseCase.java              # 장바구니에서 즉시구매 처리
│
├── domain/
│   ├── entity/
│   │   ├── AuctionItem.java                # 경매 아이템 엔티티 (buyNowPrice 포함)
│   │   ├── ItemImage.java                  # 이미지 엔티티 (URL, 순서)
│   │   ├── Bid.java                        # 입찰 엔티티
│   │   ├── Order.java                      # 주문 엔티티 (낙찰 or 즉시구매 정보)
│   │   ├── Cart.java                       # 장바구니 엔티티
│   │   └── CartItem.java                   # 장바구니 아이템 엔티티
│   │
│   ├── constant/
│   │   ├── AuctionStatus.java              # 경매 상태 Enum (대기, 진행중, 종료, 낙찰, 즉시구매완료)
│   │   ├── BidStatus.java                  # 입찰 상태 Enum (진행중, 낙찰, 패찰, 취소)
│   │   ├── OrderStatus.java                # 주문 상태 Enum (대기, 완료, 취소)
│   │   ├── CartItemStatus.java             # 장바구니 상태 Enum (활성, 구매완료, 만료)
│   │   ├── Category.java                   # 카테고리 Enum (전자제품, 의류, 도서 등)
│   │   ├── AuctionPolicy.java              # 경매 정책 (최소 금액, 연장 규칙 등)
│   │   └── BidPolicy.java                  # 입찰 정책 (최소 단위, 자동 입찰 규칙)
│   │
│   └── vo/
│       ├── Money.java                      # 금액 값 객체 (통화 단위 포함)
│       ├── BidAmount.java                  # 입찰가 값 객체
│       └── AuctionPeriod.java              # 기간 값 객체 (시작일, 종료일)
│
├── adapter/
│   ├── in/
│   │   ├── web/
│   │   │   ├── ApiV1AuctionController.java # 경매 REST API (CRUD, 검색, 즉시구매)
│   │   │   ├── ApiV1BidController.java     # 입찰 REST API
│   │   │   ├── ApiV1OrderController.java   # 주문 REST API (조회, 결제 완료 알림)
│   │   │   └── ApiV1CartController.java    # 장바구니 REST API
│   │   │
│   │   ├── event/
│   │   │   └── AuctionEventListener.java   # 외부 도메인 이벤트 수신
│   │   │
│   │   ├── websocket/
│   │   │   └── BidWebSocketHandler.java    # WebSocket 실시간 입찰 알림
│   │   │
│   │   ├── scheduler/
│   │   │   └── AuctionScheduler.java       # 경매 종료 스케줄러 (매 분 실행)
│   │   │
│   │   └── init/
│   │       └── AuctionDataInit.java        # 테스트용 경매 데이터 생성
│   │
│   └── out/
│       ├── PaymentApiClient.java           # Payment 도메인 API 호출
│       └── BidCacheAdapter.java            # Redis 분산 락 어댑터
│
└── port/out/
    ├── AuctionItemRepository.java          # 경매 데이터 저장/조회
    ├── BidRepository.java                  # 입찰 데이터 저장/조회
    ├── OrderRepository.java                # 주문 데이터 저장/조회
    ├── CartRepository.java                 # 장바구니 데이터 저장/조회
    ├── CartItemRepository.java             # 장바구니 아이템 저장/조회
    ├── ItemImageRepository.java            # 이미지 데이터 저장/조회
    └── AuctionSearchPort.java              # Elasticsearch 검색 포트
```

**역할:**
- 경매 생성, 수정, 조회, 종료, 자동 연장
- 입찰 처리 (동시성 제어, 실시간 알림)
- 즉시구매 처리 (Buy It Now)
- 장바구니 관리 (즉시구매 전용 관심 목록)
- 낙찰 정보 관리 (Order)
- 결제 프로세스 시작점

---

### 2.4 Payment 도메인 (결제 처리 - Toss Payments 연동)

```
boundedContext/payment/
│
├── app/
│   ├── PaymentFacade.java                  # 결제 처리, 취소 플로우 조율
│   ├── PaymentSupport.java                 # 결제 조회, 검증 공통 기능
│   ├── PaymentConfirmUseCase.java          # 결제 승인 (Toss API 호출)
│   ├── PaymentCancelUseCase.java           # 결제 취소
│   ├── PaymentQueryUseCase.java            # 결제 내역 조회
│   └── PaymentVerifyUseCase.java           # 결제 검증 (위변조 방지)
│
├── domain/
│   ├── Payment.java                        # 결제 엔티티 (paymentKey, orderId 저장)
│   ├── PaymentPolicy.java                  # 결제 정책 (최소 금액, 수수료 등)
│   ├── PaymentMethod.java                  # 결제 수단 Enum (카드, 계좌이체, 간편결제)
│   └── PaymentStatus.java                  # 결제 상태 Enum (대기, 완료, 실패, 취소)
│
├── in/
│   ├── ApiV1PaymentController.java         # 결제 승인 REST API (/confirm)
│   └── PaymentDataInit.java                # 테스트용 결제 데이터 생성
│
└── out/
    ├── PaymentRepository.java              # 결제 데이터 저장/조회
    ├── TossPaymentAdapter.java             # Toss Payments API 어댑터
    └── AuctionApiClient.java               # Auction 도메인 API 호출 (결제 완료 알림)
```

**역할:**
- Toss Payments API 연동 (결제 승인, 취소)
- 결제 정보 저장 (paymentKey, orderId, amount)
- 결제 완료 후 Auction 도메인에 알림

---

### 2.6 Settlement 도메인 (정산 관리)

```
boundedContext/settlement/
│
├── app/
│   ├── SettlementFacade.java               # 정산 생성, 완료 등 복잡한 플로우 조율
│   ├── SettlementSupport.java              # 정산 조회, 검증 공통 기능
│   ├── SettlementCreateUseCase.java        # 정산 생성
│   ├── SettlementCompleteUseCase.java      # 정산 완료 처리 (판매자 계좌 이체)
│   ├── SettlementQueryUseCase.java         # 정산 내역 조회
│   └── SettlementCalculateUseCase.java     # 정산 금액 계산 (수수료 차감)
│
├── domain/
│   ├── Settlement.java                     # 정산 엔티티
│   ├── SettlementPolicy.java               # 정산 정책 (수수료율, 정산 주기)
│   ├── SettlementAmount.java               # 정산 금액 값 객체 (판매가, 수수료, 실지급액)
│   └── SettlementStatus.java               # 정산 상태 Enum (대기, 진행중, 완료, 실패)
│
├── in/
│   ├── ApiV1SettlementController.java      # 정산 REST API (판매자용)
│   ├── SettlementScheduler.java            # 자동 정산 스케줄러 (주간 정산)
│   ├── SettlementEventListener.java        # 결제 완료 이벤트 수신
│   └── SettlementDataInit.java             # 테스트용 정산 데이터 생성
│
└── out/
    └── SettlementRepository.java           # 정산 데이터 저장/조회
```

---

### 2.6 Notification 도메인 (알림 관리)

```
boundedContext/notification/
│
├── app/
│   ├── NotificationFacade.java             # 알림 발송, 읽음 처리 등 플로우 조율
│   ├── NotificationSupport.java            # 알림 조회 공통 기능
│   ├── NotificationSendUseCase.java        # 알림 발송 (푸시, 이메일, SMS)
│   ├── NotificationReadUseCase.java        # 알림 읽음 처리
│   └── NotificationQueryUseCase.java       # 알림 목록 조회
│
├── domain/
│   ├── Notification.java                   # 알림 엔티티
│   ├── NotificationPolicy.java             # 알림 정책 (발송 조건, 제한)
│   ├── NotificationType.java               # 알림 유형 Enum (입찰, 낙찰, 결제, 정산)
│   └── NotificationStatus.java             # 알림 상태 Enum (미읽음, 읽음, 삭제)
│
├── in/
│   ├── ApiV1NotificationController.java    # 알림 REST API
│   ├── NotificationWebSocketHandler.java   # SSE/WebSocket 실시간 알림
│   ├── NotificationEventListener.java      # 여러 도메인 이벤트 수신 (입찰, 낙찰, 결제 등)
│   └── NotificationDataInit.java           # 테스트용 알림 데이터 생성
│
└── out/
    ├── NotificationRepository.java         # 알림 데이터 저장/조회
    ├── FcmAdapter.java                     # Firebase Cloud Messaging 어댑터
    └── EmailAdapter.java                   # 이메일 발송 어댑터
```

---

### 2.8 Watchlist 도메인 (관심상품 관리)

```
boundedContext/watchlist/
│
├── app/
│   ├── WatchlistFacade.java                # 관심상품 추가, 제거 플로우 조율
│   ├── WatchlistSupport.java               # 관심상품 조회 공통 기능
│   ├── WatchlistAddUseCase.java            # 관심상품 추가
│   ├── WatchlistRemoveUseCase.java         # 관심상품 제거
│   └── WatchlistQueryUseCase.java          # 관심상품 목록 조회
│
├── domain/
│   ├── Watchlist.java                      # 관심상품 엔티티
│   └── WatchlistPolicy.java                # 관심상품 정책 (최대 개수 제한)
│
├── in/
│   ├── ApiV1WatchlistController.java       # 관심상품 REST API
│   ├── WatchlistEventListener.java         # 경매 종료 이벤트 수신 (관심상품 알림)
│   └── WatchlistDataInit.java              # 테스트용 관심상품 데이터 생성
│
└── out/
    └── WatchlistRepository.java            # 관심상품 데이터 저장/조회
```

---

### 2.8 Refund 도메인 (환불 관리)

```
boundedContext/refund/
│
├── app/
│   ├── RefundFacade.java                   # 환불 요청, 승인, 거절 플로우 조율
│   ├── RefundSupport.java                  # 환불 조회, 검증 공통 기능
│   ├── RefundRequestUseCase.java           # 환불 요청
│   ├── RefundApproveUseCase.java           # 환불 승인 (관리자)
│   ├── RefundRejectUseCase.java            # 환불 거절 (관리자)
│   └── RefundQueryUseCase.java             # 환불 내역 조회
│
├── domain/
│   ├── Refund.java                         # 환불 엔티티
│   ├── RefundPolicy.java                   # 환불 정책 (기한, 수수료)
│   ├── RefundReason.java                   # 환불 사유 Enum
│   └── RefundStatus.java                   # 환불 상태 Enum (요청, 승인, 거절, 완료)
│
├── in/
│   ├── ApiV1RefundController.java          # 환불 REST API
│   ├── RefundEventListener.java            # 결제 취소 이벤트 수신
│   └── RefundDataInit.java                 # 테스트용 환불 데이터 생성
│
└── out/
    └── RefundRepository.java               # 환불 데이터 저장/조회
```

---

## 3. 계층별 역할 설명

### 3.1 Application 계층 (app/)

#### **🎯 Facade**
- **목적**: 복잡한 비즈니스 플로우 조율
- **책임**: 여러 UseCase 조합, 트랜잭션 관리, 이벤트 발행
- **예시**: 회원가입 시 (사용자 생성 + 이메일 발송 + 이벤트 발행)

#### **📦 Support**
- **목적**: 공통 기능 재사용
- **책임**: 조회, 검증, 유틸리티
- **예시**: 사용자 ID로 조회, 이메일 중복 검증, 권한 확인

#### **⚙️ UseCase**
- **목적**: 단일 비즈니스 작업 수행
- **책임**: 하나의 명확한 작업 (생성, 수정, 삭제, 조회)
- **예시**: 사용자 생성, 경매 종료, 입찰 처리

---

### 3.2 Domain 계층 (domain/)

#### **📄 Entity**
- **목적**: 핵심 비즈니스 객체
- **책임**: 비즈니스 규칙 포함, 상태 관리
- **특징**: 외부 의존성 없음 (Pure Java)

#### **📋 Policy**
- **목적**: 비즈니스 정책 중앙 관리
- **책임**: 상수, 규칙, 계산 로직
- **예시**: 최소 입찰 금액, 수수료율, 정산 주기

#### **💎 Value Object**
- **목적**: 불변 값 객체
- **책임**: 유효성 검증, 값 비교
- **특징**: equals/hashCode 구현

---

### 3.3 Inbound Adapter 계층 (in/)

#### **🌐 Controller**
- **목적**: REST API 엔드포인트 제공
- **책임**: Request 수신 → Facade 호출 → Response 생성
- **특징**: 비즈니스 로직 없음

#### **📡 EventListener**
- **목적**: 다른 도메인 이벤트 수신
- **책임**: 이벤트 수신 → Facade 호출
- **특징**: 비동기 처리 (@Async)

#### **⏰ Scheduler**
- **목적**: 정기 작업 실행
- **책임**: 스케줄 실행 → Facade 호출
- **특징**: Cron 표현식 사용

#### **🗃️ DataInit**
- **목적**: 개발/테스트용 초기 데이터 생성
- **책임**: 애플리케이션 시작 시 데이터 삽입
- **특징**: 로컬 환경에서만 실행

---

### 3.4 Outbound Adapter 계층 (out/)

#### **💾 Repository**
- **목적**: 데이터 저장/조회
- **책임**: JPA를 통한 DB 접근
- **특징**: Spring Data JPA 인터페이스

#### **🔌 Adapter**
- **목적**: 외부 서비스 연동
- **책임**: API 호출, 데이터 변환
- **특징**: RestTemplate, WebClient 사용

---

### 3.5 계층별 책임 요약표

| 계층 | 클래스 | 역할 | 예시 |
|-----|-------|-----|-----|
| **app/** | Facade | 복잡한 플로우 조율 | `UserFacade.registerUser()` |
| **app/** | Support | 공통 조회/검증 | `UserSupport.getUserById()` |
| **app/** | UseCase | 단일 비즈니스 작업 | `UserJoinUseCase.join()` |
| **domain/** | Entity | 비즈니스 객체 | `User`, `AuctionItem` |
| **domain/** | Policy | 정책/규칙 | `AuctionPolicy.MIN_BID_AMOUNT` |
| **domain/** | VO | 불변 값 객체 | `Email`, `Money` |
| **in/** | Controller | REST API | `ApiV1UserController` |
| **in/** | EventListener | 이벤트 수신 | `BidEventListener` |
| **in/** | Scheduler | 정기 작업 | `AuctionScheduler` |
| **out/** | Repository | 데이터 저장/조회 | `UserRepository` |
| **out/** | Adapter | 외부 서비스 연동 | `TossPaymentAdapter` |

---

## 4. Shared (도메인 간 공유)

> **Anti-Corruption Layer**: 도메인 간 결합도를 낮추고 독립성 보장

```
shared/
│
├── user/                                   # User 도메인 공유
│   ├── dto/
│   │   ├── UserDto.java                    # 외부 노출용 DTO (이메일, 이름, 역할)
│   │   └── SellerDto.java                  # 판매자 정보 DTO
│   ├── event/
│   │   ├── UserJoinedEvent.java            # 회원가입 이벤트
│   │   ├── UserModifiedEvent.java          # 사용자 수정 이벤트
│   │   └── UserDeletedEvent.java           # 사용자 삭제 이벤트
│   └── out/
│       └── UserApiClient.java              # 다른 도메인에서 User 조회 시 사용
│
├── auction/
│   ├── dto/
│   │   ├── AuctionDto.java                 # 경매 정보 DTO
│   │   ├── AuctionSummaryDto.java          # 경매 요약 DTO (목록용)
│   │   ├── CartDto.java                    # 장바구니 DTO
│   │   └── CartItemDto.java                # 장바구니 아이템 DTO
│   ├── event/
│   │   ├── AuctionCreatedEvent.java        # 경매 생성 이벤트
│   │   ├── AuctionClosedEvent.java         # 경매 종료 이벤트
│   │   ├── AuctionExtendedEvent.java       # 경매 연장 이벤트
│   │   └── AuctionBuyNowEvent.java         # 즉시구매 이벤트
│   └── out/
│       └── AuctionApiClient.java           # 다른 도메인에서 Auction 조회 시 사용
│
├── bid/
│   ├── dto/
│   │   └── BidDto.java                     # 입찰 정보 DTO
│   ├── event/
│   │   ├── BidPlacedEvent.java             # 입찰 완료 이벤트
│   │   └── BidCanceledEvent.java           # 입찰 취소 이벤트
│   └── out/
│       └── BidApiClient.java               # 다른 도메인에서 Bid 조회 시 사용
│
├── payment/
│   ├── dto/
│   │   └── PaymentDto.java                 # 결제 정보 DTO
│   ├── event/
│   │   ├── PaymentCompletedEvent.java      # 결제 완료 이벤트
│   │   └── PaymentFailedEvent.java         # 결제 실패 이벤트
│   └── out/
│       └── TossPaymentsService.java        # Toss Payments PG 서비스
│
├── settlement/
│   ├── dto/
│   │   └── SettlementDto.java              # 정산 정보 DTO
│   └── event/
│       └── SettlementCompletedEvent.java   # 정산 완료 이벤트
│
└── notification/
    ├── dto/
    │   └── NotificationDto.java            # 알림 정보 DTO
    └── event/
        └── NotificationSentEvent.java      # 알림 발송 이벤트
```

---

## 5. Global (전역 설정)

```
global/
│
├── config/                                 # Spring 설정
│   ├── JpaConfig.java                      # JPA 설정 (Auditing, QueryDSL)
│   ├── RedisConfig.java                    # Redis 설정 (연결, 직렬화)
│   ├── ElasticsearchConfig.java            # Elasticsearch 설정
│   ├── KafkaConfig.java                    # Kafka 설정 (Producer, Consumer)
│   ├── WebMvcConfig.java                   # Web MVC 설정 (CORS, Interceptor)
│   ├── SecurityConfig.java                 # Spring Security 설정 (프로파일별)
│   ├── AsyncConfig.java                    # @Async 설정 (스레드 풀)
│   └── BatchConfig.java                    # Spring Batch 설정
│
├── security/                               # 인증/인가
│   ├── jwt/
│   │   ├── JwtTokenProvider.java           # JWT 토큰 생성/검증
│   │   ├── JwtAuthenticationFilter.java    # JWT 필터
│   │   └── JwtProperties.java              # JWT 설정 (만료시간, 시크릿)
│   ├── CustomUserDetailsService.java       # Spring Security UserDetails
│   └── SecurityUtils.java                  # 현재 사용자 조회 유틸
│
├── exception/                              # 전역 예외 처리
│   ├── GlobalExceptionHandler.java         # @RestControllerAdvice 전역 예외 처리
│   ├── DomainException.java                # 도메인 예외 Base 클래스
│   ├── BusinessException.java              # 비즈니스 로직 예외
│   ├── EntityNotFoundException.java        # 엔티티 없음 예외 (404)
│   ├── UnauthorizedException.java          # 인증 실패 예외 (401)
│   ├── InvalidValueException.java          # 유효하지 않은 값 예외 (400)
│   ├── DuplicateException.java             # 중복 예외 (409)
│   ├── ErrorCode.java                      # 에러 코드 Enum
│   └── ErrorResponse.java                  # 에러 응답 DTO
│
├── util/                                   # 공통 유틸리티
│   ├── EncryptionUtil.java                 # AES-256-GCM 암호화 유틸
│   ├── DateTimeUtil.java                   # 날짜/시간 유틸
│   ├── StringUtil.java                     # 문자열 유틸
│   └── FileUtil.java                       # 파일 유틸
│
├── eventPublisher/                         # 전역 이벤트 처리
│   ├── EventPublisher.java                 # Spring Event Publisher 래퍼
│   └── EventConfig.java                    # 이벤트 설정 (@EnableAsync)
│
├── jpa/                                    # JPA 공통
│   └── entity/
│       ├── BaseEntity.java                 # createdAt, updatedAt, createdBy, updatedBy
│       ├── BaseIdAndTime.java              # id(자동생성), createdAt, updatedAt
│       └── BaseManualIdAndTime.java        # id(수동설정), createdAt, updatedAt
│
└── common/                                 # 공통 클래스
    ├── ApiResponse.java                    # 공통 API 응답 <T> (success, error)
    └── RsData.java                         # Result + Data (successCode, resultMsg, data)
```

---

## 6. Infrastructure (외부 인프라)

> **Shared Infrastructure**: 여러 도메인이 공유하는 인프라

```
infrastructure/
│
├── s3/                                     # AWS S3 파일 저장
│   ├── S3Service.java                      # S3 업로드/다운로드 서비스
│   ├── S3Config.java                       # S3 클라이언트 설정
│   └── dto/
│       └── ImageUploadResponse.java        # 이미지 업로드 응답 DTO (URL, 파일명)
│
├── elasticsearch/                          # 검색 엔진
│   ├── document/
│   │   └── AuctionDocument.java            # Elasticsearch 검색용 문서
│   ├── repository/
│   │   └── AuctionSearchRepository.java    # Elasticsearch Repository
│   └── service/
│       └── SearchService.java              # 검색 서비스 (색인, 검색)
│
├── pg/                                     # PG 결제 연동
│   ├── toss/
│   │   ├── TossPaymentClient.java          # Toss Payments API 클라이언트
│   │   ├── TossPaymentProperties.java      # Toss Payments 설정 (시크릿, URL)
│   │   └── dto/
│   │       ├── TossPaymentRequest.java     # 결제 요청 DTO
│   │       └── TossPaymentResponse.java    # 결제 응답 DTO
│   └── kakao/
│       ├── KakaoPayClient.java             # Kakao Pay API 클라이언트
│       ├── KakaoPayProperties.java         # Kakao Pay 설정
│       └── dto/
│           ├── KakaoPayRequest.java        # 결제 요청 DTO
│           └── KakaoPayResponse.java       # 결제 응답 DTO
│
├── redis/                                  # Redis 캐싱 및 분산 락
│   ├── RedisService.java                   # Redis 공통 서비스 (get, set, delete)
│   ├── DistributedLock.java                # Redisson 분산 락
│   └── CacheKeyGenerator.java              # 캐시 키 생성 유틸
│
└── kafka/                                  # Kafka 이벤트 스트리밍
    ├── producer/
    │   ├── EventProducer.java              # Kafka 이벤트 발행
    │   └── KafkaProducerConfig.java        # Kafka Producer 설정
    └── consumer/
        ├── EventConsumer.java              # Kafka 이벤트 수신
        └── KafkaConsumerConfig.java        # Kafka Consumer 설정
```

---

## 7. 아키텍처 원칙

### 7.1 의존성 방향

```
외부 세계 (Web, DB, External API)
    ↓
Inbound Adapter (Controller, EventListener)
    ↓
Facade (여러 UseCase 조합)
    ↓
UseCase (단일 비즈니스 작업)
    ↓ Support (공통 조회/검증)
    ↓
Domain (핵심 비즈니스 로직)
    ↓
Outbound Adapter (Repository, External API)
```

**핵심 원칙:**
- ✅ **도메인은 외부에 의존하지 않음** (Pure Java)
- ✅ **모든 의존성은 내부(도메인)를 향함** (Dependency Inversion)
- ✅ **Facade로 복잡도 관리** (여러 UseCase 조합)
- ✅ **Support로 공통 기능 재사용** (중복 제거)

---

### 7.2 Facade vs UseCase 사용 기준

#### **🎯 Facade 사용 시**
- 여러 UseCase를 조합해야 할 때
- 외부 서비스 호출이 포함될 때
- 복잡한 트랜잭션 관리가 필요할 때
- 이벤트 발행이 필요할 때

**예시**: 회원가입 (사용자 생성 + 이메일 발송 + 이벤트 발행)

#### **⚙️ UseCase 사용 시**
- 단일 비즈니스 작업만 수행할 때
- 다른 UseCase와 독립적일 때
- 재사용 가능한 작은 단위일 때

**예시**: 사용자 생성, 경매 조회

---

### 7.3 도메인 간 통신 방식

#### **🔄 경매 낙찰 → 결제 플로우**

```
1. 경매 낙찰 (Auction 도메인)
   ├─> AuctionCloseUseCase.close()
   ├─> Order 생성 (UUID orderId)
   └─> Order 저장
   
2. 프론트 → 경매 API 호출
   GET /api/v1/auctions/{auctionId}/order
   ← orderId, amount 조회
   
3. 프론트 → Toss Payments 결제
   토스 결제 완료 → paymentKey 생성
   
4. 프론트 → 결제 API 호출 (Payment 도메인)
   POST /api/v1/payments/confirm
   { paymentKey, orderId, amount }
   ├─> PaymentConfirmUseCase.confirm()
   ├─> Toss API 승인 요청
   ├─> Payment 저장
   └─> AuctionApiClient.notifyPaymentCompleted(orderId)
   
5. 결제 완료 알림 (Payment → Auction)
   POST /api/v1/orders/{orderId}/payment-completed
   ├─> OrderCompleteUseCase.complete()
   ├─> Order 상태 업데이트
   └─> 지갑 처리, 정산 등
```

#### **📌 통신 원칙**
- **API 호출**: 도메인 간 동기 통신 (RestTemplate/WebClient)
- **프론트 매개**: 프론트가 도메인 간 데이터 전달
- **느슨한 결합**: orderId로만 참조 (엔티티 직접 참조 X)

---

### 7.4 데이터 흐름 예시 (입찰 처리)

```
1. Client
   ↓ HTTP POST /api/bids
2. BidController (in/)
   ↓ BidRequest 수신
3. BidFacade (app/)
   ↓ 복잡한 플로우 조율
   ├─> BidSupport.validateBid()           # 검증
   ├─> BidPlaceUseCase.place()            # 입찰 (분산 락)
   ├─> AuctionApiClient.increaseCount()   # 다른 도메인 호출 (shared/)
   └─> EventPublisher.publish()           # 이벤트 발행
4. BidRepository (out/)
   ↓ JPA로 DB 저장
5. Database
```

---

## 8. MSA 전환 전략

### 8.1 현재 (Modular Monolith)

```
fourtune-app.jar
├── boundedContext/user/
├── boundedContext/auction/      # 경매 + 입찰 + 주문
└── boundedContext/payment/
```

**장점:**
- ✅ 빠른 개발
- ✅ 단순한 배포
- ✅ 트랜잭션 관리 쉬움
- ✅ 도메인별 독립적인 구조 (MSA 전환 준비 완료)

---

### 8.2 Phase 1: 모듈 완전 분리

```
각 BoundedContext 독립성 강화
- Shared 계층 통한 통신만 허용
- 직접 참조 금지 (ApiClient 사용)
- Event로만 통신 (Spring Event)
```

---

### 8.3 Phase 2: 멀티 모듈 프로젝트

```
fourtune/
├── fourtune-user/
│   └── src/main/java/.../boundedContext/user/
├── fourtune-auction/
│   └── src/main/java/.../boundedContext/auction/  # 경매 + 입찰 + 주문
├── fourtune-payment/
│   └── src/main/java/.../boundedContext/payment/
└── fourtune-common/
    └── src/main/java/.../shared/
```

---

### 8.4 Phase 3: 완전한 MSA

```
fourtune-user-service/       (독립 프로젝트, 독립 DB)
fourtune-auction-service/    (독립 프로젝트, 독립 DB) # 경매 + 입찰 + 주문
fourtune-payment-service/    (독립 프로젝트, 독립 DB)
```

**추가 구성:**
- Spring Cloud Gateway (API Gateway)
- Eureka Server (Service Discovery)
- Kafka (Event Bus - Spring Event 대체)
- Redis (Distributed Cache/Lock)

---

## 9. 테스트 전략

### 9.1 단위 테스트
- **대상**: Domain 계층 (Entity, VO, Policy)
- **특징**: 외부 의존성 없음 (Pure Java)
- **도구**: JUnit5, AssertJ

### 9.2 통합 테스트
- **대상**: UseCase, Facade, Support
- **특징**: Repository Mocking
- **도구**: Mockito, @MockBean

### 9.3 E2E 테스트
- **대상**: Controller부터 Database까지
- **특징**: 실제 DB 사용
- **도구**: Testcontainers, @SpringBootTest

---

## 10. 실행 방법

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

---
