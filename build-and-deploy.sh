#!/bin/bash

# 로컬에서 빌드하고 Docker 이미지를 생성하는 스크립트
# Render에서 이 스크립트를 사용하거나, 로컬에서 빌드 후 배포할 수 있습니다.

set -e

echo "🔨 Gradle 빌드 시작..."
./gradlew clean bootJar

echo "🐳 Docker 이미지 빌드 시작..."
docker build -f Dockerfile.jre -t lian-shop-backend:latest .

echo "✅ 빌드 완료!"
echo ""
echo "다음 명령어로 로컬에서 실행할 수 있습니다:"
echo "  docker run -p 8080:8080 lian-shop-backend:latest"
echo ""
echo "또는 Docker Hub에 푸시:"
echo "  docker tag lian-shop-backend:latest YOUR_DOCKERHUB_USERNAME/lian-shop-backend:latest"
echo "  docker push YOUR_DOCKERHUB_USERNAME/lian-shop-backend:latest"

