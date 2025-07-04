default: clean package

clean:
	mvn clean

package:
	mvn clean package

deploy:
	mvn clean deploy

copy:
	cp target/k7bot-*-full.jar docker/Bot.jar

dbuild:
	docker build -t klassenserver7b/k7bot ./docker

dstart:
	docker compose -f /opt/docker-container/k7bot/docker-compose.yml up -d

dstop:
	docker compose -f /opt/docker-container/k7bot/docker-compose.yml down

release: package

startb: deploy

restartb: deploy

stop: dstop

remove: dstop dstop
