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
     * @param lon
     * @param lat
     * @return
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
     * Converts Web Mercator coordinates to pixel coordinates relative to the TILE_SIZE.
     * @param mercatorX
     * @param mercatorY
     * @param zoom
     * @return
     */
    public static int[] toPixelCoordinates(double mercatorX, double mercatorY, int zoom) {
        double scale = TILE_SIZE * (1 << zoom) / 20037508.34 / 2;
        int pixelX = (int) ((mercatorX + 20037508.34) * scale);
        int pixelY = (int) ((20037508.34 - mercatorY) * scale);
        return new int[]{pixelX, pixelY};
    }
}
