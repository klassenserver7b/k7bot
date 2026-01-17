FROM azul/zulu-openjdk-alpine:25-latest
WORKDIR /opt/k7bot

COPY Bot.jar ./Bot.jar
RUN apk add --no-cache msttcorefonts-installer fontconfig gcompat
RUN update-ms-fonts

VOLUME /opt/k7bot/logs
VOLUME /opt/k7bot/resources
VOLUME /tmp
VOLUME /media

RUN addgroup -S k7bot && adduser -S --ingroup k7bot k7bot
USER k7bot

CMD ["java", "-jar", "./Bot.jar"]
