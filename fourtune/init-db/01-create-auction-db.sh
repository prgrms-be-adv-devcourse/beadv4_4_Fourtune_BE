#!/bin/bash
# 경매 서비스(auction-service) 전용 DB. 정산은 fourtune_api → fourtune_db 사용.
set -e
USER="${POSTGRES_USER:-postgres}"
psql -v ON_ERROR_STOP=1 --username "$USER" --dbname "postgres" -c "CREATE DATABASE auction;"
