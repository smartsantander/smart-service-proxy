package eu.spitfire_project.smart_service_proxy.backends.parking;

public class ParkingSpace {

	private String type;
	private String status;
	private GeoInfo locationCoordinates;

	// ------------------------------------------------------------------------
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	// ------------------------------------------------------------------------
	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(final String type) {
		this.type = type;
	}

	// ------------------------------------------------------------------------
	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	// ------------------------------------------------------------------------
	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(final String status) {
		this.status = status;
	}

	// ------------------------------------------------------------------------
	/**
	 * @return the locationCoordinates
	 */
	public GeoInfo getLocationCoordinates() {
		return locationCoordinates;
	}

	// ------------------------------------------------------------------------
	/**
	 * @param locationCoordinates
	 *            the locationCoordinates to set
	 */
	public void setLocationCoordinates(final GeoInfo locationCoordinates) {
		this.locationCoordinates = locationCoordinates;
	}

}
