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
     * Sets the tile bounds given the gps coordinates
     * @param shape A list of gps pairs
     * @param b The bounds to set max/min
     * @param zoom  The zoom level
     */
    private void setTileRange(List<List<Double>> shape, Bounds b, int zoom){
        for(List<Double> xy:shape){
            int [] tileXY = CoordinateUtils.toTileNumbers(xy.get(0),xy.get(1),zoom);
            if(tileXY[0]<b.minX) b.minX=tileXY[0];
            else if(tileXY[0]>b.maxX) b.maxX=tileXY[0];
            if(tileXY[1]<b.minY) b.minY=tileXY[1];
            else if(tileXY[1]>b.maxY) b.maxY=tileXY[1];
        }
    }

    /**
     * A simple class that represents a bounding box
     */
    class Bounds{
        int minX=Integer.MAX_VALUE, maxX=Integer.MIN_VALUE, minY=Integer.MAX_VALUE, maxY=Integer.MIN_VALUE;
    }


    /**
     * Creates a BufferedImage of tiles which span the area
     * @param area  The Area object
     * @param zoom  The chosen zoom level
     * @return the image
     */
    public BufferedImage renderMap(Area area, int zoom, boolean straight) {

        //We need to know the bounds so that correct tiles can be used
        Bounds bounds = new Bounds();

        //Find the max XY tile numbers
        List<List<Double>> polygon = area.getGeometry().getCoordinates();
        setTileRange(polygon,bounds,zoom);

        List<Zone> zones = area.getCurbZones();
        for (Zone zone : zones) {
            List<List<Double>> zoneCoordinates = zone.getGeometry().getCoordinates();
            setTileRange(zoneCoordinates,bounds,zoom);
        }

        List<Point> areaPoints = new ArrayList<>();
        //Create pixel points for the area
        for(List<Double> gps:polygon){
            computePoints(gps,areaPoints,bounds,zoom);
        }

        List<List<Point>> zonePoints = new ArrayList<>();
        //Create pixel points for the zones
        for (Zone zone : zones) {
            List<List<Double>> zoneCoordinates = zone.getGeometry().getCoordinates();
            List<Point> zp = new ArrayList<>();
            for(List<Double> gps:zoneCoordinates) {
                computePoints(gps,zp,bounds,zoom);
            }
            zonePoints.add(zp);
        }

        //Create the canvas
        BufferedImage bim =  new BufferedImage(((bounds.maxX-bounds.minX)+1)*TILE_SIZE,
                ((bounds.maxY-bounds.minY)+1)*TILE_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bim.createGraphics();
        g.setPaint ( new Color ( 200, 200, 200) );
        g.fillRect ( 0, 0, bim.getWidth(), bim.getHeight() );
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int X=0, Y;

        for (int x = bounds.minX; x <= bounds.maxX; x++) {
            Y=0;
            for (int y = bounds.minY; y <= bounds.maxY; y++) {
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
        drawArea(g,areaPoints);

        g.setColor(new Color(255,0,0,255));
        g.setStroke(new BasicStroke(2));

        for(List<Point> line:concatenateLines(zonePoints)){
            if(straight) {
                drawStraightLine(g, line);
            }else {
                drawArea(g, line);
            }
        }

        g.dispose();
        return bim;
    }

    /**
     * Converts gps coordinates to tile XY coordinates
     * @param gps  The gps points
     * @param dest  The destination container
     * @param bounds  The bounds
     * @param zoom  The zoom level
     */
    private void computePoints(List<Double> gps, List<Point> dest, Bounds bounds, int zoom){
        int[] pixelCoord = CoordinateUtils.toPixelCoordinates(gps.get(0), gps.get(1), zoom);
        int x = pixelCoord[0] - (bounds.minX * TILE_SIZE);
        int y = pixelCoord[1] - (bounds.minY * TILE_SIZE);
        dest.add(new Point(x,y));
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
    private void drawStraightLine(Graphics2D g, List<Point> points){

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
     * @param points The Area
     */
    private void drawArea(Graphics2D g, List<Point> points){

        Path2D.Double path = new Path2D.Double();
        boolean firstPoint = true;

        for (Point xy : points) {
            if (firstPoint) {
                path.moveTo(xy.getX(), xy.getY());
                firstPoint = false;
            } else {
                path.lineTo(xy.getX(), xy.getY());
                path.moveTo(xy.getX(), xy.getY());
            }
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
