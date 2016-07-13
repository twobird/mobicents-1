/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.mobicents.ha.javax.sip.cache;

import gov.nist.core.StackLogger;

import org.jboss.cache.Fqn;
import org.jboss.cache.TreeCache;
import org.jboss.cache.TreeCacheListener;
import org.jgroups.View;
import org.mobicents.ha.javax.sip.ClusteredSipStack;
import org.mobicents.ha.javax.sip.SipStackImpl;

/**
 * Listener on the cache to be notified and update the local stack accordingly
 * 
 * @author jean.deruelle@gmail.com
 *
 */
public class JBossJainSipCacheListener implements TreeCacheListener {

	private ClusteredSipStack clusteredSipStack;

	/**
	 * @param clusteredSipStack 
	 * 
	 */
	public JBossJainSipCacheListener(ClusteredSipStack clusteredSipStack) {
		this.clusteredSipStack = clusteredSipStack;
	}

	/* (non-Javadoc)
	 * @see org.jboss.cache.TreeCacheListener#cacheStarted(org.jboss.cache.TreeCache)
	 */
	public void cacheStarted(TreeCache cache) {
		if (clusteredSipStack.getStackLogger().isLoggingEnabled(StackLogger.TRACE_DEBUG)) {
			clusteredSipStack.getStackLogger().logDebug(
					"Mobicents JAIN SIP Cache started, state: " + cache.getStateString() + 
					", Mode: " + cache.getCacheMode());
		}
	}

	/* (non-Javadoc)
	 * @see org.jboss.cache.TreeCacheListener#cacheStopped(org.jboss.cache.TreeCache)
	 */
	public void cacheStopped(TreeCache cache) {
		if (clusteredSipStack.getStackLogger().isLoggingEnabled(StackLogger.TRACE_DEBUG)) {
			clusteredSipStack.getStackLogger().logDebug(
					"Mobicents JAIN SIP Cache stopped, state: " + cache.getStateString() + 
					", Mode: " + cache.getCacheMode());
		}
	}

	/* (non-Javadoc)
	 * @see org.jboss.cache.TreeCacheListener#nodeCreated(org.jboss.cache.Fqn)
	 */
	public void nodeCreated(Fqn fqn) {
		if (clusteredSipStack.getStackLogger().isLoggingEnabled(StackLogger.TRACE_DEBUG) && fqn.toString().indexOf(SipStackImpl.DIALOG_ROOT) != -1) {
			clusteredSipStack.getStackLogger().logDebug("sipStack " + clusteredSipStack + 
					" Node created : " + fqn);
		}
	}

	/* (non-Javadoc)
	 * @see org.jboss.cache.TreeCacheListener#nodeEvicted(org.jboss.cache.Fqn)
	 */
	public void nodeEvicted(Fqn fqn) {
		if (clusteredSipStack.getStackLogger().isLoggingEnabled(StackLogger.TRACE_DEBUG) && fqn.toString().indexOf(SipStackImpl.DIALOG_ROOT) != -1) {
			clusteredSipStack.getStackLogger().logDebug("sipStack " + clusteredSipStack + 
					" Node evicted : " + fqn);
		}
	}

	/* (non-Javadoc)
	 * @see org.jboss.cache.TreeCacheListener#nodeLoaded(org.jboss.cache.Fqn)
	 */
	public void nodeLoaded(Fqn fqn) {
		if (clusteredSipStack.getStackLogger().isLoggingEnabled(StackLogger.TRACE_DEBUG) && fqn.toString().indexOf(SipStackImpl.DIALOG_ROOT) != -1) {
			clusteredSipStack.getStackLogger().logDebug("sipStack " + clusteredSipStack + 
					" Node loaded : " + fqn);
		}
	}

	/* (non-Javadoc)
	 * @see org.jboss.cache.TreeCacheListener#nodeModified(org.jboss.cache.Fqn)
	 */
	public void nodeModified(Fqn fqn) {
		if (clusteredSipStack.getStackLogger().isLoggingEnabled(StackLogger.TRACE_DEBUG) && fqn.toString().indexOf(SipStackImpl.DIALOG_ROOT) != -1) {
			clusteredSipStack.getStackLogger().logDebug("sipStack " + clusteredSipStack + 
					" Node modified : " + fqn);
		}
		
	}

	/* (non-Javadoc)
	 * @see org.jboss.cache.TreeCacheListener#nodeRemoved(org.jboss.cache.Fqn)
	 */
	public void nodeRemoved(Fqn fqn) {
		if (clusteredSipStack.getStackLogger().isLoggingEnabled(StackLogger.TRACE_DEBUG) && fqn.toString().indexOf(SipStackImpl.DIALOG_ROOT) != -1) {
			clusteredSipStack.getStackLogger().logDebug("sipStack " + clusteredSipStack + 
					" Node removed : " + fqn);
		}
		// Fix for Issue 1418 jain sip ha does not removes dialog in local stack
		// http://code.google.com/p/mobicents/issues/detail?id=1418
		if(fqn.size() > 1) {			
			clusteredSipStack.remoteDialogRemoval(fqn.get(1).toString());
		}
	}

	/* (non-Javadoc)
	 * @see org.jboss.cache.TreeCacheListener#nodeVisited(org.jboss.cache.Fqn)
	 */
	public void nodeVisited(Fqn fqn) {
//		if (clusteredSipStack.getStackLogger().isLoggingEnabled(StackLogger.TRACE_DEBUG) && fqn.toString().indexOf(SipStackImpl.DIALOG_ROOT) != -1) {
//			clusteredSipStack.getStackLogger().logDebug("sipStack " + clusteredSipStack + 
//					" Node visited : " + fqn);
//		}
	}

	/* (non-Javadoc)
	 * @see org.jboss.cache.TreeCacheListener#viewChange(org.jgroups.View)
	 */
	public void viewChange(View view) {
		if (clusteredSipStack.getStackLogger().isLoggingEnabled(StackLogger.TRACE_DEBUG)) {
			clusteredSipStack.getStackLogger().logDebug("sipStack " + clusteredSipStack + 
					" View changed : " + view.getVid());
		}
	}

}
