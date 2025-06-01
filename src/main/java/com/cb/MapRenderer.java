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
     * @param area  The Area object
     * @param zoom  The chosen zoom level
     * @return the image
     */
    public BufferedImage renderMap(Area area, int zoom) {

        List<List<Double>> polygon = area.getGeometry().getCoordinates();
        List<List<Double>> mercatorCoordinates = new ArrayList<>();

        //We need to know the bounds so that correct tiles can be used
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

        List<Point> ar = new ArrayList<>();
        for (List<Double> mercatorCoord : mercatorCoordinates) {
            int[] pixelCoord = CoordinateUtils.toPixelCoordinates(mercatorCoord.get(0), mercatorCoord.get(1), zoom);
            int x = pixelCoord[0] - (minX * TILE_SIZE);
            int y = pixelCoord[1] - (minY * TILE_SIZE);
            ar.add(new Point(x,y));
        }
        drawArea(g,ar);

        List<List<Point>> allLines = new ArrayList<>();

        for (Map.Entry<Zone, List<List<Double>>> entry : zoneMercs.entrySet()) {
            List<List<Double>> mc = entry.getValue();
            List<Point> zo = new ArrayList<>();

            for (List<Double> mercatorCoord : mc) {
                int[] pixelCoord = CoordinateUtils.toPixelCoordinates(mercatorCoord.get(0), mercatorCoord.get(1), zoom);
                int x = pixelCoord[0] - (minX * TILE_SIZE);
                int y = pixelCoord[1] - (minY * TILE_SIZE);
                zo.add(new Point(x,y));
            }
            allLines.add(zo);
        }

        g.setColor(new Color(255,0,0,255));
        g.setStroke(new BasicStroke(2));

        for(List<Point> line:concatenateLines(allLines)){
            drawLine(g,line);
        }

        g.dispose();
        return bim;
    }

    /**
     * Concatenate the lines (Zones) associated with the Area. This is needed so we can get a straight line
     * from start to end. Basically we're piecing together curbettes and creating curbs
     * @param lines curbettes
     * @return a concatenated list of connected curbettes
     */
    private List<List<Point>> concatenateLines(List<List<Point>> lines) {
        // Use a map for quick look up of start/end points
        Map<Point, List<Point>> startMap = new HashMap<>();
        Map<Point, List<Point>> endMap = new HashMap<>();
        for (List<Point> line : lines) {
            Point start = line.get(0);
            Point end = line.get(line.size() - 1);
            startMap.put(start, line);
            endMap.put(end, line);
        }

        // Dups
        Set<List<Point>> visited = new HashSet<>();

        List<List<Point>> result = new ArrayList<>();

        for (List<Point> line : lines) {
            if (!visited.contains(line)) {
                visited.add(line);

                List<Point> connectedLine = new ArrayList<>(line);

                // Starters
                Point end = connectedLine.get(connectedLine.size() - 1);
                while (startMap.containsKey(end)) {
                    List<Point> nextLine = startMap.get(end);
                    if (visited.contains(nextLine)){
                        break;
                    }
                    visited.add(nextLine);
                    //Don't need to duplicate
                    connectedLine.addAll(nextLine.subList(1, nextLine.size()));
                    end = connectedLine.get(connectedLine.size() - 1);
                }

                // Trailers
                Point start = connectedLine.get(0);
                while (endMap.containsKey(start)) {
                    List<Point> prevLine = endMap.get(start);
                    if (visited.contains(prevLine)){
                        break;
                    }
                    visited.add(prevLine);
                    // Again, don't need to duplicate the connection point
                    List<Point> toAdd = new ArrayList<>(prevLine.subList(0, prevLine.size() - 1));
                    toAdd.addAll(connectedLine);
                    connectedLine = toAdd;
                    start = connectedLine.get(0);
                }

                result.add(connectedLine);
            }
        }

        return result;
    }


    /**
     * Draws a straight line which is the start point and end point of a list of Points onto the BufferedImage
     * @param g  The graphics object
     * @param points The concatenated curb
     */
    private void drawLine(Graphics2D g, List<Point> points){

        Path2D.Double path = new Path2D.Double();
        Point first  = points.get(0);
        Point last = points.get(points.size()-1);

        path.moveTo(first.getX(), first.getY());
        path.lineTo(last.getX(), last.getY());

        path.closePath();
        g.draw(path);

    }

    /**
     * Draws a shape onto the BufferedImage
     * @param g  The graphics object
     * @param pixels  The converted coordinates
     * @param zoom  The zoom level
     */
    private void drawArea(Graphics2D g, List<Point> points){

        Path2D.Double path = new Path2D.Double();
        boolean firstPoint = true;

        double lastx=0, lasty=0;
        for (Point xy : points) {
            if (firstPoint) {
                path.moveTo(xy.getX(), xy.getY());
                firstPoint = false;
            } else {
                path.lineTo(xy.getX(), xy.getY());
                path.moveTo(xy.getX(), xy.getY());
            }
            lastx = xy.getX();
            lasty = xy.getY();
        }
        path.closePath();
        g.draw(path);

    }

    /**
     * Gets a single tile from cache
     * @param zoom  Zoom level (0 to 20+)
     * @param x  The x coordinate
     * @param y  The y coordinate
     * @return The tile at x,y,x
     */
    private BufferedImage getTile(int zoom, int x, int y) throws Throwable {
        String name = "/"+zoom+"-"+x+"-"+y+".jpeg";
        return ImageIO.read(getClass().getResourceAsStream(name));
    }
}
