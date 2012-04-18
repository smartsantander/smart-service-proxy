/**
 * 
 */
package eu.spitfire_project.smart_service_proxy.backends.parking;

/**
 * @author massel
 *
 */
public class CityInfo {
    private String name;
    private GeoInfo geo;
    @Override
    public String toString() {
        return "CityInfo [name=" + name + ", geo=" + geo + "]";
    }
    public GeoInfo getGeo() {
        return geo;
    }
    public void setGeo(GeoInfo geo) {
        this.geo = geo;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
