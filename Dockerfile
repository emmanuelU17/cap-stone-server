# stage 1 using latest GraalVM for java 21 as per https://github.com/graalvm/container/pkgs/container/graalvm-community
FROM ghcr.io/graalvm/graalvm-community:21 AS builder

# build directory
WORKDIR /build

# copy source code into directory
COPY webserver /build

# maven install in the root of project and native compile webserver directory
RUN ./mvnw clean install -DskipTests \
    && cd webserver/ \
    && ./mvnw --no-transfer-progress -Pnative native:compile -DskipTests

# stage 2 using a aws linux OS
FROM public.ecr.aws/amazonlinux/amazonlinux:2023

# copy generated bytecode from targert directory
COPY --from=builder /build/webserver/target/webserver ./

# set permission
RUN chmod +x ./

# entry point to run bytecode
CMD ["./webserver"]