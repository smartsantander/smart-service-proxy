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
 * Base class for all backends which store and provide information about parking areas and parking spaces.
 * 
 * @author Sebastian Ebers
 * @author Florian Massel
 */
public abstract class ParkingBackend extends Backend {

	private static final Logger log = Logger.getLogger(ParkingBackend.class.getName());
	
	private static Model occupationLevelModel = null;
	
	private static final Map<String,Resource> occupationLevels = new HashMap<String, Resource>();

	/** Indicates the time, new values are considered as valid */
	protected int cachingInterval = 0;

	private final String cityName;
	
	public ParkingBackend(String cityName) {
		super();
		this.cityName = cityName;
	}
	
	
	@Override
	public void bind(EntityManager em) {
		super.bind(em);
		
		if (occupationLevelModel == null){
			occupationLevelModel = ModelFactory.createDefaultModel();
			try {
				addToResources(new URI(entityManager.getURIBase() + pathPrefix + "occupationLevels"), createOccupationLevelModelOverview());
			} catch (UnsupportedEncodingException e) {
				log.error(e,e);
			} catch (URISyntaxException e) {
				log.error(e,e);
			}
		}
	}

	// ------------------------------------------------------------------------
	/**
	 * @return the cityName
	 */
	public String getCityName() {
		return cityName;
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
				long cacheExpiration = new Date().getTime() + cachingInterval;
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
		Collection<Model> models = new LinkedList<Model>();

		try {
			for (final ParkingArea parkingArea : parkingAreas) {
				// create a model for the parking area
				String name;
				Model parkingAreaModel = ModelFactory.createDefaultModel();
				models.add(parkingAreaModel);
				name = URLEncoder.encode(parkingArea.getName(), "utf8");
				Resource areaType = "PH".equals(parkingArea.getKind()) ? ParkingVocab.PARKING_INDOOR_AREA : ParkingVocab.PARKING_OUTDOOR_AREA;
				final Resource parkingAreaResource = parkingAreaModel.createResource(entityManager.getURIBase() + pathPrefix + name, areaType);
				
				fillinParkingAreaResource(parkingArea, parkingAreaResource);
				
				
				// create models for the single parking space of the current parking area
				for (ParkingSpace parkingSpace : parkingArea.getParkingSpaces()) {
					Model parkingLotModel = ModelFactory.createDefaultModel();
					models.add(parkingLotModel);
					Resource parkingLotRes = createParkingSpaceResource(parkingLotModel, name, parkingSpace);
					parkingAreaResource.addProperty(DULVocab.HAS_PART, parkingLotRes);
				}

			}
		} catch (UnsupportedEncodingException e) {
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
		

		parkingAreaResource.addProperty(ParkingVocab.PARKINGID,parkingArea.getName());

		if (parkingArea.getCity() != null){
			parkingAreaResource.addProperty(Wgs84_posVocab.LOCATION, parkingArea.getCity());
		}
		
		
		if (null != parkingArea.getGeo()) {
			parkingAreaResource.addProperty(ParkingVocab.PARKINGID,parkingArea.getName());
			parkingAreaResource.addProperty(Wgs84_posVocab.LAT, String.valueOf(parkingArea.getGeo().getLat()));
			parkingAreaResource.addProperty(Wgs84_posVocab.LONG, String.valueOf(parkingArea.getGeo().getLng()));
		}
		
		
		// if no parking spaces are available, or the submitted state already marks
		// the area as closed, the status property is not set
		if (parkingArea.getSpaces() > 0 && !parkingArea.getStatus().equals("closed")){
			parkingAreaResource.addProperty(ParkingVocab.PARKINGFREE_LOTS,String.valueOf(parkingArea.getFree()));
			parkingAreaResource.addProperty(ParkingVocab.PARKINGSIZE,String.valueOf(parkingArea.getSpaces()));
			parkingAreaResource.addProperty(ParkingVocab.PARKINGAREA_STATUS,"open");
			
		}else {
			parkingAreaResource.addProperty(ParkingVocab.PARKINGAREA_STATUS,"closed");
		}
	}
	
	/**
	 * Just for demo, should be replaced by SPARQL queries
	 * @param parkingAreas 
	 * @param cityName 
	 * @return 
	 * @throws UnsupportedEncodingException 
	 * @deprecated
	 */
	protected Model createCityModel(Collection<ParkingArea> parkingAreas, String cityName) throws UnsupportedEncodingException{
		Model cityModel = ModelFactory.createDefaultModel();
		
		//Resource city = cityModel.createResource(entityManager.getURIBase() + pathPrefix + cityName);
		
		for (ParkingArea parkingArea : parkingAreas) {
			
			String name = URLEncoder.encode(parkingArea.getName(), "utf8");
			Resource areaType = "PH".equals(parkingArea.getKind()) ? ParkingVocab.PARKING_INDOOR_AREA : ParkingVocab.PARKING_OUTDOOR_AREA;
			final Resource parkingAreaResource = cityModel.createResource(entityManager.getURIBase() + pathPrefix + name, areaType);
			
			fillinParkingAreaResource(parkingArea, parkingAreaResource);
			
		}
		
		return cityModel;
	}
	
	protected Model createOccupationLevelModelOverview() throws UnsupportedEncodingException, URISyntaxException{
		
		for (int i = 0; i <=100; i+=25) {
			Resource occupationLevel = occupationLevelModel.createResource(entityManager.getURIBase() + pathPrefix + "level"+i, ParkingVocab.PARKING_OCCUPATION_LEVEL);
			occupationLevel.addProperty(Muo_vocabVocab.MEASURED_IN, Ucum_instancesVocab.PERCENT);
			occupationLevel.addProperty(DULVocab.HAS_DATA_VALUE, String.valueOf(i));
			createandRegisterOccupationLevelModel(i);
		}
		
		return occupationLevelModel;
	}
	
	protected Model createandRegisterOccupationLevelModel(int level) throws UnsupportedEncodingException, URISyntaxException{
		
		Model occupationLevelModel = ModelFactory.createDefaultModel();
		Resource occupationLevel = occupationLevelModel.createResource(entityManager.getURIBase() + pathPrefix + "level"+level, ParkingVocab.PARKING_OCCUPATION_LEVEL);
		occupationLevels.put("level"+level,occupationLevel);
		occupationLevel.addProperty(Muo_vocabVocab.MEASURED_IN, Ucum_instancesVocab.PERCENT);
		occupationLevel.addProperty(DULVocab.HAS_DATA_VALUE, String.valueOf(level));
		addToResources(new URI(occupationLevel.getURI()), occupationLevelModel);
		
		return occupationLevelModel;
			
	}
	
	private Model addToOccupationLevelModel() throws UnsupportedEncodingException{
		
		
		
		return null;
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
	
	protected abstract Map<URI, Model> getResourcesMapping();
	
	@Override
	public Set<URI> getResources() {
		return getResourcesMapping().keySet();
	}
	

	protected void registerModels(Collection<Model> models) throws URISyntaxException {
		for (Model model : models) {
			// assumption: each model only contains one resource
			URI uri = new URI(model.listStatements().toList().get(0).getSubject().getURI());
			addToResources(uri, model);
		}
	}
}
