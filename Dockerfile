FROM gradle:8.5-jdk21 AS builder
WORKDIR /app

# Gradle wrapper 파일들을 먼저 복사 (캐시 최적화)
COPY gradlew* ./
COPY gradle/wrapper/ gradle/wrapper/
RUN chmod +x ./gradlew

# Gradle 설정 파일 복사 (의존성 캐시 최적화)
COPY build.gradle settings.gradle.kts ./

# 의존성만 먼저 다운로드 (캐시 활용)
RUN ./gradlew dependencies --no-daemon || true

# 소스 코드 복사
COPY src ./src

# 빌드 실행 (캐시된 의존성 사용)
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
