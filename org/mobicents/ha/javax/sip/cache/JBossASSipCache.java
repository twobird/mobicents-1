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
import gov.nist.javax.sip.stack.SIPDialog;
import gov.nist.javax.sip.stack.SIPServerTransaction;

import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javax.management.ObjectName;
import javax.transaction.TransactionManager;

import org.jboss.cache.CacheException;
import org.jboss.cache.Fqn;
import org.jboss.cache.Node;
import org.jboss.cache.aop.PojoCacheMBean;
import org.jboss.mx.util.MBeanProxyExt;
import org.mobicents.ha.javax.sip.ClusteredSipStack;
import org.mobicents.ha.javax.sip.HASipDialog;
import org.mobicents.ha.javax.sip.SipStackImpl;

/**
 * Implementation of the SipCache interface, backed by a JBoss Cache 1.4.1 Cache that is retrieved from a running JBoss AS 4.2.3 in all profile.
 * No configuration is needed for the cache as it is using the one from the AS itself since we don't create the cache, JBoss AS does
 * 
 * @author jean.deruelle@gmail.com
 *
 */
public class JBossASSipCache extends AbstractJBossSipCache implements SipCache {
	protected TransactionManager transactionManager;
	protected PojoCacheMBean pojoCache;
	private boolean isLocal = false;

	/* (non-Javadoc)
	 * @see org.mobicents.ha.javax.sip.cache.SipCache#getDialog(java.lang.String)
	 */
	public SIPDialog getDialog(String dialogId) throws SipCacheException {		
		try {
			Node node = pojoCache.get(SipStackImpl.DIALOG_ROOT + dialogId + CACHE_SEPARATOR + METADATA);
			if(node != null) {
				final Map<String, Object> dialogMetaData = node.getData();
				final Object dialogAppData = pojoCache.get(SipStackImpl.DIALOG_ROOT + dialogId, APPDATA);
				
				return super.createDialog(dialogId, dialogMetaData, dialogAppData);
			} else {
				if(clusteredSipStack.getStackLogger().isLoggingEnabled(StackLogger.TRACE_DEBUG)) {
					clusteredSipStack.getStackLogger().logDebug("key " + SipStackImpl.DIALOG_ROOT + dialogId + CACHE_SEPARATOR + METADATA + " not found in cache.");
				}
				return null;
			}
		} catch (CacheException e) {
			throw new SipCacheException("A problem occured while retrieving the following dialog " + dialogId + " from the TreeCache", e);
		} 
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.ha.javax.sip.cache.SipCache#updateDialog(gov.nist.javax.sip.stack.SIPDialog)
	 */
	public void updateDialog(SIPDialog sipDialog) throws SipCacheException {
		final String dialogId = sipDialog.getDialogId();
		try {			
			Node node = pojoCache.get(SipStackImpl.DIALOG_ROOT + dialogId + CACHE_SEPARATOR + METADATA);
			if(node != null) {
				final Map<String, Object> dialogMetaData = node.getData();
				final Object dialogAppData = pojoCache.get(SipStackImpl.DIALOG_ROOT + dialogId, APPDATA);
				final HASipDialog haSipDialog = (HASipDialog) sipDialog;
				super.updateDialog(haSipDialog, dialogMetaData, dialogAppData);
			} else {
				if(clusteredSipStack.getStackLogger().isLoggingEnabled(StackLogger.TRACE_DEBUG)) {
					clusteredSipStack.getStackLogger().logDebug("key " + SipStackImpl.DIALOG_ROOT + dialogId + CACHE_SEPARATOR + METADATA + " not found in cache.");
				}
			}
		} catch (CacheException e) {
			throw new SipCacheException("A problem occured while retrieving the following dialog " + dialogId + " from the TreeCache", e);
		}
	}

	/* (non-Javadoc)
	 * @see org.mobicents.ha.javax.sip.cache.SipCache#putDialog(gov.nist.javax.sip.stack.SIPDialog)
	 */
	public void putDialog(SIPDialog dialog) throws SipCacheException {
//		Transaction tx = null;
		try {
			// no need to initiate a tx for a single put
//			tx = transactionManager.getTransaction();
//			if(tx == null) {
//				transactionManager.begin();
//				tx = transactionManager.getTransaction();				
//			}
			if(clusteredSipStack.getStackLogger().isLoggingEnabled(StackLogger.TRACE_DEBUG)) {
				clusteredSipStack.getStackLogger().logStackTrace();
			}
			final HASipDialog haSipDialog = (HASipDialog) dialog;
			final String dialogId = haSipDialog.getDialogIdToReplicate();
			if(clusteredSipStack.getStackLogger().isLoggingEnabled(StackLogger.TRACE_DEBUG)) {
				clusteredSipStack.getStackLogger().logDebug("put HA SIP Dialog " + dialog + " with dialogId " + dialogId + " in the cache");				
			}
			final Map<String, Object> dialogMetaData = haSipDialog.getMetaDataToReplicate();
			if(dialogMetaData != null) {
				for (Entry<String, Object> metaData : dialogMetaData.entrySet()) {
					pojoCache.put(SipStackImpl.DIALOG_ROOT + dialogId + CACHE_SEPARATOR + METADATA, metaData.getKey(), metaData.getValue());
				}								
			}
			final Object dialogAppData = haSipDialog.getApplicationDataToReplicate();
			if(dialogAppData != null) {
				pojoCache.put(SipStackImpl.DIALOG_ROOT + dialogId, APPDATA, dialogAppData);
			}
//			if(tx != null) {
//				tx.commit();
//			}
		} catch (Exception e) {
//			if(tx != null) {
//				try { tx.rollback(); } catch(Throwable t) {}
//			}
			throw new SipCacheException("A problem occured while putting the following dialog " + dialog.getDialogId() + "  into the TreeCache", e);
		} 
	}

	/* (non-Javadoc)
	 * @see org.mobicents.ha.javax.sip.cache.SipCache#removeDialog(java.lang.String)
	 */
	public void removeDialog(String dialogId) throws SipCacheException {
//		Transaction tx = null;
		try {
			// no need to initiate a tx for a single put
//			tx = transactionManager.getTransaction();
//			tx = transactionManager.getTransaction();
//			if(tx == null) {
//				transactionManager.begin();
//				tx = transactionManager.getTransaction();				
//			}
			pojoCache.remove(SipStackImpl.DIALOG_ROOT + dialogId);
//			if(tx != null) {
//				tx.commit();
//			}
		} catch (Exception e) {
//			if(tx != null) {
//				try { tx.rollback(); } catch(Throwable t) {}
//			}
			throw new SipCacheException("A problem occured while removing the following dialog " + dialogId + " from the TreeCache", e);
		}
	}

	/* (non-Javadoc)
	 * @see org.mobicents.ha.javax.sip.cache.SipCache#setClusteredSipStack(org.mobicents.ha.javax.sip.ClusteredSipStack)
	 */
	public void setClusteredSipStack(ClusteredSipStack clusteredSipStack) {
		this.clusteredSipStack  = clusteredSipStack;
	}

	/* (non-Javadoc)
	 * @see org.mobicents.ha.javax.sip.cache.SipCache#setConfigurationProperties(java.util.Properties)
	 */
	public void setConfigurationProperties(Properties configurationProperties) {
		this.configProperties = configurationProperties;
	}

	/**
	 * Creates a JMX proxy PojoCacheMBean for the given object name.
	 * 
	 * @param objectName
	 *            the object name
	 * @return the proxy
	 * @throws ClusteringNotSupportedException
	 *             if there is a problem
	 */
	private PojoCacheMBean getPojoCacheMBean(String objectName)
			throws SipCacheException {
		try {
			ObjectName cacheServiceName = new ObjectName(objectName);
			// Create Proxy-Object for this service
			return (PojoCacheMBean) MBeanProxyExt.create(PojoCacheMBean.class,
					cacheServiceName);
		} catch (Throwable t) {
			String str = "Could not access JBoss AS TreeCache service "
					+ (objectName == null ? "<null>" : objectName)
					+ " for SIP Dialog clustering";
			clusteredSipStack.getStackLogger().logError(str);
			throw new SipCacheException(str, t);
		}
	}
	
	public void init() throws SipCacheException {
		try {
			pojoCache = getPojoCacheMBean("jboss.cache:service=TomcatClusteringCache");
			cacheListener = new JBossJainSipCacheListener(clusteredSipStack);
			pojoCache.addTreeCacheListener(cacheListener);
		} catch (SipCacheException e) {
			clusteredSipStack.getStackLogger().logError("Could not initialize the cache, defaulting to local mode");
			isLocal = true;
		}
//		try {
//			pojoCache.createService();
//		} catch (Exception e) {
//			throw new SipCacheException("Unexpected exception while creating the pojo cache", e);
//		}		
	}

	public void start() throws SipCacheException {
		if(!isLocal) {
			try {
	//			pojoCache.start();
				transactionManager = pojoCache.getTransactionManager();
			} catch (Exception e) {
				throw new SipCacheException("Couldn't start the TreeCache", e);
			}		
			if (clusteredSipStack.getStackLogger().isLoggingEnabled(StackLogger.TRACE_INFO)) {
				clusteredSipStack.getStackLogger().logInfo(
						"Mobicents JAIN SIP Tree Cache started, state: " + pojoCache.getStateString() + 
						", Mode: " + pojoCache.getCacheMode());
			}
		}
	}

	public void stop() throws SipCacheException {
		if(!isLocal) {
	//		pojoCache.stopService();
			if (clusteredSipStack.getStackLogger().isLoggingEnabled(StackLogger.TRACE_INFO)) {
				clusteredSipStack.getStackLogger().logInfo(
						"Mobicents JAIN SIP Tree Cache stopped, state: " + pojoCache.getStateString() + 
						", Mode: " + pojoCache.getCacheMode());
			}
	//		pojoCache.destroyService();
		}
	}

	public boolean inLocalMode() {		
		return isLocal;
	}
}
