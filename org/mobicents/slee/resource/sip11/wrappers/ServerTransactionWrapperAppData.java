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

package org.mobicents.slee.resource.sip11.wrappers;

import gov.nist.javax.sip.stack.SIPServerTransaction;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.sip.Transaction;

import org.mobicents.slee.resource.sip11.SipResourceAdaptor;

public class ServerTransactionWrapperAppData implements TransactionWrapperAppData, Externalizable {

	private ServerTransactionWrapper transactionWrapper;
	
	public ServerTransactionWrapperAppData(ServerTransactionWrapper transactionWrapper) {
		this.transactionWrapper = transactionWrapper;
	}
	
	public ServerTransactionWrapperAppData() {
		
	}
	
	
	@Override
	public TransactionWrapper getTransactionWrapper(Transaction t,
			SipResourceAdaptor ra) {
		if (transactionWrapper == null) {
			transactionWrapper = new ServerTransactionWrapper((SIPServerTransaction) t, ra);
		}
		return transactionWrapper;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		// nothing
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		// nothing
	}

}
