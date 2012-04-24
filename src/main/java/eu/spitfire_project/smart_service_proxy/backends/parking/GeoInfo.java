package eu.spitfire_project.smart_service_proxy.backends.parking;

public class GeoInfo {
	private double lat;
	private double lng;

	// ------------------------------------------------------------------------
	/**
	 * Constructor
	 */
	public GeoInfo() {
		super();
	}

	// ------------------------------------------------------------------------
	/**
	 * Constructor
	 * 
	 * @param lat
	 * @param lng
	 */
	public GeoInfo(final double lat, final double lng) {
		super();
		this.lat = lat;
		this.lng = lng;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(final double lat) {
		this.lat = lat;
	}

	public double getLng() {
		return lng;
	}

	public void setLng(final double lng) {
		this.lng = lng;
	}

	@Override
	public String toString() {
		return "GeoInfo [lat=" + lat + ", lng=" + lng + "]";
	}
}
