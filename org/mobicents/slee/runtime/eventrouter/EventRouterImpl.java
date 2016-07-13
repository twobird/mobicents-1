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

package org.mobicents.slee.runtime.eventrouter;

import org.apache.log4j.Logger;
import org.mobicents.slee.container.AbstractSleeContainerModule;
import org.mobicents.slee.container.eventrouter.EventRouter;
import org.mobicents.slee.container.eventrouter.EventRouterExecutor;
import org.mobicents.slee.container.eventrouter.EventRouterExecutorMapper;
import org.mobicents.slee.container.eventrouter.stats.EventRouterStatistics;
import org.mobicents.slee.container.management.jmx.EventRouterConfiguration;
import org.mobicents.slee.runtime.eventrouter.stats.EventRouterStatisticsImpl;

/**
 * 
 * @author Eduardo Martins
 */

public class EventRouterImpl extends AbstractSleeContainerModule implements EventRouter {

	private static Logger logger = Logger.getLogger(EventRouter.class);
	
	/**
	 * The array of {@link EventRouterExecutor}s that are used to route events
	 */
	private volatile EventRouterExecutor[] executors;
		
	/**
	 * Maps executors to activities.
	 */
	private EventRouterExecutorMapper executorMapper;
	
	/**
	 * Provides performance and load statistics of the event router.
	 */
	private EventRouterStatistics statistics;
	
	private final EventRouterConfiguration configuration;
	
	/**
	 * 
	 */
	public EventRouterImpl(EventRouterConfiguration configuration) {
		this.configuration = configuration;
	}
	
	@Override
	public void sleeInitialization() {
		logger
		.info("Mobicents JAIN SLEE Event Router started.");
	}
	
	@Override
	public void sleeStarting() {
		System.out.println("sleeStarting()");
		// get ridden of old executors, if any
		if (this.executors != null) {
			for (EventRouterExecutor executor : this.executors) {
				executor.shutdown();
			}
		}
		// create new ones
		//this.executors = new EventRouterExecutor[configuration.getEventRouterThreads()];
		//for (int i = 0; i < configuration.getEventRouterThreads(); i++) {
		this.executors = new EventRouterExecutor[50];
		for (int i = 0; i < 50; i++) {
			this.executors[i] = new EventRouterExecutorImpl(configuration.isCollectStats(),sleeContainer);
		}			
		// create mapper
		try {
			Class<?> executorMapperClass = Class.forName(configuration.getExecutorMapperClassName());
			executorMapper = (EventRouterExecutorMapper) executorMapperClass.newInstance();
			executorMapper.setExecutors(this,executors);
		} catch (Throwable e) {
			throw new IllegalStateException("Unable to create event router executor mapper class instance",e);
		}		
		// create stats
		statistics = new EventRouterStatisticsImpl(this);
	}
	
	@Override
	public String toString() {
		return "EventRouter: "
		+ "\n+-- Executors: " + executors.length;
	}
	
	
	
	/* (non-Javadoc)
	 * @see org.mobicents.slee.runtime.eventrouter.EventRouter#getEventRouterStatistics()
	 */
	public EventRouterStatistics getEventRouterStatistics() {
		return statistics;
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.slee.runtime.eventrouter.EventRouter#getExecutors()
	 */
	public EventRouterExecutor[] getExecutors() {
		return executors;
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.slee.runtime.eventrouter.EventRouter#getEventRouterExecutorMapper()
	 */
	public EventRouterExecutorMapper getEventRouterExecutorMapper() {
		return executorMapper;
	}

	public EventRouterConfiguration getConfiguration() {
		return configuration;
	}
	
	public void resize(){
		// TODO Auto-generated method stub
			int len = this.executors.length;
			EventRouterExecutor[] newExecutors = new EventRouterExecutor[len*2];
			for(int i=0;i<len;i++){
				newExecutors[i] = this.executors[i];
			}
			for(int i=len;i<len*2;i++){
				newExecutors[i] = new EventRouterExecutorImpl(configuration.isCollectStats(),sleeContainer);
			}
			this.executors = newExecutors;
			executorMapper.setExecutors(this,executors);
			System.out.println("len :"+len*2);
		}
}