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

package org.mobicents.slee.container.component.deployment.jaxb.descriptors.sbb;

import java.util.ArrayList;
import java.util.List;

import org.mobicents.slee.container.component.deployment.jaxb.descriptors.common.MSecurityPermissions;

/**
 * 
 * MSbbJar.java
 *
 * <br>Project:  mobicents
 * <br>1:01:28 PM Feb 16, 2009 
 * <br>
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a> 
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 */
public class MSbbJar {

  private String description;
  
  private List<MSbb> sbb;

  private MSecurityPermissions securityPermissions;
  
  public MSbbJar(org.mobicents.slee.container.component.deployment.jaxb.slee.sbb.SbbJar sbbJar10)
  {
    this.description = sbbJar10.getDescription() == null ? null : sbbJar10.getDescription().getvalue();
    
    this.sbb = new ArrayList<MSbb>();
    
    for(org.mobicents.slee.container.component.deployment.jaxb.slee.sbb.Sbb sbb10 : sbbJar10.getSbb())
    {
      this.sbb.add( new MSbb(sbb10) );
    }
  }

  public MSbbJar(org.mobicents.slee.container.component.deployment.jaxb.slee11.sbb.SbbJar sbbJar11)
  {
    this.description = sbbJar11.getDescription() == null ? null : sbbJar11.getDescription().getvalue();

    this.sbb = new ArrayList<MSbb>();

    for(org.mobicents.slee.container.component.deployment.jaxb.slee11.sbb.Sbb sbb11 : sbbJar11.getSbb())
    {
      this.sbb.add( new MSbb(sbb11) );
    }
    
    if(sbbJar11.getSecurityPermissions()!=null)
    	this.securityPermissions = new MSecurityPermissions(sbbJar11.getSecurityPermissions());
  }
  
  public String getDescription()
  {
    return description;
  }
  
  public List<MSbb> getSbb()
  {
    return sbb;
  }
  
  public MSecurityPermissions getSecurityPermissions()
  {
    return securityPermissions;
  }
}
