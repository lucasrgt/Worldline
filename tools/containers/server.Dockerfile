ARG BASE_IMAGE=eclipse-temurin:21-jdk-alpine
FROM ${BASE_IMAGE}

RUN addgroup -S -g 10001 worldline \
    && adduser -S -D -H -u 10001 -G worldline worldline \
    && mkdir -p /workspace/.worldline/smokes /workspace/local/artifacts /tmp/home \
    && touch /workspace/local/artifacts/minecraft-b1.7.3-server.jar \
    && chown -R worldline:worldline /workspace /tmp/home

WORKDIR /workspace
COPY --chown=worldline:worldline . /workspace

USER 10001:10001
ENV HOME=/tmp/home
ENTRYPOINT ["java"]
