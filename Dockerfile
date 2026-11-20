FROM gcr.io/distroless/java25-debian13:nonroot@sha256:573ef04c0f84aaf4b34e0cb6eeef91c25a83290334586c4c44ef41ecae13a25e
WORKDIR /deployments
COPY --chown=65532:65532 target/sobek.jar sobek.jar
COPY --chown=65532:65532 docker/data /deployments/data
EXPOSE 8780
CMD ["sobek.jar"]