<h3>CarriageWay</h3>

This software creates tile composite bitmaps of areas of interest by deserializing GeoJSON file containing areas and 
corresponding zones. It then overlays these polygons onto the bitmap.  

It uses cached bitmap tiles associated with the given areas.json and zones.json only, but this could be extended by pulling from 
a tile server instead.

It's been compiled to target Java 17.

To run it in the terminal, run maven package to create the JAR then:
<blockquote>java -cp target/CarriageWay-1.0-SNAPSHOT-jar-with-dependencies.jar com.cb.Main</blockquote>
