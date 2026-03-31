#!/usr/bin/env bash
# 사용법: bash db/new-migration.sh
#
# 생성 위치: db/migrations/V{YYYYMMDDHHmmss}.sql
# 변경 내용은 파일 내부 주석으로 작성

set -euo pipefail

TIMESTAMP=$(date +%Y%m%d%H%M%S)
MIGRATIONS_DIR="$(dirname "$0")/migrations"
FILENAME="${MIGRATIONS_DIR}/V${TIMESTAMP}.sql"

if [ -f "$FILENAME" ]; then
  echo "이미 존재하는 파일: $FILENAME" >&2
  exit 1
fi

cat > "$FILENAME" <<EOF
-- Date: $(date +%Y-%m-%d %H:%M:%S)
-- (변경 내용을 주석으로 작성)

EOF

echo "생성됨: $FILENAME"
