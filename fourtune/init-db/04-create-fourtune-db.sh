#!/bin/bash
# 결제 서비스(payment-service) 전용 DB. 정산 지급 호출은 fourtune-api → payment-service API.
set -e
USER="${POSTGRES_USER:-postgres}"
psql -v ON_ERROR_STOP=1 --username "$USER" --dbname "postgres" -c "CREATE DATABASE fourtune_db;"
