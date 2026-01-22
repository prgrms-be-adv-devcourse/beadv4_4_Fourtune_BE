# 🧪 Swagger API 테스트 가이드

> Swagger UI: http://localhost:8080/swagger-ui.html

---

## 📌 테스트 순서

1. 회원가입 → 2. 로그인 → 3. Authorize 설정 → 4. API 테스트

---

## 1️⃣ 회원가입 (인증 불필요)

### `POST /api/users/signup`

```json
{
  "email": "test@test.com",
  "password": "Test1234!@",
  "nickname": "테스터",
  "phoneNumber": "010-1234-5678"
}
```

**구매자 계정 (입찰 테스트용)**
```json
{
  "email": "buyer@test.com",
  "password": "Test1234!@",
  "nickname": "구매자",
  "phoneNumber": "010-9999-8888"
}
```

---

## 2️⃣ 로그인 (JWT 토큰 발급)

### `POST /api/auth/login`

```json
{
  "email": "test@test.com",
  "password": "Test1234!@"
}
```

**응답 예시:**
```json
{
  "grantType": "Bearer",
  "accessToken": "eyJ0eXAiOiJKV1QiLCJhbGci...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

⚠️ **accessToken 복사!**

---

## 3️⃣ Authorize 설정

1. Swagger UI 오른쪽 상단 **"Authorize"** 버튼 클릭
2. **accessToken만** 입력 (Bearer 접두사 없이!)
3. **Authorize** 클릭 → **Close**

---

## 4️⃣ 경매 등록

### `POST /api/v1/auctions`

**request 필드에 입력:**
```json
{
  "title": "테스트 경매 상품",
  "description": "테스트용 경매입니다",
  "category": "ELECTRONICS",
  "startPrice": 10000,
  "bidUnit": 1000,
  "buyNowPrice": 50000,
  "auctionStartTime": "2026-01-22T10:00:00",
  "auctionEndTime": "2026-01-23T10:00:00",
  "imageUrls": []
}
```

**images 필드:** 비워두기 (파일 선택 안 함)

### 카테고리 종류
| 값 | 설명 |
|---|---|
| `ELECTRONICS` | 전자기기 |
| `CLOTHING` | 의류 |
| `POTTERY` | 도자기 |
| `APPLIANCES` | 가전제품 |
| `BEDDING` | 침구 |
| `BOOKS` | 도서 |
| `COLLECTIBLES` | 수집품 |
| `ETC` | 기타 |

---

## 5️⃣ 경매 목록 조회

### `GET /api/v1/auctions`

파라미터 (선택):
- `status`: `SCHEDULED`, `ACTIVE`, `CLOSED`, `CANCELLED`
- `category`: `ELECTRONICS`, `CLOTHING` 등
- `page`: 0
- `size`: 20

---

## 6️⃣ 경매 상세 조회

### `GET /api/v1/auctions/{id}`

- `id`: 경매 ID (예: 1)

---

## 7️⃣ 입찰하기

⚠️ **판매자 본인은 입찰 불가! 다른 계정으로 로그인 필요**

### `POST /api/v1/bids`

```json
{
  "auctionId": 1,
  "bidAmount": 15000
}
```

**주의:**
- `bidAmount`는 현재가 + 입찰단위 이상
- 경매 상태가 `ACTIVE`여야 함

---

## 8️⃣ 입찰 내역 조회

### 경매별 입찰 내역
`GET /api/v1/bids/auction/{auctionId}`

### 내 입찰 내역
`GET /api/v1/bids/my`

### 최고가 입찰
`GET /api/v1/bids/auction/{auctionId}/highest`

---

## 9️⃣ 장바구니

### 장바구니 조회
`GET /api/v1/cart`

### 장바구니에 아이템 추가
`POST /api/v1/cart/items`
```json
{
  "auctionId": 1
}
```

### 장바구니 아이템 삭제
`DELETE /api/v1/cart/items/{cartItemId}`

### 장바구니 즉시구매
`POST /api/v1/cart/buy-now`
```json
{
  "cartItemIds": [1, 2]
}
```

### 장바구니 전체 즉시구매
`POST /api/v1/cart/buy-now/all`

---

## 🔟 주문

### 내 주문 목록
`GET /api/v1/orders/my`

### 주문 상세 조회
`GET /api/v1/orders/{orderId}`

### 주문 취소
`POST /api/v1/orders/{orderId}/cancel`

---

## 🔑 토큰 재발급

### `POST /api/auth/reissue`

```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

---

## 👤 프로필 수정

### `PATCH /api/users/profile`

```json
{
  "nickname": "새닉네임"
}
```

---

## 🔐 비밀번호 변경

### `PATCH /api/users/password`

```json
{
  "currentPassword": "Test1234!@",
  "newPassword": "NewPass1234!@"
}
```

---

## ❌ 회원 탈퇴

### `DELETE /api/users/withdraw`

```json
{
  "password": "Test1234!@"
}
```

---

## ⚠️ 자주 발생하는 에러

| 상태 코드 | 원인 | 해결 |
|----------|------|------|
| `401 Unauthorized` | 토큰 없거나 만료 | 다시 로그인 후 Authorize 설정 |
| `403 Forbidden` | 권한 없음 | 본인 리소스만 접근 가능 |
| `400 Bad Request` | 유효성 검증 실패 | 요청 값 확인 |
| `404 Not Found` | 리소스 없음 | ID 확인 |
| `500 Internal Server Error` | 서버 오류 | 로그 확인 |

---

## 📝 테스트 시나리오

### 시나리오 1: 기본 경매 흐름
1. 판매자 회원가입 & 로그인
2. 경매 등록
3. 구매자 회원가입 & 로그인
4. 입찰
5. 입찰 내역 확인

### 시나리오 2: 즉시구매 흐름
1. 판매자: 경매 등록 (buyNowPrice 설정)
2. 구매자: 장바구니에 추가
3. 구매자: 즉시구매
4. 주문 확인

---

## 🛠️ Docker 명령어

```powershell
# 컨테이너 시작
docker-compose up -d

# 컨테이너 중지
docker-compose down

# 이미지 재빌드 후 시작
docker-compose up -d --build

# 앱 로그 확인
docker logs fourtune-app-local --tail 50

# 컨테이너 상태 확인
docker ps -a
```
