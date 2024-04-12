# stage 1 using latest GraalVM for java 21 as per https://github.com/graalvm/container/pkgs/container/graalvm-community
FROM ghcr.io/graalvm/graalvm-community:21 AS builder

# build directory
WORKDIR /build

# copy source code into directory
COPY . /build

# maven install in the root of project and native compile webserver directory
RUN ./mvnw clean install -DskipTests \
    && cd webserver/ \
    && ./mvnw --no-transfer-progress -Pnative native:compile -Daot.profiles=default -DskipTests

# stage 2 lightest weight linux OS
FROM amd64/alpine:3.19.1

# since bytecode generated is glibc and OS is Alpine, we need to install gcompat
RUN apk add gcompat

# copy generated bytecode from target directory
COPY --from=builder /build/webserver/target/webserver ./

# set permission
RUN chmod +x ./

# entry point to run bytecode
CMD ["./webserver"]

## stage 1: build stage
#FROM maven:3.9.6-amazoncorretto-21-al2023 as builder
#
## working directory
#WORKDIR /build
#
## copy source code into build directory
#COPY . /build
#
## generate jar file for webserver directory
#RUN mvn clean --no-transfer-progress install -DskipTests
#
## stage 2: run stage
#FROM maven:3.9.6-amazoncorretto-21-al2023
#
## set working directory
#WORKDIR /app
#
## copy jar file into app directory
#COPY --from=builder /build/webserver/target/webserver-exec.jar /app
#
## use entry point instead of command as it cannot be override
#ENTRYPOINT ["java", "-jar", "/app/webserver-exec.jar"]