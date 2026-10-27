FROM eclipse-temurin:25-jre-alpine AS builder
WORKDIR /deployments
COPY target/sobek.jar sobek.jar
RUN mkdir -p /deployments/data \
    && touch /deployments/data/.keep \
    && chown -R 65532:65532 /deployments \
    && chmod -R u+rwX /deployments/data

FROM gcr.io/distroless/java25-debian13:nonroot
WORKDIR /deployments
COPY --from=builder --chown=65532:65532 /deployments/sobek.jar sobek.jar
COPY --from=builder --chown=65532:65532 /deployments/data /deployments/data
EXPOSE 8780
CMD ["sobek.jar"]