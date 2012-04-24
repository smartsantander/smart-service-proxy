package eu.spitfire_project.smart_service_proxy.backends.parking;

import java.util.Arrays;
import java.util.List;

/**
 * Simple transport structure used for deserializing information about parking areas and lots with
 * gson.
 * 
 * @author massel
 * 
 */
public class Parkings {
	private CityInfo[] cities;

	private List<ParkingArea> parkings;

	@Override
	public String toString() {
		return "Parkings [cities=" + Arrays.toString(cities) + ", parkings=" + (parkings).toString() + "]";
	}

	public CityInfo[] getCities() {
		return cities;
	}

	public void setCities(final CityInfo[] cities) {
		this.cities = cities;
	}

	public List<ParkingArea> getParkings() {
		return parkings;
	}

	public void setParkings(final List<ParkingArea> parkings) {
		this.parkings = parkings;
	}
}
