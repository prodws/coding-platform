# syntax=docker/dockerfile:1
FROM eclipse-temurin:21-jdk AS base
WORKDIR /workspace
ARG JUNIT_PLATFORM_VERSION=1.12.2

RUN apt-get update && apt-get install -y --no-install-recommends curl ca-certificates \
    && rm -rf /var/lib/apt/lists/* \
    && mkdir -p /opt/junit \
    && curl -fsSL -o /opt/junit/junit-platform-console-standalone.jar \
    "https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/${JUNIT_PLATFORM_VERSION}/junit-platform-console-standalone-${JUNIT_PLATFORM_VERSION}.jar"

COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]