FROM gcr.io/distroless/java25-debian13:nonroot
WORKDIR /deployments
COPY --chown=65532:65532 target/sobek.jar sobek.jar
COPY --chown=65532:65532 docker/data /deployments/data
EXPOSE 8780
CMD ["sobek.jar"]