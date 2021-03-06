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

package org.mobicents.ext.javax.sip.dns;

import java.util.Queue;

import javax.sip.address.Hop;
import javax.sip.address.SipURI;
import javax.sip.address.URI;

/**
 * Interface to implement for discovering servers according to RFC 3263 and/or ENUM support. 
 * 
 * @author jean.deruelle@gmail.com
 *
 */
public interface DNSServerLocator {

	/**
	 * Discovers servers according to RFC 3263 and/or ENUM support for the given uri passed in parameters
	 * @param uri the uri for which the DNS lookups have to be done
	 * @return a queue of Hop that have to be tried each one in turn.
	 */
	Queue<Hop> locateHops(URI uri);
	
	/**
	 * <p>From the uri passed in parameter, try to find the corresponding SipURI.
	 * If the uri in parameter is already a SipURI without a user=phone param, it is just returned
	 * If the uri in parameter is a TelURL or SipURI with a user=phone param, the phone number is converted to a domain name
	 * then a corresponding NAPTR DNS lookup is done to find the SipURI</p>
	 * 
	 * <p> Usage Example </p>
	 * <pre>
	 * 	
	 * try {
	 *		URI uri = addressFactory.createTelURL("+12153208617");
	 *		SipURI sipURI = dnsServerLocator.getSipURI(uri);
	 * } catch (ServletParseException e) {
	 * 		logger.error("Impossible to create the tel URL", e);
	 * }
	 * </pre>
	 * 
	 * @param uri the uri used to find the corresponding SipURI
	 * @return the SipURI found through ENUM methods or the uri itself if the uri is already a SipURI without a user=phone param
	 */
	SipURI getSipURI(URI uri);

	/**
	 * Adds a local host name for which the DNS lookups can be bypassed, by example if an URI is targeted at the local server
	 * @param localHostName the name of the local host to add 
	 */
	void addLocalHostName(String localHostName);
	/**
	 * Removes a local host name for which the DNS lookups can be bypassed, by example if an URI is targeted at the local server
	 * @param localHostName the name of the local host to remove
	 */
	void removeLocalHostName(String localHostName);
	/**
	 * Add a supported transport (UDP, TCP, TLS, SCTP) to the set of supported transports.
	 * The supported transports are needed for supporting RFC 3263 in specific cases
	 * @param supportedTransport the supported transport to add
	 */
	void addSupportedTransport(String supportedTransport);
	/**
	 * Remove a supported transport (UDP, TCP, TLS, SCTP) to the set of supported transports.
	 * The supported transports are needed for supporting RFC 3263 in specific cases
	 * @param supportedTransport the supported transport to remove
	 */
	void removeSupportedTransport(String supportedTransport);

	/**
	 * Sets the {@link DNSLookupPerformer} to perform the DNS lookups
	 * @param dnsLookupPerformer the dnsLookupPerformer to set
	 */
	void setDnsLookupPerformer(
			DNSLookupPerformer dnsLookupPerformer);

	/**
	 * Retrieve the {@link DNSLookupPerformer} performing the DNS lookups
	 * @return the dnsLookupPerformer
	 */
	DNSLookupPerformer getDnsLookupPerformer();

}