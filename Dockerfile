FROM gcr.io/distroless/java25-debian13:nonroot@sha256:8ac5525549d14b8ddd34562f9fc8e6ba4840c995363a1507b17cadf8bb7ab57e
WORKDIR /deployments
COPY --chown=65532:65532 target/sobek.jar sobek.jar
COPY --chown=65532:65532 docker/data /deployments/data
EXPOSE 8780
CMD ["sobek.jar"]