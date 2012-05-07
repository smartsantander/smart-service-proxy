package eu.spitfire_project.smart_service_proxy.backends.parking;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

import eu.spitfire_project.smart_service_proxy.backends.parking.ParkingSpace.ParkingLotStatus;
import eu.spitfire_project.smart_service_proxy.core.Backend;
import eu.spitfire_project.smart_service_proxy.core.EntityManager;
import eu.spitfire_project.smart_service_proxy.core.SelfDescription;
import eu.spitfire_project.smart_service_proxy.utils.HttpResponseFactory;

/**
 * Base class for all backends which store and provide information about parking areas and parking
 * spaces.
 * 
 * @author Sebastian Ebers
 * @author Florian Massel
 */
public abstract class ParkingBackend extends Backend {

	private static final Logger log = Logger.getLogger(ParkingBackend.class.getName());

	private static Model occupationLevelModel = null;

	private static final Map<String, Resource> occupationLevels = new HashMap<String, Resource>();

	/** Indicates the time, new values are considered as valid */
	protected int cachingInterval = 0;

	private final String cityName;

	public ParkingBackend(final String cityName) {
		super();
		this.cityName = cityName;
	}

	// ------------------------------------------------------------------------
	/**
	 * @return the cityName
	 */
	public String getCityName() {
		return cityName;
	}

	@Override
	public void bind(final EntityManager em) {
		super.bind(em);

		if (ParkingBackend.occupationLevelModel == null) {
			ParkingBackend.occupationLevelModel = ModelFactory.createDefaultModel();
			try {
				addToResources(new URI(entityManager.getURIBase() + pathPrefix + "occupationLevels"), createOccupationLevelModelOverview());
			} catch (final UnsupportedEncodingException e) {
				ParkingBackend.log.error(e, e);
			} catch (final URISyntaxException e) {
				ParkingBackend.log.error(e, e);
			}
		}
	}

	protected abstract Model addToResources(URI uri, Model model);

	protected abstract Map<URI, Model> getResourcesMapping();

	@Override
	public Set<URI> getResources() {
		return getResourcesMapping().keySet();
	}

	protected void registerModels(final Collection<Model> models) throws URISyntaxException {
		for (final Model model : models) {
			// assumption: each model only contains one resource
			final URI uri = new URI(model.listStatements().toList().get(0).getSubject().getURI());
			addToResources(uri, model);
		}
	}

	@Override
	public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent me) throws Exception {

		final HttpRequest request = (HttpRequest) me.getMessage();
		Object response;

		// Look up resource
		final URI resourceURI = entityManager.normalizeURI(new URI(request.getUri()));
		final Model model = getResourcesMapping().get(resourceURI);

		if (model != null) {

			if (request.getMethod() == HttpMethod.GET) {

				// configure the time the returned data is held in chache
				final long cacheExpiration = new Date().getTime() + cachingInterval;
				response = new SelfDescription(model, new URI(request.getUri()), new Date(cacheExpiration));

				if (ParkingBackend.log.isDebugEnabled()) {
					ParkingBackend.log.debug("[ParkingBackend] Resource found: " + resourceURI);
				}
			} else {
				response = new DefaultHttpResponse(request.getProtocolVersion(), HttpResponseStatus.METHOD_NOT_ALLOWED);

				if (ParkingBackend.log.isDebugEnabled()) {
					ParkingBackend.log.debug("[ParkingBackend] Method not allowed: " + request.getMethod());
				}
			}
		} else {
			response = HttpResponseFactory.createHttpResponse(request.getProtocolVersion(), HttpResponseStatus.NOT_FOUND);

			if (ParkingBackend.log.isDebugEnabled()) {
				ParkingBackend.log.debug("[ParkingBackend] Resource not found: " + resourceURI);
			}
		}

		// Send response
		final ChannelFuture future = Channels.write(ctx.getChannel(), response);
		future.addListener(ChannelFutureListener.CLOSE);
	}

	/**
	 * Creates jena models for provided parking areas and parking spaces
	 * 
	 * @param parkingAreas
	 *            A collection of parking areas
	 * @return Jena models for the provided parking areas and parking lots
	 */
	protected Collection<Model> createModels(final Collection<ParkingArea> parkingAreas) {
		final Collection<Model> models = new LinkedList<Model>();

		try {
			for (final ParkingArea parkingArea : parkingAreas) {
				// create a model for the parking area
				final Model parkingAreaModel = ModelFactory.createDefaultModel();
				models.add(parkingAreaModel);
				final String urlEncodedName = URLEncoder.encode(parkingArea.getName(), "utf8");
				final Resource areaType = "PH".equals(parkingArea.getKind()) ? ParkingVocab.PARKING_INDOOR_AREA : ParkingVocab.PARKING_OUTDOOR_AREA;
				final Resource parkingAreaResource = parkingAreaModel.createResource(entityManager.getURIBase() + pathPrefix + urlEncodedName,
						areaType);

				fillinParkingAreaResource(parkingArea, parkingAreaResource);

				// create models for the single parking space of the current parking area
				for (final ParkingSpace parkingSpace : parkingArea.getParkingSpaces()) {
					final Model parkingLotModel = ModelFactory.createDefaultModel();
					models.add(parkingLotModel);
					final Resource parkingLotRes = createParkingSpaceResource(parkingArea, parkingLotModel, parkingSpace);
					parkingAreaResource.addProperty(DULVocab.HAS_PART, parkingLotRes);
				}

			}
		} catch (final UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		return models;

	}

	// ------------------------------------------------------------------------
	/**
	 * @param parkingArea
	 * @param parkingAreaResource
	 */
	private void fillinParkingAreaResource(final ParkingArea parkingArea, final Resource parkingAreaResource) {
		if (null != parkingArea.getGeo()) {
			parkingAreaResource.addProperty(Wgs84_posVocab.LAT, String.valueOf(parkingArea.getGeo().getLat()));
			parkingAreaResource.addProperty(Wgs84_posVocab.LONG, String.valueOf(parkingArea.getGeo().getLng()));
		}

		parkingAreaResource.addProperty(ParkingVocab.PARKINGID, parkingArea.getName());

		if (parkingArea.getCity() != null) {
			parkingAreaResource.addProperty(Wgs84_posVocab.LOCATION, parkingArea.getCity());
		}

		if (null != parkingArea.getGeo()) {
			parkingAreaResource.addProperty(ParkingVocab.PARKINGID, parkingArea.getName());
			parkingAreaResource.addProperty(Wgs84_posVocab.LAT, String.valueOf(parkingArea.getGeo().getLat()));
			parkingAreaResource.addProperty(Wgs84_posVocab.LONG, String.valueOf(parkingArea.getGeo().getLng()));
		}

		// if no parking spaces are available, or the submitted state already marks
		// the area as closed, the status property is not set
		if ((parkingArea.getSpaces() > 0) && !parkingArea.getStatus().equals("closed")) {
			parkingAreaResource.addProperty(ParkingVocab.PARKINGFREE_LOTS, String.valueOf(parkingArea.getFree()));
			parkingAreaResource.addProperty(ParkingVocab.PARKINGSIZE, String.valueOf(parkingArea.getSpaces()));
			parkingAreaResource.addProperty(ParkingVocab.PARKINGAREA_STATUS, "open");

		} else {
			parkingAreaResource.addProperty(ParkingVocab.PARKINGAREA_STATUS, "closed");
		}
	}

	private Resource createParkingSpaceResource(final ParkingArea parkingArea, final Model model, final ParkingSpace parkingSpace)
			throws UnsupportedEncodingException {
		final String urlEncodedName = URLEncoder.encode(parkingArea.getName(), "utf8");
		final Resource parkingLotType = "PP".equals(parkingSpace.getType()) ? ParkingVocab.PARKING_UNCOVERED_LOT : ParkingVocab.PARKING_COVERED_LOT;
		final Resource psr = model.createResource(entityManager.getURIBase() + pathPrefix + urlEncodedName + "/" + parkingSpace.getId(),
				parkingLotType);
		fillinParkingSpaceResource(parkingArea.getName(), parkingSpace, psr);
		return psr;
	}

	// ------------------------------------------------------------------------
	/**
	 * @param parkingAreaName
	 * @param parkingSpace
	 * @param psr
	 */
	private void fillinParkingSpaceResource(final String parkingAreaName, final ParkingSpace parkingSpace, final Resource psr) {
		psr.addProperty(ParkingVocab.PARKINGID, parkingAreaName + " #" + parkingSpace.getId());
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
	}

	protected Model createOccupationLevelModelOverview() throws UnsupportedEncodingException, URISyntaxException {

		for (int i = 0; i <= 100; i += 25) {
			final Resource occupationLevel = ParkingBackend.occupationLevelModel.createResource(
					entityManager.getURIBase() + pathPrefix + "level" + i, ParkingVocab.PARKING_OCCUPATION_LEVEL);
			occupationLevel.addProperty(Muo_vocabVocab.MEASURED_IN, Ucum_instancesVocab.PERCENT);
			occupationLevel.addProperty(DULVocab.HAS_DATA_VALUE, String.valueOf(i));
			createandRegisterOccupationLevelModel(i);
		}

		return ParkingBackend.occupationLevelModel;
	}

	protected Model createandRegisterOccupationLevelModel(final int level) throws UnsupportedEncodingException, URISyntaxException {

		final Model occupationLevelModel = ModelFactory.createDefaultModel();
		final Resource occupationLevel = occupationLevelModel.createResource(entityManager.getURIBase() + pathPrefix + "level" + level,
				ParkingVocab.PARKING_OCCUPATION_LEVEL);
		ParkingBackend.occupationLevels.put("level" + level, occupationLevel);
		occupationLevel.addProperty(Muo_vocabVocab.MEASURED_IN, Ucum_instancesVocab.PERCENT);
		occupationLevel.addProperty(DULVocab.HAS_DATA_VALUE, String.valueOf(level));
		addToResources(new URI(occupationLevel.getURI()), occupationLevelModel);

		return occupationLevelModel;

	}

	/**
	 * Just for demo, should be replaced by SPARQL queries
	 * 
	 * @param parkingAreas
	 * @param cityName
	 * @return
	 * @throws UnsupportedEncodingException
	 * @deprecated
	 */
	@Deprecated
	protected Model createCityModel(final Collection<ParkingArea> parkingAreas, final String cityName) throws UnsupportedEncodingException {
		final Model cityModel = ModelFactory.createDefaultModel();

		for (final ParkingArea parkingArea : parkingAreas) {

			final String name = URLEncoder.encode(parkingArea.getName(), "utf8");
			final Resource areaType = "PH".equals(parkingArea.getKind()) ? ParkingVocab.PARKING_INDOOR_AREA : ParkingVocab.PARKING_OUTDOOR_AREA;
			final Resource parkingAreaResource = cityModel.createResource(entityManager.getURIBase() + pathPrefix + name, areaType);

			fillinParkingAreaResource(parkingArea, parkingAreaResource);

		}

		return cityModel;
	}

	/**
	 * Just for demo, should be replaced by SPARQL queries
	 * 
	 * @param parkingAreas
	 * @return
	 * @throws UnsupportedEncodingException
	 * @deprecated
	 */
	@Deprecated
	protected Model createCityModelForParkingSpaces(final Collection<ParkingArea> parkingAreas) throws UnsupportedEncodingException {
		final Model cityModel = ModelFactory.createDefaultModel();

		for (final ParkingArea parkingArea : parkingAreas) {

			final Collection<ParkingSpace> parkingSpaces = parkingArea.getParkingSpaces();
			for (final ParkingSpace parkingSpace : parkingSpaces) {
				final String name = URLEncoder.encode(parkingArea.getName(), "utf8");
				final Resource parkingLotType = "PP".equals(parkingSpace.getType()) ? ParkingVocab.PARKING_UNCOVERED_LOT
						: ParkingVocab.PARKING_COVERED_LOT;
				final Resource psr = cityModel.createResource(entityManager.getURIBase() + pathPrefix + name + "/" + parkingSpace.getId(),
						parkingLotType);
				fillinParkingSpaceResource(parkingArea.getName(), parkingSpace, psr);
			}

		}

		return cityModel;
	}
}
