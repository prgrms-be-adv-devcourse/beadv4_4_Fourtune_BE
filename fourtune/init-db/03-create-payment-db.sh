#!/bin/bash
# 결제 서비스(payment-service) 전용 DB. 정산 지급 호출은 fourtune-api → payment-service API.
#
# [주의] 테이블 생성은 payment-service의 JPA(ddl-auto: update)가 담당합니다.
# 시스템 지갑 유저(holding@system.com, revenue@platform.com) 초기 데이터는
# payment-service의 SystemWalletInitializer(ApplicationRunner)가 서비스 기동 시 자동 삽입합니다.
# 여기서 INSERT 하면 테이블이 아직 없어 오류가 발생하므로 DB 생성만 수행합니다.
set -e
USER="${POSTGRES_USER:-postgres}"
psql -v ON_ERROR_STOP=1 --username "$USER" --dbname "postgres" -c "CREATE DATABASE payment_db;"
