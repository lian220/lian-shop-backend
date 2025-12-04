#!/bin/bash

# Backend 실행 스크립트
# Lian Shop Backend (Spring Boot + Kotlin)

echo "🚀 Backend 서버를 시작합니다..."
echo "============================================"
echo "프로젝트: Lian Shop Backend"
echo "프레임워크: Spring Boot + Kotlin"
echo "포트: 8080 (기본값)"
echo "============================================"
echo ""

# 현재 디렉토리 확인 (backend 폴더에서 실행되어야 함)
if [ ! -f "build.gradle" ]; then
    echo "❌ build.gradle 파일을 찾을 수 없습니다."
    echo "   backend 폴더에서 이 스크립트를 실행해주세요."
    exit 1
fi

# Gradle Wrapper에 실행 권한 부여
chmod +x ./gradlew

# Spring Boot 애플리케이션 실행
echo "📦 Gradle을 사용하여 애플리케이션을 실행합니다..."
./gradlew bootRun

# 오류 발생 시 메시지 출력
if [ $? -ne 0 ]; then
    echo ""
    echo "❌ Backend 서버 실행 중 오류가 발생했습니다."
    echo "   - Java 21이 설치되어 있는지 확인하세요."
    echo "   - PostgreSQL이 실행 중인지 확인하세요."
    echo "   - application.yml 설정을 확인하세요."
    exit 1
fi

