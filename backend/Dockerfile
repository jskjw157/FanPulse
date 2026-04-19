# ── Stage 1: Build ──
FROM gradle:8-jdk17 AS builder
WORKDIR /app

# 의존성 캐싱 레이어
COPY build.gradle.kts settings.gradle.kts gradle.properties* ./
COPY gradle ./gradle
RUN gradle dependencies --no-daemon || true

# 소스 복사 + 빌드
COPY src ./src
RUN gradle bootJar -x test --no-daemon

# ── Stage 2: Runtime ──
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

ARG YT_DLP_VERSION=2026.03.17
ARG YT_DLP_SHA256=3bda0968a01cde70d26720653003b28553c71be14dcb2e5f4c24e9921fdad745
# Deno: webhook-channel 서버 런타임 (GitHub PR 리뷰 자동화)
ARG DENO_VERSION=v2.7.12
RUN apt-get update && \
    apt-get install -y --no-install-recommends python3 ffmpeg curl unzip && \
    curl -fsSL https://github.com/yt-dlp/yt-dlp/releases/download/${YT_DLP_VERSION}/yt-dlp -o /usr/local/bin/yt-dlp && \
    echo "${YT_DLP_SHA256}  /usr/local/bin/yt-dlp" | sha256sum -c - && \
    chmod a+rx /usr/local/bin/yt-dlp && \
    ARCH=$(dpkg --print-architecture) && \
    case "${ARCH}" in \
      amd64) DENO_ARCH="x86_64-unknown-linux-gnu" DENO_SHA="4646ec32b4296fdf963a62a3df3d57e342a3c70376976e5690c34d42e23a3080" ;; \
      arm64) DENO_ARCH="aarch64-unknown-linux-gnu" DENO_SHA="4116c1bb5fa2bba348a1f89c5392d5ecdbb505dd7166796870660556ea6c6c53" ;; \
      *) echo "Unsupported arch: ${ARCH}" && exit 1 ;; \
    esac && \
    curl -fsSL "https://github.com/denoland/deno/releases/download/${DENO_VERSION}/deno-${DENO_ARCH}.zip" -o /tmp/deno.zip && \
    echo "${DENO_SHA}  /tmp/deno.zip" | sha256sum -c - && \
    unzip -o /tmp/deno.zip -d /usr/local/bin && \
    chmod a+rx /usr/local/bin/deno && \
    rm /tmp/deno.zip && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

RUN groupadd -r appgroup && useradd -r -g appgroup -s /bin/false appuser

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

USER appuser
ENTRYPOINT ["java", "-jar", "app.jar"]
