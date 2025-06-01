package com.cb;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws Throwable {
        new Main(args);
    }
    public Main(String[] args) throws Throwable {
        if(args==null || args.length==0){
            parse(new FileInputStream("zones.json"),new FileInputStream("areas.json"),true, true);
        }else{
            parse(new FileInputStream(args[0]),new FileInputStream(args[1]),true, true);
        }
    }

    public Main() {
    }

    /**
     * The inputstreams representing the zones and areas json as well as a flag to enable/disable png output
     * @param zones  An inputstream of a GeoJSON file of Zones
     * @param areas  An inputstream of a GeoJSON file of Areas
     * @param createImages  Shall we write images to disk
     * @param straight  Draw zones from start to end rather than point-to-point
     * @throws Throwable
     */
    public void parse(InputStream zones, InputStream areas, boolean createImages, boolean straight) throws Throwable{

        // Map to store zones for quick lookup
        Map<String, Zone> zonesMap = new HashMap<>();

        ZoneProcessor.processZones(zones, zone -> zonesMap.put(zone.getCurbZoneId(), zone));

        int[] N = {0};
        final MapRenderer mr = new MapRenderer();
        // Process areas one-by-one and associate zones
        AreaProcessor.processAreas(areas, zonesMap, area -> {
            try {
                String type = area.getGeometry().getType();
                if (type.equals("Polygon")) {
                    int imageNum = N[0]++;
                    BufferedImage image = mr.renderMap(area, 20, straight);
                    if(createImages){
                        File output = new File("area_and_zones_"+imageNum+".png");
                        ImageIO.write(image, "png", output);
                        System.out.println("#"+imageNum+", Image saved: " + output.getAbsolutePath());
                    }else{
                        System.out.println("#"+imageNum+", Didn't save: " + image.getWidth()+"x"+image.getHeight());
                    }
                }
            }catch(Throwable t){
                t.printStackTrace();
            }
        });

        zones.close();
        areas.close();

    }
}
