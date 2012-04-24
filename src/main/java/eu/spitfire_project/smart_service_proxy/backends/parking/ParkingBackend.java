package eu.spitfire_project.smart_service_proxy.backends.parking;

import java.util.Collection;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

import eu.spitfire_project.smart_service_proxy.core.Backend;

/**
 * Base class for all backends which store and provide information about parking areas and parking
 * spaces.
 * 
 * @author Sebastian Ebers
 * @author Florian Massel
 */
public abstract class ParkingBackend extends Backend {

	/**
	 * Creates a Jena model for provided parking areas
	 * 
	 * @param locationPrefix
	 *            Indicates the parking areas' location (e.g., a city name)
	 * @param parkingAreas
	 *            A collection of parking areas
	 * @return a Jena model for the provided parking areas
	 */
	protected Model createModel(final String locationPrefix, final Collection<ParkingArea> parkingAreas) {
		final Model model = ModelFactory.createDefaultModel();

		for (final ParkingArea parking : parkingAreas) {
			final Resource currentParking = model.createResource("http://www.smarthl.de/parking/" + locationPrefix + "/" + parking.getName(),
					ParkingVocab.PARKING_OUTDOOR_AREA);
			for (int i = 0; i < parking.getSpaces(); i++) {
				final Resource currentParkingLot = model.createResource("http://www.smarthl.de/parking/" + locationPrefix + "/" + parking.getName()
						+ "/" + i, ParkingVocab.PARKING_PARKING_LOT);
				currentParking.addProperty(DULVocab.HAS_PART, currentParkingLot);
				currentParkingLot.addProperty(ParkingVocab.PARKINGID, String.valueOf(i));
				if (i < parking.getFree()) {
					currentParkingLot.addProperty(ParkingVocab.PARKINGSTATUS, ParkingVocab.PARKING_AVAILABLE_LOT);
				} else {
					currentParkingLot.addProperty(ParkingVocab.PARKINGSTATUS, ParkingVocab.PARKING_BOOKED_LOT);
				}
			}
		}
		return model;
	}

}
