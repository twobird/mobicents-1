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

package org.mobicents.slee.resource.xcapclient;

import java.io.Serializable;
import java.net.URI;

import org.mobicents.xcap.client.XcapResponse;

public final class ResponseEvent implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private XcapResponse response = null;
	private Exception exception = null;
	private int id;
	private URI uri;
	
	public ResponseEvent(XcapResponse response, URI uri) {
		this.response = response;
		this.uri = uri;
		id = response.hashCode()*31+"null".hashCode();
	}
	
	public ResponseEvent(Exception exception, URI uri) {
		this.exception = exception;
		this.uri = uri;
		id = "null".hashCode()*31+exception.hashCode();
	}
	
	public URI getURI() {
		return uri;
	}
	
	public XcapResponse getResponse() {
		return response;
	}
	
	public Exception getException() {
		return exception;
	}
	
	public boolean equals(Object o) {
		if (o != null && o.getClass() == this.getClass()) {
			return ((ResponseEvent)o).id == this.id;
		}
		else {
			return false;
		}
	}
	
	public int hashCode() {		
		return id;
	}
}
