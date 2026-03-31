#!/bin/bash
set -euo pipefail

HOOKS_DIR=".githooks"
GIT_HOOKS_DIR=".git/hooks"

if [ ! -d "$GIT_HOOKS_DIR" ]; then
    echo "[오류] .git/hooks 디렉토리를 찾을 수 없습니다. git 저장소 루트에서 실행해주세요."
    exit 1
fi

if [ ! -d "$HOOKS_DIR" ]; then
    echo "[오류] .githooks 디렉토리를 찾을 수 없습니다. git 저장소 루트에서 실행해주세요."
    exit 1
fi

shopt -s nullglob
hooks=("$HOOKS_DIR"/*)
if [ ${#hooks[@]} -eq 0 ]; then
    echo "[오류] 설치할 hook 파일이 없습니다."
    exit 1
fi

echo "Git hooks 설치 중..."

for hook in "${hooks[@]}"; do
    [ -f "$hook" ] || continue
    hook_name=$(basename "$hook")
    target="$GIT_HOOKS_DIR/$hook_name"

    cp "$hook" "$target"
    chmod +x "$target"
    echo "  설치 완료: $hook_name → $target"
done

echo ""
echo "Git hooks 설치가 완료되었습니다."
echo "  커밋 전에 Checkstyle 코드 스타일 검사가 자동으로 실행됩니다."
