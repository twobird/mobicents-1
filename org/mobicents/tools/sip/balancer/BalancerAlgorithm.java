/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.mobicents.tools.sip.balancer;

import java.util.Properties;

import javax.sip.SipProvider;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.jboss.netty.handler.codec.http.HttpRequest;

/**
 * The BalancerAlgortihm interface exposes the methods implemented by decision making algorithms
 * 
 * @author vralev
 *
 */
public interface BalancerAlgorithm {
	/**
	 * When a request comes it is passed to the algorithm. The algorithm must add the Route headers
	 * to the node or nodes where the request should go. The actual proxying is done at the LB.
	 * 
	 * Allowing the algorithm to add headers allows more flexibility for example when you need to
	 * pass some information to the AS or the application to make further LB decisions or multiprotocol
	 * cooperative load balancing. It is very little effort for great flexibility.
	 * 
	 * @param request
	 * @return
	 */
	SIPNode processExternalRequest(Request request);
	SIPNode processAssignedExternalRequest(Request request, SIPNode assignedNode);
	void processInternalRequest(Request request);
	
	/**
	 * Handle HttpRequests here. Use the Netty API for Http request analysis.
	 * @param request
	 * @return
	 */
	SIPNode processHttpRequest(HttpRequest request);
	
	/**
	 * Allow algorithms to process responses
	 * 
	 * @param response
	 */
	void processExternalResponse(Response response);
	void processInternalResponse(Response response);
	
	/**
	 * Notifying the algorithm when a node is dead.
	 * 
	 * @param node
	 */
	void nodeRemoved(SIPNode node);
	
	/**
	 * Notify the algorithm when a node is added.
	 * @param node
	 */
	void nodeAdded(SIPNode node);
	
	/**
	 * Get the properties used to load the load balancer. This way you can read algorithm-specific settings
	 * from the main configuration file - the lb.properties.
	 * 
	 * @return
	 */
	Properties getProperties();
	
	/**
	 * Also allows to change the properties completely when it makes sense
	 * @param properties
	 */
	void setProperties(Properties properties);
	
	/**
	 * Get the balancer context, which exposes useful information such as the available AS nodes at the moment
	 * or the listening points if you need the local address.
	 * @return
	 */
	BalancerContext getBalancerContext();
	
	/**
	 * Move load from one node to another to follow mod_jk/mod_cluster
	 * @param fromJvmRoute
	 * @param toJvmRoute
	 */
	void jvmRouteSwitchover(String fromJvmRoute, String toJvmRoute);
	
	/**
	 * Lifecycle method. Notifies the algorithm when it's initialized with properties and balancer context.
	 */
	void init();
	
	/**
	 * Lifecycle method. Notifies the algorithm when it's being shut down.
	 */
	void stop();
	
	/**
	 * Assign callid to node
	 */
	void assignToNode(String id, SIPNode node);
	
	void configurationChanged();
}
