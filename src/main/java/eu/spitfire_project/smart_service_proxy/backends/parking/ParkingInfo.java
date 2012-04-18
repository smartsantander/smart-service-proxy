/**
 * 
 */
package eu.spitfire_project.smart_service_proxy.backends.parking;

/**
 * @author massel
 *
 */
public class ParkingInfo {
    private String kind;
    private String name;
    @Override
    public String toString() {
        return "ParkingInfo [kind=" + kind + ", name=" + name + ", geo=" + geo + ", free=" + free + ", spaces="
                + spaces + ", status=" + status + "]";
    }

    private GeoInfo geo;
    private int free;
    private int spaces;
    private String status;

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GeoInfo getGeo() {
        return geo;
    }

    public void setGeo(GeoInfo geo) {
        this.geo = geo;
    }

    public int getFree() {
        return free;
    }

    public void setFree(int free) {
        this.free = free;
    }

    public int getSpaces() {
        return spaces;
    }

    public void setSpaces(int spaces) {
        this.spaces = spaces;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
