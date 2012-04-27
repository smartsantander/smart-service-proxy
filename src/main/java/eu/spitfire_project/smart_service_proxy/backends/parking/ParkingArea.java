/**
 * 
 */
package eu.spitfire_project.smart_service_proxy.backends.parking;

import java.util.Collection;

/**
 * @author massel
 * 
 */
public class ParkingArea {
	private String kind;
	private String name;
	private GeoInfo geo;
	private int free;
	private int spaces;
	private String status;
	private String city;

	private Collection<ParkingSpace> parkingSpaces;

	public String getKind() {
		return kind;
	}

	public void setKind(final String kind) {
		this.kind = kind;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public GeoInfo getGeo() {
		return geo;
	}

	public void setGeo(final GeoInfo geo) {
		this.geo = geo;
	}

	public int getFree() {
		return free;
	}

	public void setFree(final int free) {
		this.free = free;
	}

	public int getSpaces() {
		return spaces;
	}

	public void setSpaces(final int spaces) {
		this.spaces = spaces;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(final String status) {
		this.status = status;
	}

	// ------------------------------------------------------------------------
	/**
	 * @return the parkingSpaces
	 */
	public Collection<ParkingSpace> getParkingSpaces() {
		return parkingSpaces;
	}

	// ------------------------------------------------------------------------
	/**
	 * @param parkingSpaces
	 *            the parkingSpaces to set
	 */
	public void setParkingSpaces(final Collection<ParkingSpace> parkingSpaces) {
		this.parkingSpaces = parkingSpaces;
	}

	// ------------------------------------------------------------------------
	/**
	 * @return the city
	 */
	public String getCity() {
		return city;
	}

	// ------------------------------------------------------------------------
	/**
	 * @param city the city to set
	 */
	public void setCity(String city) {
		this.city = city;
	}

	@Override
	public String toString() {
		return "ParkingArea [kind=" + kind + ", name=" + name + ", geo=" + geo + ", free=" + free + ", spaces=" + spaces + ", status=" + status + "]";
	}
}
