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

package org.mobicents.slee.container.rmi;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.jboss.util.naming.Util;
import org.mobicents.slee.connector.remote.RemoteSleeConnectionService;
import org.mobicents.slee.connector.remote.RemoteSleeConnectionServiceImpl;
import org.mobicents.slee.container.AbstractSleeContainerModule;

public class RmiServerInterfaceImpl extends AbstractSleeContainerModule implements
		RmiServerInterface {

	private final static Logger logger = Logger
			.getLogger(RmiServerInterfaceImpl.class);

	private RMIServerImpl rmiServer;
	
	private String jndiName;

	@Override
	public void sleeInitialization() {
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("Starting Slee Service RMI Server");
			}

			InitialContext ctx = new InitialContext();

			RemoteSleeConnectionService stub = new RemoteSleeConnectionServiceImpl(super.sleeContainer.getSleeConnectionService(),super.sleeContainer.getComponentRepository());
			rmiServer =  new RMIServerImpl(this.jndiName,RemoteSleeConnectionService.class,stub);
			
			stub = (RemoteSleeConnectionService) rmiServer.createStub();
			
			ctx.rebind(this.jndiName, stub);
					 

			if (logger.isDebugEnabled()) {
				logger.debug("Bound SleeService rmi stub in jndi");
			}
		} catch (Exception e) {
			logger.error(
					"Failed to start RMI server for Remote slee service", e);
		}

	}

	@Override
	public void sleeShutdown() {
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("Stopping RMI Server for slee service");
			}
			InitialContext ctx = new InitialContext();
			Util.unbind(ctx, this.jndiName);
			rmiServer.destroy();
		} catch (NamingException e) {
			logger.error(
					"Failed to stop RMI Server for remote slee service", e);
		}
	}

	/* (non-Javadoc)
	* @see org.mobicents.slee.container.rmi.RmiServerInterface#getJNDIName()
	*/
	public String getJndiName() {
		return this.jndiName;
	}
	
	/* (non-Javadoc)
	* @see org.mobicents.slee.container.rmi.RmiServerInterface#setJNDIName(java.lang.String)
	*/
	public void setJndiName(String name) {
		this.jndiName = name;
	}
}
