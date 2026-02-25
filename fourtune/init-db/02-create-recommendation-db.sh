#!/bin/bash
# 추천 서비스(recommendation-service) 전용 DB.
set -e
USER="${POSTGRES_USER:-postgres}"
psql -v ON_ERROR_STOP=1 --username "$USER" --dbname "postgres" -c "CREATE DATABASE recommendation;"
