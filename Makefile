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
	cd docker && docker compose up -d

dstop:
	cd docker && docker compose down
	
	
release: clean package

drelease: release copy dbuild

startb: clean package copy dbuild dstart

restartb: dstop clean package copy dbuild dstart

stop: dstop

remove: dstop dstop
