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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpRequest;

import com.hp.hpl.jena.rdf.model.Model;

import eu.spitfire_project.smart_service_proxy.backends.parking.ParkingSpace.ParkingLotStatus;
import eu.spitfire_project.smart_service_proxy.backends.parking.generated.ParkingLot;
import eu.spitfire_project.smart_service_proxy.backends.parking.generated.ParkingService;
import eu.spitfire_project.smart_service_proxy.core.httpServer.EntityManager;

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

	private final HashMap<URI, Model> resources = new HashMap<URI, Model>();

	/**
	 * Returns a new backend instance and reads the actual configuration from ssp.properties
	 * 
	 * @param config
	 *            The main configuration file.
	 * @throws org.apache.commons.configuration.ConfigurationException
	 *             if an error occurs while loading ssp.properties
	 */
	public ParkingSantanderBackend(final Configuration config) throws ConfigurationException {
		super("Santander");
		cachingInterval = config.getInt("parkingSantanderCachingMinutes", 5) * 1000 * 60;
	}

	@Override
	public void bind() {
		super.bind();
		registerResources();
	}

	@Override
	public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent me) throws Exception {
		if (!(me.getMessage() instanceof HttpRequest)) {
			ctx.sendUpstream(me);
			return;
		}

		// update all resources since they are outdated due to the cache's configuration
		final Collection<Model> models = resources.values();
		for (final Model model : models) {
			model.close();
		}
		resources.clear();
		registerResources();

		super.messageReceived(ctx, me);
	}

	@Override
	protected Map<URI, Model> getResourcesMapping() {
		return resources;
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

			try {
				// create a Jena model based on the created parking areas
				final Collection<Model> models = createModels(parkingAreas);
				registerModels(models);

				final URI cityUri = new URI(getParkingBaseUriString() + getCityName());
				addToResources(cityUri, createCityModel(parkingAreas, getCityName()));
				ParkingSantanderBackend.log.debug("registered city: " + cityUri);

				final URI parkingSpacesUri = new URI(getParkingBaseUriString() + getCityName() + "ParkingSpaces");
				addToResources(parkingSpacesUri, createCityModelForParkingSpaces(parkingAreas));
				ParkingSantanderBackend.log.debug("registered parking spaces: " + parkingSpacesUri);

			} catch (final URISyntaxException e) {
				ParkingSantanderBackend.log.fatal("This should never happen.", e);
			} catch (final UnsupportedEncodingException e) {
				ParkingSantanderBackend.log.error(e, e);
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
		area.setCity(getCityName());

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

			// get and set the number of available parking lots
			int availableLots = 0;
			for (final ParkingSpace parkingSpace : area.getParkingSpaces()) {
				if (parkingSpace.getStatus().equals(ParkingSpace.ParkingLotStatus.FREE)) {
					++availableLots;
				}
			}
			area.setFree(availableLots);

			area.setStatus("open");
		} else {
			area.setStatus("closed");
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
		for (final eu.spitfire_project.smart_service_proxy.backends.parking.generated.ParkingSpace parkingLotSpace : parkingLotSpaces) {
			final ParkingSpace space = new ParkingSpace();
			parkingSpaces.add(space);

			space.setLocationCoordinates(new GeoInfo(parkingLotSpace.getParkingSpaceCoordinates().getLatitude(), parkingLotSpace
					.getParkingSpaceCoordinates().getLongitude()));
			space.setStatus("FREE".equals(parkingLotSpace.getCurrentStatus().getSpace()) ? ParkingLotStatus.FREE : ParkingLotStatus.OCCUPIED);
			// only uncovered parking spots
			space.setType("PP");
			// web service returns either STANDARD or PEOPLE WITH DISABILITIES
			if (!"STANDARD".equals(parkingLotSpace.getParkingSpaceType().getParkingSpaceType())) {
				space.setHandicapped(Boolean.TRUE);
			}
			space.setId(String.valueOf(id++));

		}
		return parkingSpaces;
	}

	@Override
	protected Model addToResources(final URI uri, final Model model) {
		EntityManager.getInstance().entityCreated(uri, this);
		return resources.put(uri, model);
	}
}
