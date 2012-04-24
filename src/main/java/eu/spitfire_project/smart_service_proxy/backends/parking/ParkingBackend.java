package eu.spitfire_project.smart_service_proxy.backends.parking;

import java.util.Collection;

import org.apache.commons.lang3.StringEscapeUtils;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

import eu.spitfire_project.smart_service_proxy.backends.parking.ParkingSpace.ParkingLotStatus;
import eu.spitfire_project.smart_service_proxy.core.Backend;

/**
 * Base class for all backends which store and provide information about parking areas and parking spaces.
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

		for (final ParkingArea parkingArea : parkingAreas) {

			String name = StringEscapeUtils.escapeHtml4(parkingArea.getName());

			Resource areaType = "PH".equals(parkingArea.getKind()) ? ParkingVocab.PARKING_INDOOR_AREA : ParkingVocab.PARKING_OUTDOOR_AREA;
			final Resource parkingResource = model.createResource("http://www.smart" +
					"hl.de/parking/" + locationPrefix + "/" + name, areaType);
			for (ParkingSpace parkingLot : parkingArea.getParkingSpaces()) {

				Resource parkingLotRes = "PP".equals(parkingLot.getType()) ? ParkingVocab.PARKING_UNCOVERED_LOT : ParkingVocab.PARKING_COVERED_LOT;
				final Resource parkingLotResource = model.createResource("http://www.smarthl.de/parking/" + locationPrefix + "/" + name + "/"
						+ parkingLot.getId(), parkingLotRes);
				parkingResource.addProperty(DULVocab.HAS_PART, parkingLotResource);
				parkingLotResource.addProperty(ParkingVocab.PARKINGID, parkingLot.getId());
				if (ParkingLotStatus.FREE.equals(parkingLot.getStatus())) {
					parkingLotResource.addProperty(ParkingVocab.PARKINGSTATUS, ParkingVocab.PARKING_AVAILABLE_LOT);
				} else {
					parkingLotResource.addProperty(ParkingVocab.PARKINGSTATUS, ParkingVocab.PARKING_UNAVAILABLE_LOT);
				}
				if (Boolean.TRUE.equals(parkingLot.getHandicapped())) {
					parkingLotResource.addProperty(ParkingVocab.PARKINGSTATUS, ParkingVocab.PARKING_RESERVED_LOT);
				}
				if (null != parkingLot.getLocationCoordinates()) {
					parkingLotResource.addProperty(Wgs84_posVocab.LAT, String.valueOf(parkingLot.getLocationCoordinates().getLat()));
					parkingLotResource.addProperty(Wgs84_posVocab.LONG, String.valueOf(parkingLot.getLocationCoordinates().getLng()));
				}
			}
		}
		return model;
	}

}
