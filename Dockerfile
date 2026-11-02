FROM gcr.io/distroless/java25-debian13:nonroot@sha256:a90a5fadd78dc37b643da72dca2f744ef67eba625eca7227a5b375b548e6b1c2
WORKDIR /deployments
COPY --chown=65532:65532 target/sobek.jar sobek.jar
COPY --chown=65532:65532 docker/data /deployments/data
EXPOSE 8780
CMD ["sobek.jar"]