package com.cb;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Deals with parsing areas from the stream by deserializing the json
 */
public class AreaProcessor {
    /**
     * Starts the parse of areas and calls back on the FunctionalInterface
     * @param in the json area data
     * @param zonesMap
     * @param consumer
     * @throws Exception
     */
    public static void processAreas(InputStream in, Map<String, Zone> zonesMap, AreaConsumer consumer) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonFactory factory = objectMapper.getFactory();

        try (JsonParser parser = factory.createParser(in)) {

            while (parser.nextToken() != null) {
                String fieldName = parser.getCurrentName();
                if ("areas".equals(fieldName)) {
                    parser.nextToken();
                    break;
                }
            }

            if (parser.currentToken() == JsonToken.START_ARRAY) {
                while (parser.nextToken() == JsonToken.START_OBJECT) {
                    Area area = objectMapper.readValue(parser, Area.class);

                    List<Zone> associatedZones = new ArrayList<>();
                    for (String curbZoneId : area.getCurbZoneIds()) {
                        if (zonesMap.containsKey(curbZoneId)) {
                            associatedZones.add(zonesMap.get(curbZoneId));
                        }
                    }
                    area.setCurbZones(associatedZones);

                    consumer.accept(area);
                }
            }
        }
    }
}

@FunctionalInterface
interface AreaConsumer {
    void accept(Area area);
}
