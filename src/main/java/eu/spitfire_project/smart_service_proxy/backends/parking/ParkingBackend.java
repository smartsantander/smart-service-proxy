package eu.spitfire_project.smart_service_proxy.backends.parking;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;

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

	private static Logger log = Logger.getLogger(ParkingHLBackend.class.getName());
	protected final HashMap<URI, Model> resources = new HashMap<URI, Model>();

	/**
	 * Creates  jena models for provided parking areas and parking lots, registers them as resources
	 * 
	 * @param locationPrefix
	 *            Indicates the parking areas' location (e.g., a city name)
	 * @param parkingAreas
	 *            A collection of parking areas
	 * @return a Jena model for the provided parking areas
	 * @throws URISyntaxException 
	 */
	protected Model createModel(final String locationPrefix, final Collection<ParkingArea> parkingAreas) throws URISyntaxException {
		//TODO FMA: rename to createModelsAndRegisterResources
		final Model model = ModelFactory.createDefaultModel();

		for (final ParkingArea parkingArea : parkingAreas) {
			//create a model for the parking area and register it as resource
			String name = StringEscapeUtils.escapeHtml4(parkingArea.getName());
			Resource areaType = "PH".equals(parkingArea.getKind()) ? ParkingVocab.PARKING_INDOOR_AREA : ParkingVocab.PARKING_OUTDOOR_AREA;
			//TODO FMA: extract base uri as parameter
			final Resource parkingResource = model.createResource("http://www.smarthl.de/parking/" + locationPrefix + "/" + name, areaType);
			//TODO Sebastian: pull up in calling method
			URI resourceURI = new URI(entityManager.getURIBase() + pathPrefix + name);
			registerResource(resourceURI, model);
			
			//create models for the single parking lots and register them as resources
			for (ParkingSpace parkingLot : parkingArea.getParkingSpaces()) {
				Model parkingLotModel = ModelFactory.createDefaultModel();
				// TODO FMA: extract base uri as parameter
				Resource parkingLotRes = createParkingLotResource(locationPrefix, parkingLotModel, name, parkingLot, "http://www.smarthl.de/parking/");
				parkingResource.addProperty(DULVocab.HAS_PART, parkingLotRes);
				//TODO Sebastian: pull up in calling method
				resourceURI = new URI(entityManager.getURIBase() + pathPrefix + name);
				registerResource(resourceURI, parkingLotModel);
			}
		}
		return model;
	}

	//TODO Sebastian: remove
	private void registerResource(URI resourceURI, Model model) {
			resources.put(resourceURI, model);
			if (log.isDebugEnabled()) {
				log.debug("Successfully added new resource at " + resourceURI);
			}
		

	}

	private Resource createParkingLotResource(final String locationPrefix, final Model model, String name, ParkingSpace parkingLot, String baseURI) {
		Resource parkingLotType = "PP".equals(parkingLot.getType()) ? ParkingVocab.PARKING_UNCOVERED_LOT : ParkingVocab.PARKING_COVERED_LOT;
		final Resource plr = model.createResource(baseURI + locationPrefix + "/" + name + "/" + parkingLot.getId(), parkingLotType);
		plr.addProperty(ParkingVocab.PARKINGID, parkingLot.getId());
		if (ParkingLotStatus.FREE.equals(parkingLot.getStatus())) {
			plr.addProperty(ParkingVocab.PARKINGSTATUS, ParkingVocab.PARKING_AVAILABLE_LOT);
		} else {
			plr.addProperty(ParkingVocab.PARKINGSTATUS, ParkingVocab.PARKING_UNAVAILABLE_LOT);
		}
		if (Boolean.TRUE.equals(parkingLot.getHandicapped())) {
			plr.addProperty(ParkingVocab.PARKINGSTATUS, ParkingVocab.PARKING_RESERVED_LOT);
		}
		if (null != parkingLot.getLocationCoordinates()) {
			plr.addProperty(Wgs84_posVocab.LAT, String.valueOf(parkingLot.getLocationCoordinates().getLat()));
			plr.addProperty(Wgs84_posVocab.LONG, String.valueOf(parkingLot.getLocationCoordinates().getLng()));
		}
		return plr;
	}
}
