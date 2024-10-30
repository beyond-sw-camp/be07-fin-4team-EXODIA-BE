FROM openjdk:17-jdk-alpine AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY src src
COPY build.gradle .
COPY settings.gradle .

RUN chmod +x ./gradlew
RUN ./gradlew bootJar --info

FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]

# Dockerfile.getaroom
FROM node:alpine
RUN apk add --no-cache git vim openssl \
    && mkdir -p /usr/src/app \
    && cd /usr/src/app \
    && git clone https://github.com/OpenVidu/openvidu-tutorials.git \
    && npm install -g http-server \
    && openssl req -newkey rsa:2048 -new -nodes -x509 -days 3650 -subj '//CN=www.mydom.com\O=My Company LTD.\C=US' -keyout key.pem -out cert.pem

WORKDIR /usr/src/app/

CMD ["http-server", "-S", "-C", "cert.pem", "-K", "key.pem", "openvidu-tutorials/openvidu-getaroom/web"]

EXPOSE 80 443 8080
