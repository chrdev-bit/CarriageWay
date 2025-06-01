package com.cb;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;

/**
 * Deals with parsing zones from the stream by deserializing the json
 */
public class ZoneProcessor {
    /**
     * Parse the zones and consume into a Map via the FunctionalInterface
     * @param in  GeoJSON of zones
     * @param consumer  Consumer callback
     * @throws Exception
     */
    public static void processZones(InputStream in, ZoneConsumer consumer) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonFactory factory = objectMapper.getFactory();

        try (JsonParser parser = factory.createParser(in)) {

            while (parser.nextToken() != null) {
                String fieldName = parser.getCurrentName();
                if ("zones".equals(fieldName)) {
                    parser.nextToken();
                    break;
                }
            }

            if (parser.currentToken() == JsonToken.START_ARRAY) {
                while (parser.nextToken() == JsonToken.START_OBJECT) {
                    Zone zone = objectMapper.readValue(parser, Zone.class);
                    consumer.accept(zone);
                }
            }
        }
    }
}

@FunctionalInterface
interface ZoneConsumer {
    void accept(Zone zone);
}
