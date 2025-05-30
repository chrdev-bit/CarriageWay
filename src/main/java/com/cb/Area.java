package com.cb;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * This class represents an Object version of a json area
 */
public class Area {
    @JsonProperty("curb_area_id")
    private String curbAreaId;

    @JsonProperty("geometry")
    private Geometry geometry;

    @JsonProperty("published_date")
    private long publishedDate;

    @JsonProperty("last_updated_date")
    private long lastUpdatedDate;

    @JsonProperty("curb_zone_ids")
    private List<String> curbZoneIds;

    private List<Zone> curbZones;

    public void setCurbZones(List<Zone> curbZones) {
        this.curbZones = curbZones;
    }

    public List<Zone> getCurbZones() {
        return curbZones;
    }

    public List<String> getCurbZoneIds() {
        return curbZoneIds;
    }

    public String getCurbAreaId() {
        return curbAreaId;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public static class Geometry {
        private String type;
        //nested?
        private List<List<List<Double>>> coordinates;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<List<Double>> getCoordinates() {
            return coordinates.get(0);
        }

        public void setCoordinates(List<List<List<Double>>> coordinates) {
            this.coordinates = coordinates;
        }
    }


}

