docker_compose_file := "/opt/docker-container/k7bot/compose.yml"
docker_image := "klassenserver7b/k7bot"

# Build the project (default)
default: package

# Remove build artifacts
clean:
    mvn clean

# Build the JAR
package:
    mvn clean package

# ── Local Docker operations ────────────────────────────────────────────────────

# Stop the local bot container
dstop:
    docker compose -f {{ docker_compose_file }} down

# Pull the latest image from Docker Hub
dpull:
    docker compose -f {{ docker_compose_file }} pull

# Start the local bot container
dstart:
    docker compose -f {{ docker_compose_file }} up -d

# Stop, pull latest image, and restart (full update cycle)
dupdate: dstop dpull dstart
    @echo "Local bot updated to latest version"

# Build JAR, build local Docker image, and restart the local instance
local-update: package
    cp target/k7bot-*-full.jar docker/Bot.jar
    docker build -t {{ docker_image }}:local ./docker
    docker compose -f {{ docker_compose_file }} down
    docker compose -f {{ docker_compose_file }} up -d
    @echo "Local bot rebuilt and restarted with your changes"

# ── Docker build & publish ─────────────────────────────────────────────────────

# Build and tag the Docker image locally (tag defaults to "local")
docker-build tag="local": package
    cp target/k7bot-*-full.jar docker/Bot.jar
    docker build -t {{ docker_image }}:{{ tag }} ./docker
    @echo "Built {{ docker_image }}:{{ tag }}"

# Push a previously built image to Docker Hub (tag defaults to "local")
docker-push tag="local":
    docker push {{ docker_image }}:{{ tag }}
    @echo "Pushed {{ docker_image }}:{{ tag }}"

# Build, tag, and push to Docker Hub in one step; also tags as :latest
# Usage: just docker-publish 1.28.2.1
docker-publish version: (docker-build version)
    docker tag {{ docker_image }}:{{ version }} {{ docker_image }}:latest
    docker push {{ docker_image }}:{{ version }}
    docker push {{ docker_image }}:latest
    @echo "Published {{ docker_image }}:{{ version }} and :latest"

# ── Legacy aliases ─────────────────────────────────────────────────────────────

# Alias for package
release: package

# Alias for dstop
stop: dstop

# Alias for dupdate
restart: dupdate
