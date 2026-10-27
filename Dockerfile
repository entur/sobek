FROM eclipse-temurin:25-jre-alpine AS builder
WORKDIR /deployments
COPY target/sobek.jar sobek.jar
RUN mkdir -p /deployments/data \
    && chown -R 2000:2000 /deployments/data

FROM gcr.io/distroless/java25-debian13:nonroot
WORKDIR /deployments
COPY --from=builder --chown=nonroot:nonroot /deployments/sobek.jar sobek.jar
COPY --from=builder --chown=nonroot:nonroot /deployments/data /deployments/data
EXPOSE 8780
CMD ["sobek.jar"]