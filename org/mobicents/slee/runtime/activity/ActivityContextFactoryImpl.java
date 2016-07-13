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

package org.mobicents.slee.runtime.activity;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.slee.SLEEException;
import javax.slee.resource.ActivityAlreadyExistsException;

import org.apache.log4j.Logger;
import org.jboss.cache.Fqn;
import org.mobicents.cluster.DataRemovalListener;
import org.mobicents.slee.container.AbstractSleeContainerModule;
import org.mobicents.slee.container.SleeContainer;
import org.mobicents.slee.container.activity.ActivityContextFactory;
import org.mobicents.slee.container.activity.ActivityContextHandle;
import org.mobicents.slee.container.activity.ActivityType;
import org.mobicents.slee.container.eventrouter.EventRouterExecutor;

/**
 * Activity context factory -- return an activity context given an activity or
 * create one and put it in the table. This also implements the activity context
 * naming facility for the SLEE.
 * 
 * @author F.Moggia
 * @author M. Ranganathan
 * @author Tim Fox
 * @author eduardomartins second version
 * @version 2.0
 * 
 * 
 */
public class ActivityContextFactoryImpl extends AbstractSleeContainerModule implements ActivityContextFactory {

	private static Logger logger = Logger.getLogger(ActivityContextFactoryImpl.class);
	
	/**
	 * a map with the local resources related with an activity context, which hold all runtime structures related to the activity
	 */
	private final ConcurrentHashMap<ActivityContextHandle, LocalActivityContextImpl> localActivityContexts = new ConcurrentHashMap<ActivityContextHandle, LocalActivityContextImpl>();
	
	private ActivityContextFactoryCacheData cacheData;
	
	private final ActivityManagementConfiguration configuration;
	
	private final static boolean doTraceLogs = logger.isTraceEnabled();
	
	public ActivityContextFactoryImpl(ActivityManagementConfiguration configuration) {
		this.configuration = configuration;
	}
	
	/**
	 * 
	 * @return
	 */
	public ActivityManagementConfiguration getConfiguration() {
		return configuration;
	}
	
	@Override
	public void sleeInitialization() {
		sleeContainer.getCluster().addDataRemovalListener(new DataRemovaClusterListener());		
	}
	
	@Override
	public void sleeStarting() {
		cacheData = new ActivityContextFactoryCacheData(sleeContainer.getCluster());
		cacheData.create();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.slee.container.AbstractSleeContainerModule#getSleeContainer()
	 */
	public SleeContainer getSleeContainer() {
		return sleeContainer;
	}
	
	LocalActivityContextImpl getLocalActivityContext(ActivityContextImpl ac) {
		final ActivityContextHandle ach = ac.getActivityContextHandle();
		LocalActivityContextImpl localActivityContext = localActivityContexts.get(ach);
		if (localActivityContext == null) {
			final LocalActivityContextImpl newLocalActivityContext = new LocalActivityContextImpl(ach,ac.getActivityFlags(),this);
			localActivityContext = localActivityContexts.putIfAbsent(ach,newLocalActivityContext);
			if (localActivityContext == null) {
				localActivityContext = newLocalActivityContext;
				EventRouterExecutor executor = sleeContainer.getEventRouter().getEventRouterExecutorMapper().getExecutor(ach);
				localActivityContext.setExecutorService(executor);
				executor.activityMapped(ach);
			}
		}
		return localActivityContext;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.slee.container.activity.ActivityContextFactory#createActivityContext(org.mobicents.slee.container.activity.ActivityContextHandle, int)
	 */
	public ActivityContextImpl createActivityContext(final ActivityContextHandle ach, int activityFlags) throws ActivityAlreadyExistsException {
		
		if (sleeContainer.getCongestionControl().refuseStartActivity()) {
			throw new SLEEException("congestion control refused activity start");
		}
		
		// create ac
		ActivityContextCacheData activityContextCacheData = new ActivityContextCacheData(ach, sleeContainer.getCluster());
		if (activityContextCacheData.exists()) {
			throw new ActivityAlreadyExistsException(ach.toString());
		}
				
		ActivityContextImpl ac = new ActivityContextImpl(ach,activityContextCacheData,tracksIdleTime(ach,true),Integer.valueOf(activityFlags),this);
		if (logger.isDebugEnabled()) {
			logger.debug("Created activity context with handle "+ach);			
		}
		return ac;
	}

	private boolean tracksIdleTime(ActivityContextHandle ach, boolean updateLastAccessTime) {
		if(!updateLastAccessTime) {
			return false;
		}
		if (configuration.getTimeBetweenLivenessQueries() < 1) {
			return false;
		}
		return ach.getActivityType() == ActivityType.RA;
	}

	@Override
	public ActivityContextImpl getActivityContext(ActivityContextHandle ach) {
		return getActivityContext(ach,false);
	}
	
	@Override
	public ActivityContextImpl getActivityContext(ActivityContextHandle ach, boolean updateLastAccessTime) {
		ActivityContextCacheData activityContextCacheData = new ActivityContextCacheData(ach, sleeContainer.getCluster());
		if (activityContextCacheData.exists()) {
			return new ActivityContextImpl(ach,activityContextCacheData,tracksIdleTime(ach, updateLastAccessTime),this);
		}
		else {
			return null; 
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.slee.container.activity.ActivityContextFactory#getAllActivityContextsHandles()
	 */
	public Set<ActivityContextHandle> getAllActivityContextsHandles() {
		return cacheData.getActivityContextHandles();
	}
	
	public void removeActivityContext(final ActivityContextImpl ac) {

		if (doTraceLogs) {
			logger.trace("Removing activity context "+ac.getActivityContextHandle());
		}
		
		// remove runtime resources
		final LocalActivityContextImpl localActivityContext = localActivityContexts.remove(ac.getActivityContextHandle());
		if (localActivityContext != null) {
			localActivityContext.getExecutorService().activityUnmapped(ac.getActivityContextHandle());
		}
				
		if (logger.isDebugEnabled()) {
			logger.debug("Removed activity context with handle "+ac.getActivityContextHandle());			
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.slee.container.activity.ActivityContextFactory#getActivityContextCount()
	 */
	public int getActivityContextCount() {		
		return getAllActivityContextsHandles().size();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.slee.container.activity.ActivityContextFactory#activityContextExists(org.mobicents.slee.container.activity.ActivityContextHandle)
	 */
	public boolean activityContextExists(ActivityContextHandle ach) {
		return new ActivityContextCacheData(ach, sleeContainer.getCluster()).exists();
	}	
	
	@Override
	public String toString() {
		return "ActivityContext Factory: " 
			+ "\n+-- Number of Local ACs: " + localActivityContexts.size()
			+ "\n+-- Number of ACs: " + getActivityContextCount();
	}
	
	private class DataRemovaClusterListener implements DataRemovalListener {
		
		@SuppressWarnings("rawtypes")
		public void dataRemoved(Fqn arg0) {
			final ActivityContextHandle ach = (ActivityContextHandle) arg0.getLastElement();
			final LocalActivityContextImpl localActivityContext = localActivityContexts.remove(ach);
			if(localActivityContext != null) {
				final EventRouterExecutor executor = localActivityContext.getExecutorService(); 
				if (executor != null) {
					executor.activityUnmapped(localActivityContext.getActivityContextHandle());
				}
				localActivityContext.setExecutorService(null);
				if(doTraceLogs) {
					logger.trace("Remotely removed local AC for "+ach);
				}
			}
		}

		@SuppressWarnings("rawtypes")
		public Fqn getBaseFqn() {
			return ActivityContextFactoryCacheData.NODE_FQN;
		}
		
	}
}