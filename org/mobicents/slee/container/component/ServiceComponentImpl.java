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

package org.mobicents.slee.container.component;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.slee.ComponentID;
import javax.slee.SbbID;
import javax.slee.ServiceID;
import javax.slee.management.ComponentDescriptor;
import javax.slee.management.DependencyException;
import javax.slee.management.DeploymentException;
import javax.slee.management.ServiceState;

import org.apache.log4j.Logger;
import org.mobicents.slee.container.component.ComponentRepository;
import org.mobicents.slee.container.component.deployment.jaxb.descriptors.ServiceDescriptorImpl;
import org.mobicents.slee.container.component.sbb.ResourceAdaptorEntityBindingDescriptor;
import org.mobicents.slee.container.component.sbb.ResourceAdaptorTypeBindingDescriptor;
import org.mobicents.slee.container.component.sbb.SbbComponent;
import org.mobicents.slee.container.component.service.ServiceComponent;
import org.mobicents.slee.container.management.jmx.ServiceUsageMBean;

/**
 * Start time:16:00:31 2009-01-25<br>
 * Project: mobicents-jainslee-server-core<br>
 * 
 * @author <a href="mailto:baranowb@gmail.com">baranowb - Bartosz Baranowski
 *         </a>
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 */
public class ServiceComponentImpl extends AbstractSleeComponent implements ServiceComponent {

	private static final Logger LOGGER = Logger.getLogger(ServiceComponentImpl.class);
	
	/**
	 * the service descriptor
	 */
	private final ServiceDescriptorImpl descriptor;

	/**
	 * the JAIN SLEE specs descriptor
	 */
	private javax.slee.management.ServiceDescriptor specsDescriptor = null;

	/**
	 * the {@link SbbComponent} the service defines as root
	 */
	private SbbComponent rootSbbComponent = null;

	/**
	 * the usage mbean for this service
	 */
	private ServiceUsageMBean serviceUsageMBean;

	/**
	 * the service alarm notification sources, mapped by sbb id.
	 * 
	 * note: Tracer and Alarm have their own MNotificationSource, sequence don't
	 * match, and alarm sequence numbers must be consistent.
	 */
	private ConcurrentHashMap<SbbID, Object> alarmNotificationSources =
		new ConcurrentHashMap<SbbID, Object>();
	
	/**
	 * the time in milliseconds this component was created
	 */
	private final long creationTime = System.currentTimeMillis();
	
	private ServiceState serviceState = ServiceState.INACTIVE;
	
	/**
	 * 
	 * @param descriptor
	 */
	public ServiceComponentImpl(ServiceDescriptorImpl descriptor) {
		this.descriptor = descriptor;
	}

	/**
	 * Retrieves the service alarm notification sources, mapped by sbb id.
	 * @return
	 */
	public ConcurrentHashMap<SbbID, Object> getAlarmNotificationSources() {
		return alarmNotificationSources;
	}
	
	/**
	 * Retrieves the service descriptor
	 * 
	 * @return
	 */
	public ServiceDescriptorImpl getDescriptor() {
		return descriptor;
	}

	/**
	 * Retrieves the id of the service
	 * 
	 * @return
	 */
	public ServiceID getServiceID() {
		return descriptor.getServiceID();
	}

	@Override
	public boolean addToDeployableUnit() {
		return getDeployableUnit().getServiceComponents().put(getServiceID(), this) == null;
	}
	
	@Override
	public Set<ComponentID> getDependenciesSet() {
		return descriptor.getDependenciesSet();
	}

	@Override
	public boolean isSlee11() {
		return descriptor.isSlee11();
	}

	@Override
	public ComponentID getComponentID() {
		return getServiceID();
	}

	@Override
	public boolean validate() throws DependencyException, DeploymentException {
		// validator needed?
		return true;
	}

	/**
	 * Retrieves the JAIN SLEE specs descriptor
	 * 
	 * @return
	 */
	public javax.slee.management.ServiceDescriptor getSpecsDescriptor() {
		if (specsDescriptor == null) {
			specsDescriptor = new javax.slee.management.ServiceDescriptor(getServiceID(),
					getDeployableUnit().getDeployableUnitID(),
					getDeploymentUnitSource(), descriptor.getRootSbbID(),
					descriptor.getAddressProfileTable(),
					descriptor.getResourceInfoProfileTable());
		}
		return specsDescriptor;
	}

	@Override
	public ComponentDescriptor getComponentDescriptor() {
		return getSpecsDescriptor();
	}

	/**
	 * Retrieves the time in milliseconds this component was created
	 * @return
	 */
	public long getCreationTime() {
		return creationTime;
	}
	
	/**
	 * Retrieves the set of sbbs used by this service
	 * 
	 * @param componentRepository
	 * @return
	 */
	public Set<SbbID> getSbbIDs(ComponentRepository componentRepository) {
		Set<SbbID> result = new HashSet<SbbID>();
		buildSbbTree(descriptor.getRootSbbID(), result,
				componentRepository);
		return result;
	}

	private void buildSbbTree(SbbID sbbID, Set<SbbID> result,
			ComponentRepository componentRepository) {
		result.add(sbbID);
		SbbComponent sbbComponent = componentRepository.getComponentByID(sbbID);
		for (ComponentID componentID : sbbComponent.getDependenciesSet()) {
			if (componentID instanceof SbbID) {
				SbbID anotherSbbID = (SbbID) componentID;
				if (!result.contains(anotherSbbID)) {
					buildSbbTree(anotherSbbID, result, componentRepository);
				}
			}
		}
	}

	/**
	 * Retrieves the set of ra entity links referenced by the sbbs related with the service.
	 * @param componentRepository
	 * @return
	 */
	public Set<String> getResourceAdaptorEntityLinks(ComponentRepository componentRepository) {
		Set<String> result = new HashSet<String>();
		for (SbbID sbbID : getSbbIDs(componentRepository)) {
			SbbComponent sbbComponent = componentRepository.getComponentByID(sbbID);
			for (ResourceAdaptorTypeBindingDescriptor raTypeBinding : sbbComponent.getDescriptor().getResourceAdaptorTypeBindings()) {
				for (ResourceAdaptorEntityBindingDescriptor raEntityBinding : raTypeBinding.getResourceAdaptorEntityBinding()) {
					result.add(raEntityBinding.getResourceAdaptorEntityLink());
				}
			}
		}
		return result;
	}
	
	/**
	 * Retrieves the {@link SbbComponent} the service defines as root
	 * 
	 * @return
	 */
	public SbbComponent getRootSbbComponent() {
		return rootSbbComponent;
	}

	/**
	 * Sets the {@link SbbComponent} the service defines as root
	 * 
	 * @param rootSbbComponent
	 */
	public void setRootSbbComponent(SbbComponent rootSbbComponent) {
		this.rootSbbComponent = rootSbbComponent;
	}

	/**
	 * Retrieves the usage mbean for this service
	 * 
	 * @return
	 */
	public ServiceUsageMBean getServiceUsageMBean() {
		return serviceUsageMBean;
	}

	/**
	 * Sets the usage mbean for this service
	 * 
	 * @param serviceUsageMBean
	 */
	public void setServiceUsageMBean(ServiceUsageMBean serviceUsageMBean) {
		this.serviceUsageMBean = serviceUsageMBean;
	}
	@Override
	public void processSecurityPermissions() throws DeploymentException {
		//Do nothing
		
	}
	
	@Override
	public void undeployed() {
		super.undeployed();
		specsDescriptor = null;
		rootSbbComponent = null;
		serviceUsageMBean = null;
		if (alarmNotificationSources != null) {
			alarmNotificationSources.clear();
			alarmNotificationSources = null;
		}
	}
	
	@Override
	public String toString() {
		return getServiceID().toString();
	}
	
	@Override
	public ServiceState getServiceState() {
		return serviceState;
	}
	
	@Override
	public void setServiceState(ServiceState state) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Changing "+getServiceID()+" state to "+state);
		}
		this.serviceState = state;
	}
	
	private ServiceID oldVersion;
	
	@Override
	public ServiceID getOldVersion() {
		return oldVersion;
	}
	
	@Override
	public void setOldVersion(ServiceID oldVersion) {
		this.oldVersion = oldVersion; 		
	}
}
