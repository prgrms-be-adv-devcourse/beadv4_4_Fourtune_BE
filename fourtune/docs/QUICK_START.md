# 🚀 Fourtune 빠른 시작 가이드 (팀 개발)

## ⚡ 3분 빠른 시작

### 1️⃣ 필수 준비물

```bash
✅ Docker Desktop 설치
✅ Git
✅ (선택) IntelliJ IDEA 또는 VSCode
```

**Docker Desktop 다운로드:**
- Windows/Mac: https://www.docker.com/products/docker-desktop

---

### 2️⃣ 프로젝트 클론

```bash
git clone <repository-url>
cd fourtune
```

---

### 3️⃣ 실행 (한 줄!)

```bash
docker-compose up -d --build
```

**끝!** 🎉

---

### 4️⃣ 확인

```bash
# 컨테이너 상태 확인
docker-compose ps

# 애플리케이션 로그 확인
docker-compose logs -f app

# Health Check
curl http://localhost:8080/actuator/health
```

**응답이 오면 성공!**

```json
{
  "status": "UP"
}
```

---

## 🌐 접속 정보

### Spring Boot API
```
http://localhost:8080
http://localhost:8080/actuator/health
```

### 데이터베이스 (로컬 접속)
```
Host: localhost
Port: 5432
Database: fourtune_db
Username: fourtune_user
Password: fourtune_password
```

### Redis
```
Host: localhost
Port: 6379
```

### Elasticsearch
```
http://localhost:9200
```

### Kafka
```
localhost:9092
```

---

## 🛠️ 주요 명령어

### 시작/중지

```bash
# 시작
docker-compose up -d

# 중지
docker-compose down

# 재시작
docker-compose restart

# 재빌드 후 시작
docker-compose up -d --build
```

### 로그 확인

```bash
# 전체 로그
docker-compose logs -f

# Spring Boot만
docker-compose logs -f app

# PostgreSQL만
docker-compose logs -f postgres
```

### 상태 확인

```bash
# 컨테이너 상태
docker-compose ps

# Health Check
curl http://localhost:8080/actuator/health
```

### 컨테이너 내부 접속

```bash
# Spring Boot 컨테이너
docker exec -it fourtune-app-local sh

# PostgreSQL
docker exec -it fourtune-postgres psql -U fourtune_user -d fourtune_db

# Redis
docker exec -it fourtune-redis redis-cli
```

---

## 🐛 디버깅

### IntelliJ IDEA

1. **Run > Edit Configurations**
2. **+ > Remote JVM Debug**
3. 설정:
   ```
   Host: localhost
   Port: 5005
   ```
4. **Debug 버튼 클릭**

### VSCode

`.vscode/launch.json`:
```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "Debug Fourtune",
      "request": "attach",
      "hostName": "localhost",
      "port": 5005
    }
  ]
}
```

---

## ❓ 문제 해결

### 포트 충돌
```bash
Error: port is already allocated

해결:
1. docker-compose down
2. 다른 프로그램 종료 (PostgreSQL, Redis 등)
3. docker-compose up -d
```

### 컨테이너가 안 뜸
```bash
# 로그 확인
docker-compose logs

# 재생성
docker-compose down
docker-compose up -d --force-recreate
```

### 빌드 실패
```bash
# 캐시 삭제 후 재빌드
docker-compose build --no-cache
docker-compose up -d
```

### 데이터베이스 초기화
```bash
# 주의: 모든 데이터 삭제됨!
docker-compose down -v
docker-compose up -d
```

---

## 📚 코드 수정 시

### 옵션 1: 자동 재빌드 (추천)

```bash
# 코드 수정 후
docker-compose up -d --build
```

### 옵션 2: 로컬에서 실행

```bash
# 인프라만 Docker
docker-compose up -d postgres redis elasticsearch

# Spring Boot는 로컬 실행
./gradlew bootRun
```

---

## 🎓 다음 단계

### 1. API 테스트
```bash
# Health Check
curl http://localhost:8080/actuator/health

# 향후 API 예시
curl http://localhost:8080/api/auctions
```

### 2. DB 확인
```bash
docker exec -it fourtune-postgres psql -U fourtune_user -d fourtune_db

# SQL 실행
SELECT * FROM users;
\dt  # 테이블 목록
\q   # 종료
```

### 3. 로그 모니터링
```bash
# 실시간 로그 보기
docker-compose logs -f app
```

---

## 🤝 팀 협업 팁

### Git Pull 후
```bash
# 새 의존성이나 설정 변경 시 재빌드
git pull
docker-compose up -d --build
```

### .env 파일
```bash
# .env는 gitignore에 있음
# 개인 설정이 필요하면 생성
cp env.example .env
nano .env

# 팀원과 공유 필요한 설정은 docker-compose.yml에
```

### 환경 통일
```bash
✅ 모두 docker-compose로 실행
✅ Java 버전, DB 버전 자동 통일
✅ "내 PC에선 되는데?" 문제 없음
```

---

## 🎯 체크리스트

### 새 팀원 온보딩
- [ ] Docker Desktop 설치
- [ ] 프로젝트 클론
- [ ] `docker-compose up -d --build`
- [ ] Health Check 성공
- [ ] 로그 확인
- [ ] 디버깅 포트 연결 (선택)

### 개발 시작 전
- [ ] `docker-compose ps` (모든 컨테이너 실행 중)
- [ ] Health Check 통과
- [ ] DB 연결 확인

### 종료 시
- [ ] `docker-compose down` (컨테이너 정리)
- [ ] 또는 그냥 두기 (다음 날 바로 시작)

---

