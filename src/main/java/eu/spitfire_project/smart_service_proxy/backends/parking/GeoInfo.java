package eu.spitfire_project.smart_service_proxy.backends.parking;

public class GeoInfo {
    private double lat;
    private double lng;

    @Override
    public String toString() {
        return "GeoInfo [lat=" + lat + ", lng=" + lng + "]";
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}
