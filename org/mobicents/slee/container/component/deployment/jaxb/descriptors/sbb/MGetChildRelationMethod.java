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

/**
 * Start time:11:43:35 2009-01-20<br>
 * Project: mobicents-jainslee-server-core<br>
 * 
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski
 *         </a>
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 */
package org.mobicents.slee.container.component.deployment.jaxb.descriptors.sbb;

import java.io.Serializable;

import javax.slee.SbbID;

import org.mobicents.slee.container.component.sbb.GetChildRelationMethodDescriptor;


/**
 * Start time:11:43:35 2009-01-20<br>
 * Project: mobicents-jainslee-server-core<br>
 * 
 * @author <a href="mailto:baranowb@gmail.com">Bartosz Baranowski
 *         </a>
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 */
public class MGetChildRelationMethod implements GetChildRelationMethodDescriptor,Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5123911596278960115L;
	private String description=null;
	private String sbbAliasRef=null;
	private String childRelationMethodName=null;
	private byte defaultPriority=0;
	
	/**
	 * the sbb id referenced by the alias
	 */
	private SbbID sbbID;
	
	public MGetChildRelationMethod(org.mobicents.slee.container.component.deployment.jaxb.slee.sbb.GetChildRelationMethod getChildRelationMethod) {
		super();
		this.description=getChildRelationMethod.getDescription()==null?null:getChildRelationMethod.getDescription().getvalue();
		this.sbbAliasRef=getChildRelationMethod.getSbbAliasRef().getvalue();
		this.childRelationMethodName=getChildRelationMethod.getGetChildRelationMethodName().getvalue();
		String v=getChildRelationMethod.getDefaultPriority().getvalue();
		//This will fail when def priority is empty
		this.defaultPriority=Byte.parseByte(v);
	}
	public MGetChildRelationMethod(
			org.mobicents.slee.container.component.deployment.jaxb.slee11.sbb.GetChildRelationMethod llGetChildRelationMethod) {
		super();
		
		this.description=llGetChildRelationMethod.getDescription()==null?null:llGetChildRelationMethod.getDescription().getvalue();
		this.sbbAliasRef=llGetChildRelationMethod.getSbbAliasRef().getvalue();
		this.childRelationMethodName=llGetChildRelationMethod.getGetChildRelationMethodName().getvalue();
		String v=llGetChildRelationMethod.getDefaultPriority().getvalue();
		this.defaultPriority=Byte.parseByte(v);
	}
	public String getDescription() {
		return description;
	}
	public String getSbbAliasRef() {
		return sbbAliasRef;
	}
	public String getChildRelationMethodName() {
		return childRelationMethodName;
	}
	public byte getDefaultPriority() {
		return defaultPriority;
	}
	
	public SbbID getSbbID() {
		return sbbID;
	}
	
	public void setSbbID(SbbID sbbID) {
		this.sbbID = sbbID;
	}
		
}
