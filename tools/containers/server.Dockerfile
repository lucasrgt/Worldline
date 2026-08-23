ARG BASE_IMAGE=eclipse-temurin:21-jdk-alpine
FROM ${BASE_IMAGE}

RUN addgroup -S -g 10001 worldline \
    && adduser -S -D -H -u 10001 -G worldline worldline \
    && mkdir -p /workspace/.worldline/smokes /workspace/local/artifacts /tmp/home \
    && touch /workspace/local/artifacts/minecraft-b1.7.3-server.jar \
    && chown -R worldline:worldline /workspace /tmp/home

WORKDIR /workspace
COPY --chown=worldline:worldline . /workspace

RUN mkdir -p /runtime /workspace/.worldline/runtime-fabric \
    && rm -rf /workspace/.worldline/gate /workspace/.worldline/reports \
       /workspace/.worldline/smoke-logs /workspace/.worldline/smokes \
    && ln -s /runtime/gate /workspace/.worldline/gate \
    && ln -s /runtime/reports /workspace/.worldline/reports \
    && ln -s /runtime/smoke-logs /workspace/.worldline/smoke-logs \
    && ln -s /runtime/smokes /workspace/.worldline/smokes

USER 10001:10001
ENV HOME=/tmp/home
ENTRYPOINT ["java"]
