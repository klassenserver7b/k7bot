default: clean package

clean:
	mvn clean

package:
	mvn package

copy:
	cp target/k7bot-*-full.jar docker/Bot.jar

dbuild:
	docker build -t klassenserver7b/k7bot ./docker

dstart:
	docker compose -f /opt/docker-container/k7bot/docker-compose.yml up -d

dstop:
	docker compose -f /opt/docker-container/k7bot/docker-compose.yml down

release: clean package

drelease: release copy dbuild

startb: clean package copy dbuild dstart

restartb: dstop clean package copy dbuild dstart

stop: dstop

remove: dstop dstop
