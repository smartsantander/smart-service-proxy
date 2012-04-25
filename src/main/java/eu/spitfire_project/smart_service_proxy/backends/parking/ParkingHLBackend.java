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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;

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

import com.google.gson.Gson;
import com.hp.hpl.jena.rdf.model.Model;

import eu.spitfire_project.smart_service_proxy.backends.parking.ParkingSpace.ParkingLotStatus;
import eu.spitfire_project.smart_service_proxy.core.EntityManager;
import eu.spitfire_project.smart_service_proxy.core.SelfDescription;
import eu.spitfire_project.smart_service_proxy.utils.HttpResponseFactory;

/**
 * A {@link ParkingHLBackend} instance hosts models for parking areas.
 * 
 * @author Oliver Kleine, Florian Massel
 * 
 */

public class ParkingHLBackend extends ParkingBackend {

	private static Logger log = Logger.getLogger(ParkingHLBackend.class.getName());

	protected final HashMap<URI, Model> resources = new HashMap<URI, Model>();

	
	private final String paringkURL;

	/**
	 * Returns a new Backend instance and reads the actual configuration from ssp.properties
	 * 
	 * @param parkingUrl
	 *            URL of the proprietary parking information
	 * 
	 * @throws org.apache.commons.configuration.ConfigurationException
	 *             if an error occurs while loading ssp.properties
	 */
	public ParkingHLBackend(final String parkingUrl) throws ConfigurationException {
		super();
		this.paringkURL = parkingUrl;
	}

	@Override
	public void bind(final EntityManager em) {
		super.bind(em);
		registerResources();
	}

	private void registerResources() {
		try {

			// get and parse proprietary JSON
			final URL url = new URL(paringkURL);
			final URLConnection conn = url.openConnection();
			final InputStream is = conn.getInputStream();
			final String json = new Scanner(is, "utf8").useDelimiter("\\A").next();
			final Gson gson = new Gson();
			final Parkings parkings = gson.fromJson(json, Parkings.class);
			if (parkings.getParkings() == null) {
				throw new IOException("No parkings could not be parsed from: " + json);
			}

			// create virtual/dummy parking lots as we have only the total amount and free amount in Lübeck
			for (ParkingArea parkingArea : parkings.getParkings()) {
				Collection<ParkingSpace> psl = new ArrayList<ParkingSpace>(parkingArea.getSpaces());

				// the free lots
				for (int lot = 0; lot < parkingArea.getFree(); lot++) {
					psl.add(new ParkingSpace(String.valueOf(lot), parkingArea.getKind(), ParkingLotStatus.FREE, parkingArea.getGeo()));
				}
				// the occupied lots
				for (int lot = parkingArea.getFree(); lot < parkingArea.getSpaces(); lot++) {
					psl.add(new ParkingSpace(String.valueOf(lot), parkingArea.getKind(), ParkingLotStatus.OCCUPIED, parkingArea.getGeo()));
				}
				parkingArea.setParkingSpaces(psl);
			}

			// create resources
			final Collection<Model> models = createModels(parkings.getParkings());
			registerModels(models);
			
			

		} catch (final URISyntaxException e) {
			ParkingHLBackend.log.fatal("This should never happen.", e);
		} catch (final MalformedURLException e) {
			ParkingHLBackend.log.fatal("Parking URL is malformed.", e);
		} catch (final IOException e) {
			ParkingHLBackend.log.fatal("Parking URL is not acessible.", e);
		}

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
				// TODO FMA: set caching option appropriately
				response = new SelfDescription(model, new URI(request.getUri()), new Date());

				if (ParkingHLBackend.log.isDebugEnabled()) {
					ParkingHLBackend.log.debug("[ParkingBackend] Resource found: " + resourceURI);
				}
			} else {
				response = new DefaultHttpResponse(request.getProtocolVersion(), HttpResponseStatus.METHOD_NOT_ALLOWED);

				if (ParkingHLBackend.log.isDebugEnabled()) {
					ParkingHLBackend.log.debug("[ParkingBackend] Method not allowed: " + request.getMethod());
				}
			}
		} else {
			response = HttpResponseFactory.createHttpResponse(request.getProtocolVersion(), HttpResponseStatus.NOT_FOUND);

			if (ParkingHLBackend.log.isDebugEnabled()) {
				ParkingHLBackend.log.debug("[ParkingBackend] Resource not found: " + resourceURI);
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

	@Override
	protected Model addToResources(URI uri, Model model) {
		return resources.put(uri, model);
	}
}
