#!/bin/bash

# 백엔드 Keep-Alive 스크립트
# 이 스크립트를 cron job이나 외부 서비스(UptimeRobot, Uptime Monitor 등)에서 실행하여
# 백엔드가 sleep 상태로 들어가는 것을 방지합니다.

# 환경 변수에서 API URL 가져오기 (기본값: localhost)
BACKEND_URL="${BACKEND_URL:-http://localhost:8080}"

# 색상 코드
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo "🔥 백엔드 Keep-Alive 시작..."
echo "📡 대상 URL: $BACKEND_URL"

# Health Check 엔드포인트 호출
HEALTH_ENDPOINT="$BACKEND_URL/api/health"
PRODUCTS_ENDPOINT="$BACKEND_URL/api/products"

# 1. Health Check
echo ""
echo "1️⃣ Health Check..."
if curl -s -f -o /dev/null -w "%{http_code}" "$HEALTH_ENDPOINT" | grep -q "200"; then
    echo -e "${GREEN}✅ Health Check 성공${NC}"
else
    echo -e "${YELLOW}⚠️  Health Check 실패 (엔드포인트가 없을 수 있음)${NC}"
fi

# 2. Products API 호출 (데이터베이스 연결 확인)
echo ""
echo "2️⃣ Products API 호출..."
RESPONSE_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$PRODUCTS_ENDPOINT")
if [ "$RESPONSE_CODE" -eq "200" ]; then
    echo -e "${GREEN}✅ Products API 응답 성공 (HTTP $RESPONSE_CODE)${NC}"
else
    echo -e "${RED}❌ Products API 응답 실패 (HTTP $RESPONSE_CODE)${NC}"
fi

# 3. 응답 시간 측정
echo ""
echo "3️⃣ 응답 시간 측정..."
RESPONSE_TIME=$(curl -s -o /dev/null -w "%{time_total}" "$PRODUCTS_ENDPOINT")
echo -e "${GREEN}⏱️  응답 시간: ${RESPONSE_TIME}초${NC}"

echo ""
echo "🎉 Keep-Alive 완료!"
echo "---"

