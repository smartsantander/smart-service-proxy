package eu.spitfire_project.smart_service_proxy.backends.parking;

import java.util.Arrays;

/**
 * Simple transport structure used for deserializing information about parking areas and lots with gson.
 * 
 * @author massel
 * 
 */
public class Parkings {
    private CityInfo[] cities;

    @Override
    public String toString() {
        return "Parkings [cities=" + Arrays.toString(cities) + ", parkings=" + Arrays.toString(parkings) + "]";
    }

    public CityInfo[] getCities() {
        return cities;
    }

    public void setCities(CityInfo[] cities) {
        this.cities = cities;
    }

    public ParkingInfo[] getParkings() {
        return parkings;
    }

    public void setParkings(ParkingInfo[] parkings) {
        this.parkings = parkings;
    }

    private ParkingInfo[] parkings;
}
