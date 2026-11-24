FROM azul-zulu:25-jre-headless-alpine
WORKDIR /opt/k7bot

RUN apk add --no-cache fontconfig freetype font-dejavu gcompat opus libsodium libstdc++ libgcc
RUN addgroup -S k7bot && adduser -S --ingroup k7bot k7bot

VOLUME ["/opt/k7bot/logs", "/opt/k7bot/resources", "/tmp", "/media"]

COPY --chown=k7bot:k7bot Bot.jar ./Bot.jar

USER k7bot

CMD ["java", "-jar", "./Bot.jar"]
