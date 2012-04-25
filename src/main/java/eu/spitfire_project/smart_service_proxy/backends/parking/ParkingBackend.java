package eu.spitfire_project.smart_service_proxy.backends.parking;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

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

	/**
	 * Creates jena models for provided parking areas and parking spaces
	 * 
	 * @param locationPrefix
	 *            Indicates the parking areas' location (e.g., a city name)
	 * @param parkingAreas
	 *            A collection of parking areas
	 * @return Jena models for the provided parking areas and parking lots
	 * @throws URISyntaxException
	 */
	protected Collection<Model> createModels(final Collection<ParkingArea> parkingAreas) throws URISyntaxException {
		Collection<Model> models = new LinkedList<Model>();

		try {
			for (final ParkingArea parkingArea : parkingAreas) {
				// create a model for the parking area
				String name;
				Model parkingAreaModel = ModelFactory.createDefaultModel();
				models.add(parkingAreaModel);
				name = URLEncoder.encode(parkingArea.getName(), "utf8");
				Resource areaType = "PH".equals(parkingArea.getKind()) ? ParkingVocab.PARKING_INDOOR_AREA : ParkingVocab.PARKING_OUTDOOR_AREA;
				final Resource parkingResource = parkingAreaModel.createResource(entityManager.getURIBase() + pathPrefix + name, areaType);
				if (null != parkingArea.getGeo()) {
					parkingResource.addProperty(Wgs84_posVocab.LAT, String.valueOf(parkingArea.getGeo().getLat()));
					parkingResource.addProperty(Wgs84_posVocab.LONG, String.valueOf(parkingArea.getGeo().getLng()));
				}

				// create models for the single parking space of the current parking area
				for (ParkingSpace parkingSpace : parkingArea.getParkingSpaces()) {
					Model parkingLotModel = ModelFactory.createDefaultModel();
					models.add(parkingLotModel);
					Resource parkingLotRes = createParkingSpaceResource(parkingLotModel, name, parkingSpace);
					parkingResource.addProperty(DULVocab.HAS_PART, parkingLotRes);
				}

			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		return models;

	}

	private Resource createParkingSpaceResource(final Model model, String name, ParkingSpace parkingSpace) {
		Resource parkingLotType = "PP".equals(parkingSpace.getType()) ? ParkingVocab.PARKING_UNCOVERED_LOT : ParkingVocab.PARKING_COVERED_LOT;
		final Resource psr = model.createResource(entityManager.getURIBase() + pathPrefix + name + "/" + parkingSpace.getId(), parkingLotType);
		psr.addProperty(ParkingVocab.PARKINGID, parkingSpace.getId());
		if (ParkingLotStatus.FREE.equals(parkingSpace.getStatus())) {
			psr.addProperty(ParkingVocab.PARKINGSTATUS, ParkingVocab.PARKING_AVAILABLE_LOT);
		} else {
			psr.addProperty(ParkingVocab.PARKINGSTATUS, ParkingVocab.PARKING_UNAVAILABLE_LOT);
		}
		if (Boolean.TRUE.equals(parkingSpace.getHandicapped())) {
			psr.addProperty(ParkingVocab.PARKINGSTATUS, ParkingVocab.PARKING_RESERVED_LOT);
		}
		if (null != parkingSpace.getLocationCoordinates()) {
			psr.addProperty(Wgs84_posVocab.LAT, String.valueOf(parkingSpace.getLocationCoordinates().getLat()));
			psr.addProperty(Wgs84_posVocab.LONG, String.valueOf(parkingSpace.getLocationCoordinates().getLng()));
		}
		return psr;
	}

	protected abstract Model addToResources(URI uri, Model model);

	protected void registerModels(Collection<Model> models) throws URISyntaxException {
		for (Model model : models) {
			// assumption: each model only contains one resource
			URI uri = new URI(model.listStatements().toList().get(0).getSubject().getURI());
			addToResources(uri, model);
		}
	}
}
