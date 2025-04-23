# 빌드 스테이지: Gradle을 사용하여 애플리케이션 빌드
FROM gradle:8.10-jdk17 AS build
WORKDIR /app

# 의존성 먼저 복사 및 다운로드 (캐시 활용)
COPY build.gradle settings.gradle ./
RUN gradle dependencies --no-daemon

# 소스 코드 복사 및 빌드
COPY . .
RUN gradle build --no-daemon -x test --stacktrace

# 실행 스테이지: JRE를 사용하여 애플리케이션 실행
FROM eclipse-temurin:17-jre
WORKDIR /app

# 애플리케이션 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 컨테이너 메모리 인식 및 자동 조정 설정
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=70 -XX:MinRAMPercentage=50 -XX:InitialRAMPercentage=50"

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT java $JAVA_OPTS -jar app.jar
