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

package org.mobicents.slee.resource.xmpp;

import java.util.concurrent.ConcurrentHashMap;

import javax.slee.EventTypeID;
import javax.slee.facilities.EventLookupFacility;
import javax.slee.facilities.Tracer;
import javax.slee.resource.FireableEventType;

/**
 * Caches event types for Xmpp RA
 * @author martins
 *
 */
public class FireableEventTypeCache {

	public static final String VENDOR = "org.jivesoftware.smack";
	public static final String VERSION = "1.0";
		
	private ConcurrentHashMap<String, FireableEventType> eventTypes = new ConcurrentHashMap<String, FireableEventType>();
	
	private final Tracer tracer;
	
	public FireableEventTypeCache(Tracer tracer) {
		this.tracer = tracer;
	}
	
	public String getEventName(Object event) {
		return event.getClass().getName();
	}
	
	public FireableEventType getEventType(EventLookupFacility eventLookupFacility, Object event) {
		String eventName = getEventName(event);
		FireableEventType eventType = eventTypes.get(eventName); 
		if (eventType == null) {
			try {
				eventType = eventLookupFacility.getFireableEventType(new EventTypeID(eventName, VENDOR, VERSION));
			} catch (Throwable e) {
				tracer.severe("Failed to obtain fireable event type for event with name "+eventName,e);
				return null;
			}
			eventTypes.put(eventName, eventType);
		}
		return eventType;	
	}
	
}
