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
 * 
 */
package org.mobicents.slee.runtime.eventrouter.mapping;

import org.mobicents.slee.container.activity.ActivityContextHandle;
import org.mobicents.slee.container.eventrouter.EventRouterExecutor;
import org.mobicents.slee.container.eventrouter.EventRouterExecutorMapper;
import org.mobicents.slee.runtime.eventrouter.EventRouterImpl;

/**
 * 
 * @author martins
 * 
 */
public abstract class AbstractEventRouterExecutorMapper implements
		EventRouterExecutorMapper {

	protected EventRouterExecutor[] executors;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.slee.runtime.eventrouter.mapping.EventRouterExecutorMapper
	 * #getExecutor(org.mobicents.slee.runtime.activity.ActivityContextHandle)
	 */

	public abstract EventRouterExecutor getExecutor(
			ActivityContextHandle activityContextHandle);
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.slee.runtime.eventrouter.mapping.EventRouterExecutorMapper
	 * #setExecutors
	 * (org.mobicents.slee.runtime.eventrouter.EventRouterExecutor[])
	 */
	public void setExecutors(EventRouterImpl eventrouterimpl,EventRouterExecutor[] executors) {
		this.executors = executors;
		//System.out.println("setExecutors");
	}
/*	public void setExecutors(EventRouterExecutor[] executors) {
		this.executors = executors;
		System.out.println("setExecutors");
	}*/
	
}