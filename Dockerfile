FROM gcr.io/distroless/java25-debian13:nonroot@sha256:5d3177252775d4b01603470fe21c015393019089904f68724bd37242e4bc6e6d
WORKDIR /deployments
COPY --chown=65532:65532 target/sobek.jar sobek.jar
COPY --chown=65532:65532 docker/data /deployments/data
EXPOSE 8780
CMD ["sobek.jar"]