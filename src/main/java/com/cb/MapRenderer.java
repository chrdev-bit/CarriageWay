package com.cb;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/**
 * This class deals with creating the graphics
 */
public class MapRenderer {

    private static final int TILE_SIZE = 256;

    /**
     * Creates a BufferedImage of tiles which span the area
     * @param area The Area object
     * @param zoom The chosen zoom level
     * @return the image
     */
    public BufferedImage renderMap(Area area, int zoom) {

        List<List<Double>> polygon = area.getGeometry().getCoordinates();
        List<List<Double>> mercatorCoordinates = new ArrayList<>();

        //we need to know the bounds so that correct tiles can be used
        int minX=Integer.MAX_VALUE, maxX=Integer.MIN_VALUE, minY=Integer.MAX_VALUE, maxY=Integer.MIN_VALUE;

        for(List<Double> xy:polygon){
            int [] tileXY = CoordinateUtils.toTileNumbers(xy.get(0),xy.get(1),zoom);
            if(tileXY[0]<minX) minX=tileXY[0];
            else if(tileXY[0]>maxX) maxX=tileXY[0];
            if(tileXY[1]<minY) minY=tileXY[1];
            else if(tileXY[1]>maxY) maxY=tileXY[1];

            mercatorCoordinates.add(CoordinateUtils.toWebMercator(xy.get(0), xy.get(1)));
        }

        List<Zone> zones = area.getCurbZones();
        Map<Zone,List<List<Double>>> zoneMercs = new LinkedHashMap<>();
        for (Zone zone : zones) {
            List<List<Double>> zoneCoordinates = zone.getGeometry().getCoordinates();
            for(List<Double> xy:zoneCoordinates) {
                int[] tileXY = CoordinateUtils.toTileNumbers(xy.get(0), xy.get(1), zoom);
                if (tileXY[0] < minX) minX = tileXY[0];
                else if (tileXY[0] > maxX) maxX = tileXY[0];
                if (tileXY[1] < minY) minY = tileXY[1];
                else if (tileXY[1] > maxY) maxY = tileXY[1];
                List<List<Double>> merc = zoneMercs.get(zone);
                if (merc == null){
                    merc = new ArrayList<>();
                }
                List<Double> mercs = CoordinateUtils.toWebMercator(xy.get(0), xy.get(1));
                merc.add(mercs);
                zoneMercs.put(zone,merc);
            }

        }

        BufferedImage bim =  new BufferedImage(((maxX-minX)+1)*TILE_SIZE, ((maxY-minY)+1)*TILE_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bim.createGraphics();
        g.setPaint ( new Color ( 200, 200, 200) );
        g.fillRect ( 0, 0, bim.getWidth(), bim.getHeight() );
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int X=0, Y;

        for (int x = minX; x <= maxX; x++) {
            Y=0;
            for (int y = minY; y <= maxY; y++) {
                try {
                    BufferedImage tile = getTile(zoom, x, y);
                    g.drawImage(tile, X * TILE_SIZE, Y * TILE_SIZE, null);
                }catch(Throwable t){
                    t.printStackTrace();
                }
                Y++;
            }
            X++;
        }

        g.setColor(new Color(0, 255, 0, 255));
        g.setStroke(new BasicStroke(2));

        drawShape(g,mercatorCoordinates,minX,minY,zoom);

        g.setColor(new Color(255, 0, 0, 255));

        for (Map.Entry<Zone, List<List<Double>>> entry : zoneMercs.entrySet()) {
            List<List<Double>> value = entry.getValue();
            drawShape(g,value,minX,minY,zoom);
        }

        g.dispose();
        return bim;
    }

    /**
     * Draws a shape onto the BufferedImage
     * @param g The graphics object
     * @param mercatorCoordinates The converted coordinates
     * @param zoom The zoom level
     */
    private void drawShape(Graphics2D g, List<List<Double>> mercatorCoordinates, int minX, int minY,int zoom){

        Path2D.Double path = new Path2D.Double();
        boolean firstPoint = true;

        for (List<Double> mercatorCoord : mercatorCoordinates) {
            int[] pixelCoord = CoordinateUtils.toPixelCoordinates(mercatorCoord.get(0), mercatorCoord.get(1), zoom);
            int x = pixelCoord[0] - (minX * TILE_SIZE);
            int y = pixelCoord[1] - (minY * TILE_SIZE);
            if (firstPoint) {
                path.moveTo(x, y);
                firstPoint = false;
            } else {
                path.lineTo(x, y);
            }
        }
        path.closePath();
        g.draw(path);
    }

    /**
     * Gets a single tile from cache
     * @param zoom Zoom level (0 to 20+)
     * @param x The x coordinate
     * @param y The y coordinate
     * @return The tile at x,y,x
     */
    public BufferedImage getTile(int zoom, int x, int y) throws Throwable {
        String name = "/"+zoom+"-"+x+"-"+y+".jpeg";
        return ImageIO.read(getClass().getResourceAsStream(name));
    }
}
