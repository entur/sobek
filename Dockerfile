FROM eclipse-temurin:25-jre-alpine@sha256:b7528962e65d43064d8a8b67ba175367896bc948363ea2d3fcd7f10d34e1f048 AS builder
WORKDIR /deployments
COPY target/sobek.jar sobek.jar
RUN mkdir -p /deployments/data \
    && touch /deployments/data/.keep \
    && chown -R 65532:65532 /deployments \
    && chmod -R u+rwX /deployments/data

FROM gcr.io/distroless/java25-debian13:nonroot@sha256:5d3177252775d4b01603470fe21c015393019089904f68724bd37242e4bc6e6d
WORKDIR /deployments
COPY --from=builder --chown=65532:65532 /deployments/sobek.jar sobek.jar
COPY --from=builder --chown=65532:65532 /deployments/data /deployments/data
EXPOSE 8780
CMD ["sobek.jar"]