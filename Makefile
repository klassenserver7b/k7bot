DOCKER_COMPOSE_FILE=/opt/docker-container/k7bot/docker-compose.yml

default: clean package

clean:
	mvn clean

package:
	mvn clean package

# Local Docker operations - for updating your local bot instance
dstop:
	docker compose -f $(DOCKER_COMPOSE_FILE) down

dpull:
	docker compose -f $(DOCKER_COMPOSE_FILE) pull

dstart:
	docker compose -f $(DOCKER_COMPOSE_FILE) up -d

# Complete local update cycle: stop, pull latest, start
dupdate: dstop dpull dstart
	@echo "Local bot updated to latest version"

# Local development - build and update local instance
local-update: package
	cp target/k7bot-*-full.jar docker/Bot.jar
	docker build -t klassenserver7b/k7bot:local ./docker
	docker compose -f $(DOCKER_COMPOSE_FILE) down
	docker compose -f $(DOCKER_COMPOSE_FILE) up -d
	@echo "Local bot rebuilt and restarted with your changes"

# Legacy aliases
release: package

stop: dstop

# Restart by pulling latest from Docker Hub
restart: dupdate
