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
import java.util.Date;
import java.util.HashMap;
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

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.VCARD;

import eu.spitfire_project.smart_service_proxy.core.Backend;
import eu.spitfire_project.smart_service_proxy.core.EntityManager;
import eu.spitfire_project.smart_service_proxy.core.SelfDescription;
import eu.spitfire_project.smart_service_proxy.utils.HttpResponseFactory;

/**
 * A {@link ParkingHLBackend} instance hosts a simple standard model. This back end is basically to ensure the functionality
 * of the underlying handler stack. If it's instantiated (by setting <code>enableBackend="simple"</code> in the
 * <code>ssp.properties</code> file) it registers its service (/JohnSmith) at the {@link EntityManager} instance which
 * causes this service to occur on the HTML page (at <code>http://<ssp-ip>:<ssp-port>/) listing the available services. 
 *
 * @author Oliver Kleine, Florian Massel
 *
 */

public class ParkingHLBackend extends Backend {

    private static Logger log = Logger.getLogger(ParkingHLBackend.class.getName());

    private HashMap<URI, Model> resources = new HashMap<URI, Model>();

    /**
     * Returns a new Backend instance and reads the actual configuration from ssp.properties
     *
     * @throws org.apache.commons.configuration.ConfigurationException
     *          if an error occurs while loading ssp.properties
     */
    public ParkingHLBackend() throws ConfigurationException {
        super();
    }

    public void bind(EntityManager em){
        super.bind(em);
        registerResources();
    }
    
    private void registerResources(){
        try {
            String personURI = "http://example.org/JohnSmith";
            Model model = ModelFactory.createDefaultModel();
            model.createResource(personURI).addProperty(VCARD.FN, "John Smith");
            
            URI resourceURI = new URI(entityManager.getURIBase() + pathPrefix + "JohnSmith");
            resources.put(resourceURI, model);

            if(log.isDebugEnabled()){
                log.debug("[ParkingBackend] Successfully added new resource at " + resourceURI);
            }

        } catch (URISyntaxException e) {
            log.fatal("[ParkingBackend] This should never happen.", e);
        }
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent me) throws Exception{
        if(!(me.getMessage() instanceof HttpRequest)){
            ctx.sendUpstream(me);
            return;
        }

        HttpRequest request = (HttpRequest) me.getMessage();
        Object response;

        //Look up resource
        URI resourceURI = entityManager.normalizeURI(new URI(request.getUri()));
        Model model = resources.get(resourceURI);
            
        if(model != null){
            if(request.getMethod() == HttpMethod.GET){
                response = new SelfDescription(model, new URI(request.getUri()), new Date());
                
                if(log.isDebugEnabled()){
                    log.debug("[ParkingBackend] Resource found: " + resourceURI);
                }
            }
            else{
                response = new DefaultHttpResponse(request.getProtocolVersion(), HttpResponseStatus.METHOD_NOT_ALLOWED);

                if(log.isDebugEnabled()){
                    log.debug("[ParkingBackend] Method not allowed: " + request.getMethod());
                }
            }
        }
        else{
            response = HttpResponseFactory.createHttpResponse(request.getProtocolVersion(),
                    HttpResponseStatus.NOT_FOUND);
            
            if(log.isDebugEnabled()){
                log.debug("[ParkingBackend] Resource not found: " + resourceURI);
            }
        }
       
        //Send response
        ChannelFuture future = Channels.write(ctx.getChannel(), response);
        future.addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public Set<URI> getResources(){
        return resources.keySet();
    }
}
