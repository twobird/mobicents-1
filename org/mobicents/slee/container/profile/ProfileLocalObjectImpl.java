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

package org.mobicents.slee.container.profile;

import java.security.PrivilegedActionException;

import javax.slee.NoSuchObjectLocalException;
import javax.slee.SLEEException;
import javax.slee.TransactionRequiredLocalException;
import javax.slee.TransactionRolledbackLocalException;
import javax.slee.profile.ProfileTable;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

import org.apache.log4j.Logger;
import org.mobicents.slee.container.SleeContainer;
import org.mobicents.slee.container.profile.entity.ProfileEntity;

/**
 * Start time:14:20:46 2009-03-14<br>
 * Project: mobicents-jainslee-server-core<br>
 * This class implements profile local interface to provide all required
 * methods. It obtains on the fly ProfileObject to perform its tasks.
 * 
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 * @author martins
 */
public class ProfileLocalObjectImpl implements ProfileLocalObject {

	protected static final Logger logger = Logger.getLogger(ProfileLocalObjectImpl.class);
	
	protected static final SleeContainer sleeContainer = SleeContainer.lookupFromJndi();
	
	/**
	 * the profile object to be used
	 */
	protected final ProfileObjectImpl profileObject;
	
	/**
	 * the transaction that defines if the profile object is tsill valid 
	 */
	protected final Transaction transaction;
	
	/**
	 * the name of the profile related with this object
	 */
	private final String profileName;
	
	public ProfileLocalObjectImpl(ProfileObjectImpl profileObject) {
		this.profileObject = profileObject;
		try {
			this.transaction = sleeContainer.getTransactionManager().getTransaction();
			this.profileName = profileObject.getProfileEntity().getProfileName();
		} catch (Throwable e) {
			throw new SLEEException(e.getMessage(),e);
		}		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.slee.profile.ProfileLocalObject#getProfileName()
	 */
	public String getProfileName() throws SLEEException {
		return profileName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.slee.profile.ProfileLocalObject#getProfileTable()
	 */
	public ProfileTable getProfileTable() throws SLEEException {
		return profileObject.getProfileTable();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.slee.profile.ProfileLocalObject#getProfileTableName()
	 */
	public String getProfileTableName() throws SLEEException {
		return profileObject.getProfileTable().getProfileTableName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.slee.profile.ProfileLocalObject#isIdentical(javax.slee.profile.
	 * ProfileLocalObject)
	 */
	public boolean isIdentical(javax.slee.profile.ProfileLocalObject other) throws SLEEException {
		
		if (!(other instanceof ProfileLocalObjectImpl)) {
			return false;
		}
		
		return this._equals(other);
	}
	
	@Override
	public int hashCode() {
		return getProfileTable().hashCode() * 31 + ((profileName == null) ? 0 : profileName.hashCode());
	}	
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ProfileLocalObjectImpl) {
			ProfileLocalObjectImpl other = (ProfileLocalObjectImpl) obj;
			return this._equals(other);
		}
		else {
			return false;
		}
	}
	
	/**
	 * 
	 * @param other
	 * @return
	 */
	private boolean _equals(javax.slee.profile.ProfileLocalObject other) {
		
		if (!this.getProfileTableName().equals(other.getProfileTableName())) {
			return false;
		}
		
		if (this.getProfileName() == null) {
			if (other.getProfileName() == null) {
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return this.getProfileName().equals(other.getProfileName());
		}
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.slee.profile.ProfileLocalObject#remove()
	 */
	public void remove() throws TransactionRequiredLocalException, TransactionRolledbackLocalException, SLEEException {

		if (logger.isDebugEnabled()) {
			logger.debug("removing profile with name " + getProfileName() + " from table with name " + getProfileTableName());
		}

		sleeContainer.getTransactionManager().mandateTransaction();

		try {
			ProfileEntity profileEntity = profileObject.getProfileEntity();
			if (profileEntity != null) {
				// confirm it is still the same tx
				checkTransaction();
				// remove
				profileEntity.remove();
			}
			else {
				// there is no profile assigned to the object
				if(getProfileTable().find(getProfileName()) == null) {
					// this exception has priority
					throw new NoSuchObjectLocalException("the profile with name "+getProfileName()+" was not found on table with name "+getProfileTableName());
				}
				else {
					throw new IllegalStateException("the profile object is no longer valid");
				}
			}								
		}
		catch (RuntimeException e) {
			try {
				profileObject.invalidateObject();
				sleeContainer.getTransactionManager().setRollbackOnly();
			} catch (SystemException e1) { 
				throw new SLEEException(e1.getMessage(),e1); 
			};
			throw new TransactionRolledbackLocalException(e.getMessage(),e);
		}
	}
	
	/**
	 * Verifies that the current transaction is still the one used to create the object
	 * @throws IllegalStateException
	 */
	protected void checkTransaction() throws IllegalStateException {
		try {
			if (!sleeContainer.getTransactionManager().getTransaction().equals(this.transaction)) {
				throw new IllegalStateException();
			}
		} catch (SystemException e) {
			throw new IllegalStateException();
		}		
	}

	/**
	 * Process a {@link RuntimeException} occurred when invoking a method in the custom local object interface.
	 * @param e
	 * @throws SLEEException
	 * @throws TransactionRolledbackLocalException
	 */
	protected void processRuntimeException(RuntimeException e) throws SLEEException, TransactionRolledbackLocalException {
		try {
			profileObject.invalidateObject();
			sleeContainer.getTransactionManager().setRollbackOnly();
		} catch (SystemException e1) {
			throw new SLEEException(e1.getMessage(),e1); 
		}
		throw new TransactionRolledbackLocalException(e.getMessage(),e);
	}
	
	/**
	 * Process a {@link PrivilegedActionException} occurred when invoking a method in the custom local object interface.
	 * @param e
	 * @throws Exception
	 * @throws TransactionRolledbackLocalException
	 */
	protected void processPrivilegedActionException(PrivilegedActionException e) throws Exception, TransactionRolledbackLocalException {
		if (e.getCause() instanceof RuntimeException) {
			processRuntimeException((RuntimeException) e.getCause());
		}
		else if (e.getCause() instanceof Exception) {
			throw (Exception) e.getCause();
		}
		else {
			throw new SLEEException("unexpected type of cause",e.getCause());
		}
	}

	
}
