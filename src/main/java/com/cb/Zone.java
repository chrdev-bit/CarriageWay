package com.cb;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * This class represents an Object version of a json zone
 */
public class Zone {
    @JsonProperty("curb_zone_id")
    private String curbZoneId;

    @JsonProperty("geometry")
    private Geometry geometry;

    @JsonProperty("published_date")
    private long publishedDate;

    @JsonProperty("last_updated_date")
    private long lastUpdatedDate;

    @JsonProperty("start_date")
    private long startDate;

    @JsonProperty("location_references")
    private List<Map<String, Object>> locationReferences;

    @JsonProperty("street_name")
    private String streetName;

    @JsonProperty("cross_street_start_name")
    private String crossStreetStartName;

    @JsonProperty("cross_street_end_name")
    private String crossStreetEndName;

    @JsonProperty("curb_policy_ids")
    private List<String> curbPolicyIds;

    @JsonProperty("parking_angle")
    private String parkingAngle;

    @JsonProperty("num_spaces")
    private int numSpaces;

    public String getCurbZoneId() {
        return curbZoneId;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public static class Geometry {
        private String type;
        private List<List<Double>> coordinates;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<List<Double>> getCoordinates() {
            return coordinates;
        }

        public void setCoordinates(List<List<Double>> coordinates) {
            this.coordinates = coordinates;
        }
    }

}
