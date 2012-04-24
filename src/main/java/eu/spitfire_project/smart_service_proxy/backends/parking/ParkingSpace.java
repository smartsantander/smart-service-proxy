package eu.spitfire_project.smart_service_proxy.backends.parking;

public class ParkingSpace {

	private String type;
	public enum ParkingLotStatus {FREE, OCCUPIED};
	private GeoInfo locationCoordinates;
	private String id;
	private ParkingLotStatus status;

	public ParkingSpace(String id, String type, ParkingLotStatus status, GeoInfo locationCoordinates) {
		super();
		this.id = id;
		this.type = type;
		this.status = status;
		this.locationCoordinates = locationCoordinates;
	}

	
	
	public ParkingSpace() {
		super();
	}



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
	public ParkingLotStatus getStatus() {
		return status;
	}

	// ------------------------------------------------------------------------
	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(final ParkingLotStatus status) {
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



	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}



	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

}
