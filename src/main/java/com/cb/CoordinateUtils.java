package com.cb;

import java.util.ArrayList;
import java.util.List;

/**
 * Various methods for transcoding numbers
 */
public class CoordinateUtils {

    private static final double TILE_SIZE = 256;

    /**
     * Converts longitude/latitude to Web Mercator coordinates (EPSG:3857).
     * @param lon The longitude
     * @param lat The latitude
     * @return A pair of mercator coordinates
     */
    public static List<Double> toWebMercator(double lon, double lat) {
        lon = Math.max(-180, Math.min(180, lon));
        lat = Math.max(-85.05112878, Math.min(85.05112878, lat));

        double x = lon * 20037508.34 / 180;
        double y = Math.log(Math.tan((90 + lat) * Math.PI / 360)) / (Math.PI / 180);
        y = y * 20037508.34 / 180;
        List<Double> list = new ArrayList<>();
        list.add(x);
        list.add(y);
        return list;
    }

    /**
     * Converts longitude and latitude to tile numbers.
     *
     * @param lon  Longitude (-180 to 180)
     * @param lat  Latitude (-90 to 90)
     * @param zoom Zoom level (0 to 20+)
     * @return An array containing the tile numbers [x, y]
     */
    public static int[] toTileNumbers(double lon, double lat, int zoom) {
        // Calculate x and y tile numbers
        int xTile = (int) Math.floor((lon + 180) / 360 * (1 << zoom));
        int yTile = (int) Math.floor((1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * (1 << zoom));

        return new int[]{xTile, yTile};
    }

    /**
     * Converts Web Mercator coordinates to pixel coordinates relative to the TILE_SIZE.
     * @param lon Longitude
     * @param lat Latitude
     * @param zoom Zoom level 0-20+
     * @return A pair of pixel coordinates
     */
    public static int[] toPixelCoordinates(double lon, double lat, int zoom) {
        List<Double> merc = toWebMercator(lon, lat);
        double scale = TILE_SIZE * (1 << zoom) / 20037508.34 / 2;
        int pixelX = (int) ((merc.get(0) + 20037508.34) * scale);
        int pixelY = (int) ((20037508.34 - merc.get(1)) * scale);
        return new int[]{pixelX, pixelY};
    }
}
