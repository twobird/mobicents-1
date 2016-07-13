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

package org.mobicents.slee.container.component.validator;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.mobicents.slee.container.component.ComponentRepository;
import org.mobicents.slee.container.component.ServiceComponentImpl;
import org.mobicents.slee.container.component.deployment.jaxb.descriptors.ServiceDescriptorImpl;
import org.mobicents.slee.container.component.service.ServiceComponent;

public class ServiceValidator implements Validator {

	private ServiceComponentImpl component = null;
	private ComponentRepository repository = null;
	private final static transient Logger logger = Logger.getLogger(ServiceValidator.class);

	public void setComponentRepository(ComponentRepository repository) {
		this.repository = repository;

	}

	public ServiceComponentImpl getComponent() {
		return component;
	}

	public void setComponent(ServiceComponentImpl component) {
		this.component = component;
	}

	public boolean validate() {
		boolean passed = true;
		
		if(!validateDescriptor())
		{
			passed = false;
			return passed;
		}
		
		
		if(validateCompatibilityReferenceConstraints())
		{
			passed = false;
			
		}
		
		
		return passed;
	}

	boolean validateDescriptor() {
		boolean passed = true;
		String errorBuffer = new String("");

		try {
			ServiceDescriptorImpl descritpor=this.component.getDescriptor();
			//Nothign so far here.
		} finally {
			if (!passed) {
				if (logger.isEnabledFor(Level.ERROR)) {
					logger.error(errorBuffer);
				}
			}
		}
		return passed;
	}

	/**
	 * See section 1.3 of jslee 1.1 specs
	 * 
	 * @return
	 */
	boolean validateCompatibilityReferenceConstraints() {

		boolean passed = true;
		String errorBuffer = new String("");

		try {
			if (!this.component.isSlee11()) {
				// A 1.0 SBB must not reference or use a 1.1 Profile
				// Specification. This must be enforced by a 1.1
				// JAIN SLEE.

				ServiceComponent specComponent = this.repository.getComponentByID(this.component.getServiceID());
				if (specComponent == null) {
					// should not happen
					passed = false;
					errorBuffer = appendToBuffer("Referenced " + this.component.getServiceID()
							+ " was not found in component repository, this should not happen since dependencies were already verified", "1.3", errorBuffer);

				} else {
					if (specComponent.isSlee11()) {
						passed = false;
						errorBuffer = appendToBuffer("Service is following 1.0 JSLEE contract, it must not reference 1.1 Sbb as root: " + this.component.getServiceID(), "1.3", errorBuffer);
					}
				}

			}
		} finally {
			if (!passed) {
				if (logger.isEnabledFor(Level.ERROR)) {
					logger.error(errorBuffer);
				}
			}

		}

		return passed;
	}

	protected String appendToBuffer(String message, String section, String buffer) {
		buffer += (this.component.getDescriptor().getServiceID() + " : violates section " + section + " of jSLEE 1.1 specification : " + message + "\n");
		return buffer;
	}
}
