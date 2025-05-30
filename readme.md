<h3>CarriageWay</h3>

This software creates subimage tiles of areas of interest by deserializing GeoJSON files containing areas and 
corresponding zones. It then overlays these polygons onto the bitmap.  

It uses cached bitmap tiles which are for the given areas.json and zones.json files but could be extended by pulling from 
a tile server.

It's been compiled to target Java 17.

To run it in the terminal, run maven package then:
<blockquote>java -cp target/CarriageWay-1.0-SNAPSHOT-jar-with-dependencies.jar com.cb.Main</blockquote>
