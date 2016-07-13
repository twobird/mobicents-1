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
 * Start time:08:53:04 2009-04-16<br>
 * Project: mobicents-jainslee-server-core<br>
 * 
 * @author <a href="mailto:baranowb@gmail.com">baranowb - Bartosz Baranowski
 *         </a>
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 */
package org.mobicents.slee.container.component.management.jmx;

import org.jboss.system.ServiceMBean;

/**
 * Start time:08:53:04 2009-04-16<br>
 * Project: mobicents-jainslee-server-core<br>
 * 
 * 
 * Simple MBean to allow access to some info so we know whats going on inside and provide means of polite instalation of policy.
 * @author <a href="mailto:baranowb@gmail.com">baranowb - Bartosz Baranowski
 *         </a>
 */
public interface PolicyMBeanImplMBean extends ServiceMBean {

	/**
	 * Returns String form of urls that point files loaded
	 * @return
	 */
	public String getPolicyFilesURL();
	/**
	 * Return Arrays.toString form of code source URLs loaded into policy
	 * @return
	 */
	public String getCodeSources();
	
	public boolean isUseMPolicy();
	
	public void setUseMPolicy(boolean useMPolicy);
	
}
