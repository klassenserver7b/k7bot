package de.klassenserver7b.k7bot.tu.navigator;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class TileBackgroundLoader {
    private final String apiBase;
    private final CloseableHttpClient httpClient;

    public TileBackgroundLoader(String apiBase) {
        this.apiBase = apiBase;
        httpClient = HttpClients.createDefault();
    }

    public void shutdown() {
        try {
            httpClient.close();
        } catch (IOException ignored) {
        }
    }

    public BufferedImage requestImage(String building, String floor, double contentWidth, double contentHeight, int quality, int tileSize, int padding) {
        String id = building + floor;

        int width = (int) Math.ceil(contentWidth * (double) quality / (double) tileSize);
        int height = (int) Math.ceil(contentHeight * (double) quality / (double) tileSize);

        BufferedImage image = new BufferedImage(
                (int) (contentWidth * quality) + 2 * padding,
                (int) (contentHeight * quality) + 2 * padding,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D gfx = image.createGraphics();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                BufferedImage tile = requestTile(id, quality, x, y);
                if (tile == null) continue;

                gfx.drawImage(tile, x * tileSize + padding, y * tileSize + padding, null);
            }
        }

        gfx.dispose();
        return image;
    }

    private BufferedImage requestTile(String id, int quality, int x, int y) {
        HttpGet request = new HttpGet(apiBase + "/images/etplan_cache/" + id + "_" + quality + "/" + x + "_" + y + ".png/nobase64");

        try {
            return httpClient.execute(request, response -> {
                try {
                    return ImageIO.read(response.getEntity().getContent());
                } catch (IOException e) {
                    return null;
                }
            });
        } catch (IOException e) {
            return null;
        }
    }
}
