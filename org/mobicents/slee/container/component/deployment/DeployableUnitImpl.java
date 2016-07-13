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
 * Start time:17:44:15 2009-01-25<br>
 * Project: mobicents-jainslee-server-core<br>
 * 
 * @author <a href="mailto:baranowb@gmail.com">baranowb - Bartosz Baranowski
 *         </a>
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 * @author martins
 */
package org.mobicents.slee.container.component.deployment;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.slee.ComponentID;
import javax.slee.EventTypeID;
import javax.slee.SbbID;
import javax.slee.ServiceID;
import javax.slee.management.DeployableUnitDescriptor;
import javax.slee.management.DeployableUnitID;
import javax.slee.management.LibraryID;
import javax.slee.profile.ProfileSpecificationID;
import javax.slee.resource.ResourceAdaptorID;
import javax.slee.resource.ResourceAdaptorTypeID;

import org.mobicents.slee.container.component.AbstractSleeComponent;
import org.mobicents.slee.container.component.ComponentRepository;
import org.mobicents.slee.container.component.SleeComponent;
import org.mobicents.slee.container.component.deployment.jaxb.descriptors.DeployableUnitDescriptorImpl;
import org.mobicents.slee.container.component.du.DeployableUnit;
import org.mobicents.slee.container.component.event.EventTypeComponent;
import org.mobicents.slee.container.component.library.LibraryComponent;
import org.mobicents.slee.container.component.profile.ProfileSpecificationComponent;
import org.mobicents.slee.container.component.ra.ResourceAdaptorComponent;
import org.mobicents.slee.container.component.ratype.ResourceAdaptorTypeComponent;
import org.mobicents.slee.container.component.sbb.SbbComponent;
import org.mobicents.slee.container.component.service.ServiceComponent;

/**
 * Start time:17:44:15 2009-01-25<br>
 * Project: mobicents-jainslee-server-core<br>
 * 
 * @author <a href="mailto:baranowb@gmail.com">baranowb - Bartosz Baranowski
 *         </a>
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 */
public class DeployableUnitImpl implements DeployableUnit {

	/**
	 * the DU id
	 */
	private final DeployableUnitID id;

	/**
	 * the DU descriptor
	 */
	private final DeployableUnitDescriptorImpl descriptor;

	/**
	 * the DU repository, provides an extended view of the container's component
	 * repository
	 */
	private final DeployableUnitRepositoryImpl repository;

	/**
	 * the temp dir where the DU is installed
	 */
	private final File deploymentDir;

	/**
	 * the DU event type components
	 */
	private final Map<EventTypeID, EventTypeComponent> eventTypeComponents = new HashMap<EventTypeID, EventTypeComponent>();

	/**
	 * the DU library components
	 */
	private final Map<LibraryID, LibraryComponent> libraryComponents = new HashMap<LibraryID, LibraryComponent>();

	/**
	 * the DU profile spec components
	 */
	private final Map<ProfileSpecificationID, ProfileSpecificationComponent> profileSpecificationComponents = new HashMap<ProfileSpecificationID, ProfileSpecificationComponent>();

	/**
	 * the DU ra components
	 */
	private final Map<ResourceAdaptorID, ResourceAdaptorComponent> resourceAdaptorComponents = new HashMap<ResourceAdaptorID, ResourceAdaptorComponent>();

	/**
	 * the DU ratype components
	 */
	private final Map<ResourceAdaptorTypeID, ResourceAdaptorTypeComponent> resourceAdaptorTypeComponents = new HashMap<ResourceAdaptorTypeID, ResourceAdaptorTypeComponent>();

	/**
	 * the DU sbb components
	 */
	private final Map<SbbID, SbbComponent> sbbComponents = new HashMap<SbbID, SbbComponent>();

	/**
	 * the DU service components
	 */
	private final Map<ServiceID, ServiceComponent> serviceComponents = new HashMap<ServiceID, ServiceComponent>();

	/**
	 * the date this deployable unit was built
	 */
	private final Date date = new Date();

	public DeployableUnitImpl(DeployableUnitID deployableUnitID,
			DeployableUnitDescriptorImpl duDescriptor,
			ComponentRepository componentRepository, File deploymentDir) {
		if (deployableUnitID == null) {
			throw new NullPointerException("null deployableUnitID");
		}
		if (duDescriptor == null) {
			throw new NullPointerException("null duDescriptor");
		}
		if (componentRepository == null) {
			throw new NullPointerException("null componentRepository");
		}
		if (deploymentDir == null) {
			throw new NullPointerException("null deploymentDir");
		}
		this.id = deployableUnitID;
		this.descriptor = duDescriptor;
		this.repository = new DeployableUnitRepositoryImpl(this,
				componentRepository);
		this.deploymentDir = deploymentDir;
	}

	/**
	 * Retrieves the DU descriptor
	 * 
	 * @return
	 */
	public DeployableUnitDescriptorImpl getDeployableUnitDescriptor() {
		return descriptor;
	}

	/**
	 * Retrieves the DU id
	 * 
	 * @return
	 */
	public DeployableUnitID getDeployableUnitID() {
		return id;
	}

	/**
	 * Retrieves the DU component repository
	 * 
	 * @return
	 */
	public DeployableUnitRepositoryImpl getDeployableUnitRepository() {
		return repository;
	}

	/**
	 * Retrieves the temp dir where the DU is installed
	 * 
	 * @return
	 */
	public File getDeploymentDir() {
		return deploymentDir;
	}

	/**
	 * Retrieves the DU event type components
	 * 
	 * @return
	 */
	public Map<EventTypeID, EventTypeComponent> getEventTypeComponents() {
		return eventTypeComponents;
	}

	/**
	 * Retrieves the DU library components
	 * 
	 * @return
	 */
	public Map<LibraryID, LibraryComponent> getLibraryComponents() {
		return libraryComponents;
	}

	/**
	 * Retrieves the DU profile spec components
	 * 
	 * @return
	 */
	public Map<ProfileSpecificationID, ProfileSpecificationComponent> getProfileSpecificationComponents() {
		return profileSpecificationComponents;
	}

	/**
	 * Retrieves the DU ra components
	 * 
	 * @return
	 */
	public Map<ResourceAdaptorID, ResourceAdaptorComponent> getResourceAdaptorComponents() {
		return resourceAdaptorComponents;
	}

	/**
	 * Retrieves the DU ratype components
	 * 
	 * @return
	 */
	public Map<ResourceAdaptorTypeID, ResourceAdaptorTypeComponent> getResourceAdaptorTypeComponents() {
		return resourceAdaptorTypeComponents;
	}

	/**
	 * Retrieves the DU sbb components
	 * 
	 * @return
	 */
	public Map<SbbID, SbbComponent> getSbbComponents() {
		return sbbComponents;
	}

	/**
	 * Retrieves the DU service components
	 * 
	 * @return
	 */
	public Map<ServiceID, ServiceComponent> getServiceComponents() {
		return serviceComponents;
	}

	/**
	 * Returns an unmodifiable set with all {@link AbstractSleeComponent}s of the
	 * deployable unit.
	 * 
	 * @return
	 */
	public Set<SleeComponent> getDeployableUnitComponents() {
		Set<SleeComponent> result = new HashSet<SleeComponent>();
		result.addAll(getEventTypeComponents().values());
		result.addAll(getLibraryComponents().values());
		result.addAll(getProfileSpecificationComponents().values());
		result.addAll(getResourceAdaptorComponents().values());
		result.addAll(getResourceAdaptorTypeComponents().values());
		result.addAll(getSbbComponents().values());
		result.addAll(getServiceComponents().values());
		return Collections.unmodifiableSet(result);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj.getClass() == this.getClass()) {
			return ((DeployableUnitImpl) obj).id.equals(this.id);
		} else {
			return false;
		}
	}

	/**
	 * Undeploys this unit
	 */
	public void undeploy() {		
		// remove components
		for (SleeComponent component : getDeployableUnitComponents()) {
			component.undeployed();
		}
		// now delete the deployment dir
		deletePath(getDeploymentDir());
	}

	/**
	 * deletes the whole path, going through directories
	 * 
	 * @param path
	 */
	private void deletePath(File path) {
		if (path.isDirectory()) {
			File[] files = path.listFiles();
			if (files != null) {
				for (File file : files) {
					deletePath(file);
				}
			}
		}
		path.delete();
	}

	/**
	 * Returns the {@link DeployableUnitDescriptor} for this deployable unit.
	 * 
	 * @return
	 */
	public javax.slee.management.DeployableUnitDescriptor getSpecsDeployableUnitDescriptor() {
		Set<ComponentID> componentIDs = new HashSet<ComponentID>();
		for (SleeComponent component : getDeployableUnitComponents()) {
			componentIDs.add(component.getComponentID());
		}
		return new DeployableUnitDescriptor(getDeployableUnitID(), date,
				componentIDs.toArray(new ComponentID[0]));
	}
	
}
