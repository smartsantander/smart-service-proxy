/**
 * Copyright (c) 2012, all partners of project SPITFIRE (http://www.spitfire-project.eu)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *    disclaimer.
 *
 *  - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *  - Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote
 *    products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package eu.spitfire_project.smart_service_proxy.backends.parking;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
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

import eu.spitfire_project.smart_service_proxy.backends.parking.ParkingSpace.ParkingLotStatus;
import eu.spitfire_project.smart_service_proxy.backends.parking.generated.ParkingLot;
import eu.spitfire_project.smart_service_proxy.backends.parking.generated.ParkingService;
import eu.spitfire_project.smart_service_proxy.core.EntityManager;
import eu.spitfire_project.smart_service_proxy.core.SelfDescription;
import eu.spitfire_project.smart_service_proxy.utils.HttpResponseFactory;

/**
 * A {@link ParkingSantanderBackend} instance hosts models for parking areas located in the city of
 * Santander, Spain.
 * 
 * @author Oliver Kleine, Florian Massel, Sebastian Ebers
 * 
 */

public class ParkingSantanderBackend extends ParkingBackend {

	/** Instance to log messages */
	private static Logger log = Logger.getLogger(ParkingSantanderBackend.class.getName());

	protected final HashMap<URI, Model> resources = new HashMap<URI, Model>();


	/** Inicates the time, new values are considered as valid */
	private int cachingInterval = 0;

	/** Indicates the currently cached values's expiration date */
	private long cacheExpiration;

	/**
	 * Returns a new backend instance and reads the actual configuration from ssp.properties
	 * 
	 * @param config
	 *            The main configuration file.
	 * @throws org.apache.commons.configuration.ConfigurationException
	 *             if an error occurs while loading ssp.properties
	 */
	public ParkingSantanderBackend(final Configuration config) throws ConfigurationException {
		super();
		//TODO FMA: pull up in super class (messageReceived then should be pulled up too)
		cachingInterval = config.containsKey("parkingSantanderCachingMinutes") ?  config.getInt("parkingSantanderCachingMinutes") * 1000 * 60 : 5 * 60 * 1000;
	}

	@Override
	public void bind(final EntityManager em) {
		super.bind(em);
		registerResources();
	}

	@Override
	public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent me) throws Exception {
		if (!(me.getMessage() instanceof HttpRequest)) {
			ctx.sendUpstream(me);
			return;
		}

		final HttpRequest request = (HttpRequest) me.getMessage();
		Object response;

		// Look up resource
		final URI resourceURI = entityManager.normalizeURI(new URI(request.getUri()));
		final Model model = resources.get(resourceURI);

		if (model != null) {
			if (request.getMethod() == HttpMethod.GET) {
				response = new SelfDescription(model, new URI(request.getUri()), new Date(cacheExpiration));

				if (ParkingSantanderBackend.log.isDebugEnabled()) {
					ParkingSantanderBackend.log.debug("[ParkingBackend] Resource found: " + resourceURI);
				}
			} else {
				response = new DefaultHttpResponse(request.getProtocolVersion(), HttpResponseStatus.METHOD_NOT_ALLOWED);

				if (ParkingSantanderBackend.log.isDebugEnabled()) {
					ParkingSantanderBackend.log.debug("[ParkingBackend] Method not allowed: " + request.getMethod());
				}
			}
		} else {
			response = HttpResponseFactory.createHttpResponse(request.getProtocolVersion(), HttpResponseStatus.NOT_FOUND);

			if (ParkingSantanderBackend.log.isDebugEnabled()) {
				ParkingSantanderBackend.log.debug("[ParkingBackend] Resource not found: " + resourceURI);
			}
		}

		// Send response
		final ChannelFuture future = Channels.write(ctx.getChannel(), response);
		future.addListener(ChannelFutureListener.CLOSE);
	}

	@Override
	public Set<URI> getResources() {
		return resources.keySet();
	}

	// ------------------------------------------------------------------------
	/**
	 * Fetches all parking areas located in the city of Santander which are provided via the
	 * "ParkingService" web service, creates a Jena model for them, and publishes them on a web
	 * site.
	 */
	private void registerResources() {

		// get all available parking areas
		final Collection<ParkingArea> parkingAreas = getParkingAreas();

		if (parkingAreas != null) {

			cacheExpiration = new Date().getTime() + cachingInterval;

			try {
				// create a Jena model based on the created parking areas
				final Collection<Model> models = createModels(parkingAreas);
				registerModels(models);
			} catch (final URISyntaxException e) {
				ParkingSantanderBackend.log.fatal("This should never happen.", e);
			}
		}

	}

	// ------------------------------------------------------------------------
	/**
	 * Fetches parking areas by using a web service and returns the result.
	 * 
	 * @return All parking areas provided by a web service.
	 */
	private Collection<ParkingArea> getParkingAreas() {
		// container class to store all parking areas which are accessible via the web service
		// used below.
		final Collection<ParkingArea> parkingAreas = new LinkedList<ParkingArea>();

		// get all parking lots located in the city of Santander which are accessible via
		// the "ParkingService" web service.
		List<ParkingLot> parkingLot = null;
		try {
			parkingLot = new ParkingService().getParkingService().getParkingLots().getParkingLot();
		} catch (final Exception e) {
			ParkingSantanderBackend.log.error(e, e);
			return null;
		}

		// iterate over all parking lots
		for (final ParkingLot pl : parkingLot) {
			parkingAreas.add(toParkingArea(pl));
		}

		return parkingAreas;
	}

	// ------------------------------------------------------------------------
	/**
	 * Converts a parking lot which was fetched via web service to an internal representation and
	 * returns the result
	 * 
	 * @param pl
	 *            A parking lot provided via web service
	 * @return An internal representation of the provided parking lot
	 */
	private ParkingArea toParkingArea(final ParkingLot pl) {

		// create a new parking area object for this parking lot and put it into the container
		final ParkingArea area = new ParkingArea();

		area.setName(pl.getParkingLotAddress());
		area.setKind("PP");

		// ... get a list of all single parking spaces
		final List<eu.spitfire_project.smart_service_proxy.backends.parking.generated.ParkingSpace> parkingLotSpaces = pl.getParkingSpaces();

		// if there are parking spaces accessible ...
		if ((parkingLotSpaces != null) && (parkingLotSpaces.size() > 0)) {

			// ... use the first parking space to set the geo location of the whole
			// parking space
			final eu.spitfire_project.smart_service_proxy.backends.parking.generated.ParkingSpace firstSpace = parkingLotSpaces.get(0);
			area.setGeo(new GeoInfo(firstSpace.getParkingSpaceCoordinates().getLatitude(), firstSpace.getParkingSpaceCoordinates().getLongitude()));

			// ... and set the number of single spaces as the parking area's size
			area.setSpaces(parkingLotSpaces.size());

			// create a new collection of parking spaces and add them to the recently created
			// parking area
			area.setParkingSpaces(toParkingSpaces(parkingLotSpaces));
		}

		return area;
	}

	// ------------------------------------------------------------------------
	/**
	 * Converts a collection of parking spaces which was fetched via web service to an internal
	 * representation and returns the result
	 * 
	 * @param parkingLotSpaces
	 *            A collection of parking spaces fetched via web service
	 * @return An internal representation of the fetched list
	 */
	private Collection<ParkingSpace> toParkingSpaces(
			final Collection<eu.spitfire_project.smart_service_proxy.backends.parking.generated.ParkingSpace> parkingLotSpaces) {
		final Collection<ParkingSpace> parkingSpaces = new LinkedList<ParkingSpace>();

		// add all parking spaces fetched via web service to the recently created parking
		// area
		int id = 0;
		for (final eu.spitfire_project.smart_service_proxy.backends.parking.generated.ParkingSpace parkingSpace : parkingLotSpaces) {
			final ParkingSpace space = new ParkingSpace();
			parkingSpaces.add(space);

			space.setLocationCoordinates(new GeoInfo(parkingSpace.getParkingSpaceCoordinates().getLatitude(), parkingSpace
					.getParkingSpaceCoordinates().getLongitude()));
			space.setStatus("FREE".equals(parkingSpace.getCurrentStatus()) ? ParkingLotStatus.FREE : ParkingLotStatus.OCCUPIED);
			// only uncovered parking spots
			space.setType("PP");
			// web service returns either STANDARD or PEOPLE WITH DISABILITIES
			if (!"STANDARD".equals(parkingSpace.getParkingSpaceType().getParkingSpaceType())) {
				space.setHandicapped(Boolean.TRUE);
			}
			space.setId(String.valueOf(id++));

		}
		return parkingSpaces;
	}

	@Override
	protected Model addToResources(URI uri, Model model) {
		return resources.put(uri, model);
	}
}
