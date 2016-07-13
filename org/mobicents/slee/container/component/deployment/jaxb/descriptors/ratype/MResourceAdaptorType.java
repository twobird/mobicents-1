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

package org.mobicents.slee.container.component.deployment.jaxb.descriptors.ratype;

import java.util.ArrayList;
import java.util.List;

import javax.slee.EventTypeID;
import javax.slee.management.LibraryID;

/**
 * 
 * MResourceAdaptorType.java
 *
 * <br>Project:  mobicents
 * <br>11:50:43 AM Jan 22, 2009 
 * <br>
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a> 
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 */
public class MResourceAdaptorType {

  private String description;
  private String resourceAdaptorTypeName;
  private String resourceAdaptorTypeVendor;
  private String resourceAdaptorTypeVersion;
  private List<LibraryID> libraryRefs = new ArrayList<LibraryID>();
  private MResourceAdaptorTypeClasses resourceAdaptorTypeClasses;
  private List<EventTypeID> eventTypeRefs = new ArrayList<EventTypeID>();
  
  public MResourceAdaptorType(org.mobicents.slee.container.component.deployment.jaxb.slee.ratype.ResourceAdaptorType resourceAdaptorType10)
  {
    
    this.description = resourceAdaptorType10.getDescription() == null ? null : resourceAdaptorType10.getDescription().getvalue();
    
    this.resourceAdaptorTypeName = resourceAdaptorType10.getResourceAdaptorTypeName().getvalue();
    this.resourceAdaptorTypeVendor = resourceAdaptorType10.getResourceAdaptorTypeVendor().getvalue();
    this.resourceAdaptorTypeVersion = resourceAdaptorType10.getResourceAdaptorTypeVersion().getvalue();
        
    this.resourceAdaptorTypeClasses = new MResourceAdaptorTypeClasses(resourceAdaptorType10.getResourceAdaptorTypeClasses());
    
    for(org.mobicents.slee.container.component.deployment.jaxb.slee.ratype.EventTypeRef eventTypeRef10 : resourceAdaptorType10.getEventTypeRef())
    {
      this.eventTypeRefs.add( new EventTypeID(eventTypeRef10.getEventTypeName().getvalue(),eventTypeRef10.getEventTypeVendor().getvalue(),eventTypeRef10.getEventTypeVersion().getvalue()) );
    }
  }
  
  public MResourceAdaptorType(org.mobicents.slee.container.component.deployment.jaxb.slee11.ratype.ResourceAdaptorType resourceAdaptorType11)
  {
    
    this.description = resourceAdaptorType11.getDescription() == null ? null : resourceAdaptorType11.getDescription().getvalue();
    
    this.resourceAdaptorTypeName = resourceAdaptorType11.getResourceAdaptorTypeName().getvalue();
    this.resourceAdaptorTypeVendor = resourceAdaptorType11.getResourceAdaptorTypeVendor().getvalue();
    this.resourceAdaptorTypeVersion = resourceAdaptorType11.getResourceAdaptorTypeVersion().getvalue();
    
    for(org.mobicents.slee.container.component.deployment.jaxb.slee11.ratype.LibraryRef libraryRef11 : resourceAdaptorType11.getLibraryRef())
    {
    	this.libraryRefs.add(new LibraryID(libraryRef11.getLibraryName()
    			.getvalue(), libraryRef11.getLibraryVendor().getvalue(),
    			libraryRef11.getLibraryVersion().getvalue()));
    }
    
    this.resourceAdaptorTypeClasses = new MResourceAdaptorTypeClasses(resourceAdaptorType11.getResourceAdaptorTypeClasses());
    
    for(org.mobicents.slee.container.component.deployment.jaxb.slee11.ratype.EventTypeRef eventTypeRef11 : resourceAdaptorType11.getEventTypeRef())
    {
    	this.eventTypeRefs.add( new EventTypeID(eventTypeRef11.getEventTypeName().getvalue(),eventTypeRef11.getEventTypeVendor().getvalue(),eventTypeRef11.getEventTypeVersion().getvalue()) );    }
  }
  
  public String getDescription()
  {
    return description;
  }
  
  public String getResourceAdaptorTypeName()
  {
    return resourceAdaptorTypeName;
  }
  
  public String getResourceAdaptorTypeVendor()
  {
    return resourceAdaptorTypeVendor;
  }
  
  public String getResourceAdaptorTypeVersion()
  {
    return resourceAdaptorTypeVersion;
  }
  
  public List<LibraryID> getLibraryRefs()
  {
    return libraryRefs;
  }
  
  public MResourceAdaptorTypeClasses getResourceAdaptorTypeClasses()
  {
    return resourceAdaptorTypeClasses;
  }
  
  public List<EventTypeID> getEventTypeRefs()
  {
    return eventTypeRefs;
  }
  
}
