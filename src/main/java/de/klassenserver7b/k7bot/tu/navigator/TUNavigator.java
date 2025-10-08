package de.klassenserver7b.k7bot.tu.navigator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class TUNavigator {
    static final Logger logger = LoggerFactory.getLogger(TUNavigator.class);

    public static final String API_BASE = "https://navigator.tu-dresden.de";

    public static final int TILE_SIZE = 480;
    public static final int TILE_QUALITY = 2;
    public static final int TILE_BACKGROUND_PADDING = 20;

    private File tileCacheDir;

    private final Cache cache;
    private final TileBackgroundLoader tileBackgroundLoader;

    public TUNavigator() {
        try {
            this.tileCacheDir = Files.createTempDirectory("k7bot_tunav").toFile();
        } catch (IOException e) {
            this.tileCacheDir = new File(".cache");
        }

        cache = new Cache();
        tileBackgroundLoader = new TileBackgroundLoader(API_BASE);
    }

    public void shutdown() {
        cache.shutdown();
        tileBackgroundLoader.shutdown();
    }

    public Set<String> getBuildingNames() {
        JsonElement response = getBuildings();
        if (response == null) return new HashSet<>();

        try {
            return StreamSupport.stream(response.getAsJsonArray().spliterator(), false)
                    .map(JsonElement::getAsJsonObject)
                    .map(b -> b.get("krz").getAsString())
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            logger.error("could not parse buildings json", e);
            return new HashSet<>();
        }
    }

    public Set<String> getRoomNames(String building) {
        JsonElement response = getFloors(building);
        if (response == null) return new HashSet<>();

        try {
            return StreamSupport.stream(response.getAsJsonArray().spliterator(), false)
                    .map(floor -> floor.getAsJsonObject().get("typen").getAsJsonArray())
                    .flatMap(types -> StreamSupport.stream(types.spliterator(), false)
                            .map(type -> type.getAsJsonObject().get("räume").getAsJsonArray())
                            .flatMap(rooms -> StreamSupport.stream(rooms.spliterator(), false)
                                    .map(JsonElement::getAsJsonObject)
                                    .filter(room -> room.has("name"))
                                    .map(room -> room.get("name").getAsString())))
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            logger.error("could not parse floors json for building " + building, e);
            return new HashSet<>();
        }
    }

    public JsonElement getBuilding(String shortName) {
        JsonElement response = getBuildings();
        if (response == null) return null;

        try {
            return StreamSupport.stream(response.getAsJsonArray().spliterator(), false)
                    .map(JsonElement::getAsJsonObject)
                    .filter(b -> shortName.equals(b.get("krz").getAsString()))
                    .findFirst().orElse(null);
        } catch (Exception e) {
            logger.error("could not parse buildings json", e);
            return null;
        }
    }

    public String getBuildingNameByPartId(String partId) {
        JsonElement response = getBuildings();
        if (response == null) return null;

        try {
            return StreamSupport.stream(response.getAsJsonArray().spliterator(), false)
                    .map(JsonElement::getAsJsonObject)
                    .filter(building -> StreamSupport.stream(building.get("teilgeb").getAsJsonArray().spliterator(), false)
                            .map(JsonElement::getAsJsonObject)
                            .map(p -> p.get("gebnr").getAsString())
                            .anyMatch(partId::equals))
                    .map(building -> building.get("krz").getAsString())
                    .findFirst().orElse(null);
        } catch (Exception e) {
            logger.error("could not parse buildings json", e);
            return null;
        }
    }

    public JsonElement getFloor(String building, String floor) {
        JsonElement response = getFloors(building);
        if (response == null) return null;

        try {
            return StreamSupport.stream(response.getAsJsonArray().spliterator(), false)
                    .map(JsonElement::getAsJsonObject)
                    .filter(b -> floor.equals(b.get("etage").getAsString()))
                    .findFirst().orElse(null);
        } catch (Exception e) {
            logger.error("could not parse floors json for building " + building, e);
            return null;
        }
    }

    public String getFloorName(String building, String room) {
        JsonElement response = getFloors(building);
        if (response == null) return null;

        try {
            return StreamSupport.stream(response.getAsJsonArray().spliterator(), false)
                    .filter(f -> StreamSupport.stream(f.getAsJsonObject().get("typen").getAsJsonArray().spliterator(), false)
                            .flatMap(t -> StreamSupport.stream(t.getAsJsonObject().get("räume").getAsJsonArray().spliterator(), false)
                                    .map(JsonElement::getAsJsonObject)
                                    .filter(r -> r.has("name"))
                                    .map(r -> r.get("name").getAsString()))
                            .anyMatch(room::equals))
                    .findFirst().map(f -> f.getAsJsonObject().get("etage").getAsString()).orElse(null);
        } catch (Exception e) {
            logger.error("could not parse floors json for building " + building, e);
            return null;
        }
    }

    public String getBuildingPartId(String building, String room) {
        JsonElement response = getFloors(building);
        if (response == null) return null;

        try {
            return StreamSupport.stream(response.getAsJsonArray().spliterator(), false)
                    .flatMap(f -> StreamSupport.stream(f.getAsJsonObject().get("typen").getAsJsonArray().spliterator(), false)
                            .flatMap(t -> StreamSupport.stream(t.getAsJsonObject().get("räume").getAsJsonArray().spliterator(), false)
                                    .map(JsonElement::getAsJsonObject)))
                    .filter(r -> r.has("name"))
                    .filter(r -> room.equals(r.get("name").getAsString()))
                    .findFirst().map(f -> f.getAsJsonObject().get("id").getAsString().substring(0, 4)).orElse(null);
        } catch (Exception e) {
            logger.error("could not parse floors json for building " + building, e);
            return null;
        }
    }

    public String getRoomNameById(String building, String roomId) {
        JsonElement response = getFloors(building);
        if (response == null) return null;

        try {
            return StreamSupport.stream(response.getAsJsonArray().spliterator(), false)
                    .flatMap(f -> StreamSupport.stream(f.getAsJsonObject().get("typen").getAsJsonArray().spliterator(), false)
                            .flatMap(t -> StreamSupport.stream(t.getAsJsonObject().get("räume").getAsJsonArray().spliterator(), false)
                                    .map(JsonElement::getAsJsonObject)))
                    .filter(r -> roomId.equals(r.get("id").getAsString()))
                    .findFirst().map(r -> r.getAsJsonObject().get("name").getAsString()).orElse(null);
        } catch (Exception e) {
            logger.error("could not parse floors json for building " + building, e);
            return null;
        }
    }

    public JsonElement getBuildings() {
        return cache.request(API_BASE + "/m/json_gebaeude/all");
    }

    public JsonElement getFloors(String building) {
        return cache.request(API_BASE + "/m/json_etagen/" + building);
    }

    public BufferedImage getBackground(String building, String floor) {
        File cacheFile = tileCacheDir.toPath().resolve(building + "-" + floor + "-" + TILE_QUALITY + ".png").toFile();
        try {
            if (cacheFile.exists())
                return ImageIO.read(cacheFile);
        } catch (IOException ignored) {
        }

        double contentWidth = 480.0;
        double contentHeight = 480.0;

        JsonElement floorInfo = getFloor(building, floor);
        if (floorInfo != null) {
            try {
                contentWidth = floorInfo.getAsJsonObject().get("maxX").getAsDouble();
                contentHeight = floorInfo.getAsJsonObject().get("maxY").getAsDouble();
            } catch (Exception e) {
                logger.error("could not parse floors json for building " + building, e);
            }
        }

        BufferedImage image = tileBackgroundLoader.requestImage(building, floor, contentWidth, contentHeight,
                TILE_QUALITY, TILE_SIZE, TILE_BACKGROUND_PADDING);
        try {
            ImageIO.write(image, "png", cacheFile);
        } catch (IOException ignored) {
        }

        return image;
    }

    public void renderFloor(String building, String floor, String highlightRoom, Graphics2D gfx) {
        JsonElement response = getFloors(building);
        if (response == null) return;

        JsonElement floorData = StreamSupport.stream(response.getAsJsonArray().spliterator(), false)
                .filter(f -> floor.equals(f.getAsJsonObject().get("etage").getAsString()))
                .findFirst().orElse(null);
        if (floorData == null) return;

        HashMap<String, Color> colors = new HashMap<>();
        for (String entry : floorData.getAsJsonObject().get("raumf").getAsString().split("\\|")) {
            String[] parts = entry.split(":");
            if (parts.length != 2) continue;

            try {
                Color color = Color.decode(parts[1]);
                colors.put(parts[0], color);
            } catch (NumberFormatException ignored) {
            }
        }

        for (JsonElement typeGroup : floorData.getAsJsonObject().get("typen").getAsJsonArray()) {
            String type = typeGroup.getAsJsonObject().get("typ").getAsString();

            Color color = colors.get(type);
            if (color == null) continue;

            for (JsonElement room : typeGroup.getAsJsonObject().get("räume").getAsJsonArray()) {
                JsonObject roomObject = room.getAsJsonObject();
                Polygon polygon = new Polygon();

                for (JsonElement point : roomObject.get("punkte").getAsJsonArray()) {
                    int x = (int) (point.getAsJsonObject().get("x").getAsDouble() * TILE_QUALITY);
                    int y = (int) (point.getAsJsonObject().get("y").getAsDouble() * TILE_QUALITY);

                    polygon.addPoint(x + TILE_BACKGROUND_PADDING, y + TILE_BACKGROUND_PADDING);
                }

                String name = roomObject.has("name") ? roomObject.get("name").getAsString() : null;

                if (Objects.equals(name, highlightRoom))
                    gfx.setColor(Color.RED);
                else gfx.setColor(color);
                gfx.fillPolygon(polygon);
            }
        }
    }

    public void renderFloorLabels(String building, String floor, Graphics2D gfx) {
        JsonElement response = getFloors(building);
        if (response == null) return;

        JsonElement floorData = StreamSupport.stream(response.getAsJsonArray().spliterator(), false)
                .filter(f -> floor.equals(f.getAsJsonObject().get("etage").getAsString()))
                .findFirst().orElse(null);
        if (floorData == null) return;

        for (JsonElement typeGroup : floorData.getAsJsonObject().get("typen").getAsJsonArray()) {
            String type = typeGroup.getAsJsonObject().get("typ").getAsString();

            for (JsonElement room : typeGroup.getAsJsonObject().get("räume").getAsJsonArray()) {
                JsonObject roomObject = room.getAsJsonObject();
                String name = roomObject.has("name") ? roomObject.get("name").getAsString() : null;
                if (name == null) continue;

                double nameX = roomObject.get("namex").getAsDouble() * TILE_QUALITY + TILE_BACKGROUND_PADDING;
                double nameY = roomObject.get("namey").getAsDouble() * TILE_QUALITY + TILE_BACKGROUND_PADDING;

                Font font = gfx.getFont();
                FontMetrics metrics = gfx.getFontMetrics(font);
                nameX -= (double) metrics.stringWidth(name) / 2.0;
                nameY -= (double) metrics.getHeight() / 2.0 - (double) metrics.getAscent();

                gfx.setColor(Color.BLACK);
                gfx.drawString(name, (int) nameX, (int) nameY);
            }
        }
    }

    public String getMapsUrl(String building, String partId) {
        JsonElement response = getBuilding(building);
        if (response == null) return null;

        try {
            JsonArray parts = response.getAsJsonObject().get("teilgeb").getAsJsonArray();
            JsonObject part = StreamSupport.stream(parts.spliterator(), false)
                    .map(JsonElement::getAsJsonObject)
                    .filter(p -> Objects.equals(p.get("gebnr").getAsString(), partId))
                    .findFirst()
                    .orElse(parts.get(0).getAsJsonObject());

            String searchQuery = part.get("name").getAsString().replaceFirst("^[A-Z]{3}\\s+", "") + ", " + part.get("plz").getAsString() + ", " + part.get("ort").getAsString();

            URIBuilder uri = new URIBuilder("https://www.google.com/maps/search/");
            uri.addParameter("api", "1");
            uri.addParameter("query", searchQuery);

            return uri.build().toString();
        } catch (Exception e) {
            logger.error("could not find address for building " + building, e);
            return null;
        }
    }

    public record Target(String building, String partId, String floor, String room) {
    }

    public Target resolveTarget(String query) {
        String[] parts = query.split("/", -1);
        if (parts.length == 0) return null;

        if (parts.length == 1) {
            Target target = resolveBuildingTarget(parts[0]);
            if (target != null) return target;

            return resolveSearch(query);
        }

        Target target = resolveRoomTarget(parts[0], parts[1]);
        if (target != null) return target;

        return resolveSearch(query);
    }

    private Target resolveSearch(String query) {
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            String uri = new URIBuilder(API_BASE + "/api/search")
                    .addParameter("q", query)
                    .build().toString();
            HttpGet request = new HttpGet(uri);
            JsonArray results = client.execute(request, response -> {
                String content = EntityUtils.toString(response.getEntity());
                return JsonParser.parseString(content).getAsJsonObject().get("results").getAsJsonArray();
            });

            List<JsonObject> potentialResults = StreamSupport.stream(results.spliterator(), false)
                    .map(JsonElement::getAsJsonObject)
                    .filter(r -> "room".equalsIgnoreCase(r.get("type").getAsString())
                            || "building".equalsIgnoreCase(r.get("type").getAsString()))
                    .toList();

            JsonObject result = potentialResults.stream()
                    .filter(r -> query.equals(r.get("name").getAsString())
                            || query.equals(r.get("id").getAsString()))
                    .findFirst()
                    .or(() -> potentialResults.stream().findFirst())
                    .orElse(null);
            if (result == null) return null;

            String type = result.get("type").getAsString();
            String id = result.get("id").getAsString();

            if ("room".equalsIgnoreCase(type))
                return resolveRoomIdTarget(id);
            else
                return resolveBuildingTarget(id);


        } catch (Exception e) {
            return null;
        }
    }

    private Target resolveBuildingTarget(String building) {
        JsonElement buildingInfo = getBuilding(building);
        if (buildingInfo == null) return null;

        String floor = buildingInfo.getAsJsonObject().get("stdetage").getAsString();
        return new Target(building, null, floor, null);
    }

    private Target resolveRoomTarget(String building, String room) {
        String partId = getBuildingPartId(building, room);
        String floor = getFloorName(building, room);
        if (floor == null) return null;

        return new Target(building, partId, floor, room);
    }

    private Target resolveRoomIdTarget(String roomId) {
        String partId = roomId.substring(0, 4);
        String building = getBuildingNameByPartId(partId);
        if (building == null) return null;
        String room = getRoomNameById(building, roomId);
        if (room == null) return null;
        String floor = getFloorName(building, room);
        if (floor == null) return null;

        return new Target(building, partId, floor, room);
    }
}
