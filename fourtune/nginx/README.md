# Nginx 설정 파일

## 📁 파일 구조

```
nginx/
├── nginx.dev.conf    # 개발 서버용 (HTTP만)
├── nginx.prod.conf   # 프로덕션용 (HTTPS, 보안 강화)
├── nginx.conf        # 기본 (사용 안함, 참고용)
└── ssl/              # SSL 인증서 (프로덕션)
    ├── fullchain.pem
    └── privkey.pem
```

---

## 🔧 개발 서버 (nginx.dev.conf)

### 특징
- HTTP만 사용 (포트 80)
- SSL 없음
- Rate Limiting 느슨함 (50 req/s)
- 간단한 설정

### 사용
```bash
docker-compose -f docker-compose.dev.yml up -d

# 접속
http://dev-server-ip
http://dev.fourtune.com  # 도메인 설정 시
```

### 주요 설정
```nginx
listen 80;                      # HTTP만
server_name _;                  # 모든 도메인 허용
limit_req_zone rate=50r/s;      # 느슨한 제한
```

---

## 🚀 프로덕션 (nginx.prod.conf)

### 특징
- HTTPS 사용 (포트 443)
- HTTP → HTTPS 자동 리다이렉트
- SSL 인증서 필요
- Rate Limiting 엄격 (10 req/s)
- 보안 헤더 설정
- 로그 관리

### 사용
```bash
# 1. SSL 인증서 준비
./nginx/ssl/fullchain.pem
./nginx/ssl/privkey.pem

# 2. 실행
docker-compose -f docker-compose.prod.yml up -d

# 3. 접속
https://fourtune.com
```

### 주요 설정
```nginx
listen 443 ssl http2;           # HTTPS
ssl_certificate ...;            # SSL 인증서
limit_req_zone rate=10r/s;      # 엄격한 제한
add_header Strict-Transport-Security;  # 보안 헤더
```

---

## 🔐 SSL 인증서 준비

### Let's Encrypt (무료)

```bash
# 1. Certbot 설치
sudo apt install certbot

# 2. 인증서 발급
sudo certbot certonly --standalone -d fourtune.com -d www.fourtune.com

# 3. 인증서 복사
sudo cp /etc/letsencrypt/live/fourtune.com/fullchain.pem nginx/ssl/
sudo cp /etc/letsencrypt/live/fourtune.com/privkey.pem nginx/ssl/

# 4. 권한 설정
chmod 644 nginx/ssl/fullchain.pem
chmod 600 nginx/ssl/privkey.pem
```

### 자동 갱신 (90일마다)

```bash
# Cron 등록
sudo crontab -e

# 매달 1일 새벽 3시 갱신
0 3 1 * * certbot renew --quiet && docker-compose -f /path/to/docker-compose.prod.yml restart nginx
```

---

## 🎯 엔드포인트 매핑

### API
```
외부: http(s)://domain/api/*
→ Nginx
→ 내부: http://app:8080/api/*
```

### WebSocket
```
외부: ws(s)://domain/ws/*
→ Nginx (Upgrade)
→ 내부: ws://app:8080/ws/*
```

### Health Check
```
외부: http(s)://domain/actuator/health
→ Nginx
→ 내부: http://app:8080/actuator/health
```

---

## 📊 설정 비교

| 항목 | 개발 (dev) | 프로덕션 (prod) |
|------|-----------|----------------|
| 프로토콜 | HTTP | HTTPS |
| 포트 | 80 | 80, 443 |
| SSL | ❌ | ✅ 필수 |
| Rate Limit | 50 req/s | 10 req/s |
| 보안 헤더 | 기본 | 강화 |
| 로그 | 기본 | Rotation |
| CORS | 허용 | 제한 |

---

## 🛠️ 커스터마이징

### Rate Limiting 조정

```nginx
# nginx.dev.conf 또는 nginx.prod.conf
limit_req_zone $binary_remote_addr zone=api_limit:10m rate=10r/s;
#                                                          ↑ 이 값 조정

# API 엔드포인트
limit_req zone=api_limit burst=20 nodelay;
#                             ↑ burst 조정
```

### CORS 설정 (필요 시)

```nginx
location /api/ {
    # CORS 헤더 추가
    add_header 'Access-Control-Allow-Origin' '*';
    add_header 'Access-Control-Allow-Methods' 'GET, POST, PUT, DELETE, OPTIONS';
    add_header 'Access-Control-Allow-Headers' 'Authorization, Content-Type';
    
    # OPTIONS 요청 처리
    if ($request_method = 'OPTIONS') {
        return 204;
    }
    
    proxy_pass http://backend;
}
```

### 업로드 크기 제한

```nginx
http {
    client_max_body_size 50M;  # 기본 50MB
    #                      ↑ 필요에 따라 조정
}
```

---

## 🔍 테스트

### 개발 서버

```bash
# Nginx 설정 검증
docker exec fourtune-nginx-dev nginx -t

# 재시작
docker-compose -f docker-compose.dev.yml restart nginx

# 접속 테스트
curl http://dev-server-ip/actuator/health
```

### 프로덕션

```bash
# Nginx 설정 검증
docker exec fourtune-nginx-prod nginx -t

# 재시작
docker-compose -f docker-compose.prod.yml restart nginx

# 접속 테스트
curl https://fourtune.com/actuator/health

# SSL 확인
openssl s_client -connect fourtune.com:443 -servername fourtune.com
```

---

## ⚠️ 주의사항

### 1. 개발 서버
```
⚠️ HTTP만 사용 (보안 주의)
⚠️ Rate Limiting 느슨함
✅ SSL 인증서 불필요
✅ 빠른 개발 가능
```

### 2. 프로덕션
```
✅ HTTPS 필수
✅ SSL 인증서 필요
✅ 보안 강화 설정
⚠️ SSL 인증서 갱신 필요 (90일)
```

---

## 📚 참고

- Nginx 공식 문서: https://nginx.org/en/docs/
- Let's Encrypt: https://letsencrypt.org/
- SSL Labs 테스트: https://www.ssllabs.com/ssltest/

