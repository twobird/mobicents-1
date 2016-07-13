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

package org.mobicents.timers;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.apache.log4j.Logger;
import org.jboss.cache.Fqn;
import org.jgroups.Address;
import org.mobicents.cluster.DataRemovalListener;
import org.mobicents.cluster.FailOverListener;
import org.mobicents.cluster.MobicentsCluster;
import org.mobicents.cluster.cache.ClusteredCacheData;
import org.mobicents.cluster.election.ClientLocalListenerElector;
import org.mobicents.timers.cache.FaultTolerantSchedulerCacheData;
import org.mobicents.timers.cache.TimerTaskCacheData;

/**
 * 
 * @author martins
 *
 */
public class FaultTolerantScheduler {

	private static final Logger logger = Logger.getLogger(FaultTolerantScheduler.class);
	
	/**
	 * the executor of timer tasks
	 */
	private final ScheduledThreadPoolExecutor executor;
	
	/**
	 * the jta tx manager
	 */
	private final TransactionManager txManager;
	
	/**
	 * the local running tasks. NOTE: never ever check for values, class instances may differ due cache replication, ALWAYS use keys.
	 */
	private final ConcurrentHashMap<Serializable, TimerTask> localRunningTasks = new ConcurrentHashMap<Serializable, TimerTask>();
	
	/**
	 * the timer task factory associated with this scheduler
	 */
	private TimerTaskFactory timerTaskFactory;
	
	/**
	 * the base fqn used to store tasks data in mobicents cluster's cache
	 */
	@SuppressWarnings("unchecked")
	private final Fqn baseFqn;
	
	private FaultTolerantSchedulerCacheData cacheData;
	
	/**
	 * the scheduler name
	 */
	private final String name;
		
	/**
	 * the mobicents cluster 
	 */
	private final MobicentsCluster cluster;
	
	/**
	 * listener for fail over events in mobicents cluster
	 */
	private final ClientLocalListener clusterClientLocalListener;
	
	/**
	 * 
	 * @param name
	 * @param corePoolSize
	 * @param cluster
	 * @param priority
	 * @param txManager
	 * @param timerTaskFactory
	 */
	public FaultTolerantScheduler(String name, int corePoolSize, MobicentsCluster cluster, byte priority, TransactionManager txManager,TimerTaskFactory timerTaskFactory) {
		this(name, corePoolSize, cluster, priority, txManager, timerTaskFactory, 0);
	}
	
	/**
	 * 
	 * @param name
	 * @param corePoolSize
	 * @param cluster
	 * @param priority
	 * @param txManager
	 * @param timerTaskFactory
	 * @param purgePeriod
	 */
	public FaultTolerantScheduler(String name, int corePoolSize, MobicentsCluster cluster, byte priority, TransactionManager txManager,TimerTaskFactory timerTaskFactory, int purgePeriod) {
		this.name = name;
		this.executor = new ScheduledThreadPoolExecutor(corePoolSize);
		if(purgePeriod > 0) {
			Runnable r = new Runnable() {			
				@Override
				public void run() {
					try {
						executor.purge();				
					}
					catch (Exception e) {
						logger.error("failed to execute purge",e);
					}
				}
			};
			this.executor.scheduleWithFixedDelay(r, purgePeriod, purgePeriod, TimeUnit.MINUTES);
		}
		this.baseFqn = Fqn.fromElements(name);
		this.cluster = cluster;		
		this.timerTaskFactory = timerTaskFactory;
		this.txManager = txManager;		
		cacheData = new FaultTolerantSchedulerCacheData(baseFqn,cluster);
		if (cluster.isStarted()) {
			cacheData.create();
		}
		clusterClientLocalListener = new ClientLocalListener(priority);
		cluster.addFailOverListener(clusterClientLocalListener);
		cluster.addDataRemovalListener(clusterClientLocalListener);
	}

	/**
	 * Retrieves the {@link TimerTaskData} associated with the specified taskID. 
	 * @param taskID
	 * @return null if there is no such timer task data
	 */
	public TimerTaskData getTimerTaskData(Serializable taskID) {
		TimerTaskCacheData timerTaskCacheData = new TimerTaskCacheData(taskID, baseFqn, cluster);
		if (timerTaskCacheData.exists()) {
			return timerTaskCacheData.getTaskData();
		}
		else {
			return null;
		}
	}
	
	/**
	 * Retrieves the executor of timer tasks.
	 * @return
	 */
	ScheduledThreadPoolExecutor getExecutor() {
		return executor;
	}
	
	/**
	 * Retrieves local running tasks map.
	 * @return
	 */
	ConcurrentHashMap<Serializable, TimerTask> getLocalRunningTasksMap() {
		return localRunningTasks;
	}
	
	/**
	 * Retrieves a set containing all local running tasks. Removals on the set
	 * will not be propagated to the internal state of the scheduler.
	 * 
	 * @return
	 */
	public Set<TimerTask> getLocalRunningTasks() {
		return new HashSet<TimerTask>(localRunningTasks.values());
	}
	
	/**
	 * Retrieves a local running task by its id
	 * 
	 * @return the local task if found, null otherwise
	 */
	public TimerTask getLocalRunningTask(Serializable taskId) {
		return localRunningTasks.get(taskId);
	}
	
	/**
	 *  Retrieves the scheduler name.
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 *  Retrieves the priority of the scheduler as a client local listener of the mobicents cluster.
	 * @return the priority
	 */
	public byte getPriority() {
		return clusterClientLocalListener.getPriority();
	}
	
	/**
	 * Retrieves the jta tx manager.
	 * @return
	 */
	public TransactionManager getTransactionManager() {
		return txManager;
	}

	/**
	 * Retrieves the timer task factory associated with this scheduler.
	 * @return
	 */
	public TimerTaskFactory getTimerTaskFactory() {
		return timerTaskFactory;
	}
	
	// logic 
	
	public void schedule(TimerTask task) {
		schedule(task, true);
	}
	/**
	 * Schedules the specified task.
	 * 
	 * @param task
	 */
	public void schedule(TimerTask task, boolean checkIfAlreadyPresent) {
		
		final TimerTaskData taskData = task.getData(); 
		final Serializable taskID = taskData.getTaskID();
		task.setScheduler(this);
		
		if (logger.isDebugEnabled()) {
			logger.debug("Scheduling task with id " + taskID);
		}
		
		// store the task and data
		final TimerTaskCacheData timerTaskCacheData = new TimerTaskCacheData(taskID, baseFqn, cluster);
		if (timerTaskCacheData.create()) {
			timerTaskCacheData.setTaskData(taskData);
		} else if(checkIfAlreadyPresent) {
            throw new IllegalStateException("timer task " + taskID + " already scheduled");
		}
				
		// schedule task
		final SetTimerAfterTxCommitRunnable setTimerAction = new SetTimerAfterTxCommitRunnable(task, this);
		if (txManager != null) {
			try {
				Transaction tx = txManager.getTransaction();
				if (tx != null) {
					TransactionContext txContext = TransactionContextThreadLocal.getTransactionContext();
					if (txContext == null) {
						txContext = new TransactionContext();
						tx.registerSynchronization(new TransactionSynchronization(txContext));
					}
					txContext.put(taskID, setTimerAction);					
					task.setSetTimerTransactionalAction(setTimerAction);
				}
				else {
					setTimerAction.run();
				}
			}
			catch (Throwable e) {
				remove(taskID,true);
				throw new RuntimeException("Unable to register tx synchronization object",e);
			}
		}
		else {
			setTimerAction.run();
		}		
	}

	/**
	 * Cancels a local running task with the specified ID.
	 * 
	 * @param taskID
	 * @return the task canceled
	 */
	public TimerTask cancel(Serializable taskID) {
		
		if (logger.isDebugEnabled()) {
			logger.debug("Canceling task with timer id "+taskID);
		}
		
		TimerTask task = localRunningTasks.get(taskID);
		if (task != null) {
			// remove task data
			new TimerTaskCacheData(taskID, baseFqn, cluster).remove();

			final SetTimerAfterTxCommitRunnable setAction = task.getSetTimerTransactionalAction();
			if (setAction != null) {
				// we have a tx action scheduled to run when tx commits, to set the timer, lets simply cancel it
				setAction.cancel();
			}
			else {
				// do cancellation
				AfterTxCommitRunnable runnable = new CancelTimerAfterTxCommitRunnable(task,this);
				if (txManager != null) {
					try {
						Transaction tx = txManager.getTransaction();
						if (tx != null) {
							TransactionContext txContext = TransactionContextThreadLocal.getTransactionContext();
							if (txContext == null) {
								txContext = new TransactionContext();
								tx.registerSynchronization(new TransactionSynchronization(txContext));
							}
							txContext.put(taskID, runnable);					
						}
						else {
							runnable.run();
						}
					}
					catch (Throwable e) {
						throw new RuntimeException("Unable to register tx synchronization object",e);
					}
				}
				else {
					runnable.run();
				}			
			}		
		}
		else {
			// not found locally
			// if there is a tx context there may be a set timer action there
			if (txManager != null) {
				try {
					Transaction tx = txManager.getTransaction();
					if (tx != null) {
						TransactionContext txContext = TransactionContextThreadLocal.getTransactionContext();
						if (txContext != null) {
							final AfterTxCommitRunnable r = txContext.remove(taskID);
							if (r != null) {
								task = r.task;
								// remove from cluster
								new TimerTaskCacheData(taskID, baseFqn, cluster).remove();
							}							
						}											
					}
				}
				catch (Throwable e) {
					throw new RuntimeException("Failed to check tx context.",e);
				}
			}			
		}
		
		return task;
	}
	
	void remove(Serializable taskID,boolean removeFromCache) {
		if(logger.isDebugEnabled())
		{
			logger.debug("remove() : "+taskID+" - "+removeFromCache);
		}
		
		localRunningTasks.remove(taskID);
		if(removeFromCache)
			new TimerTaskCacheData(taskID, baseFqn, cluster).remove();
	}
	
	/**
	 * Recovers a timer task that was running in another node.
	 * 
	 * @param taskData
	 */
	private void recover(TimerTaskData taskData) {
		TimerTask task = timerTaskFactory.newTimerTask(taskData);
		if(task != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Recovering task with id "+taskData.getTaskID());
			}
			task.beforeRecover();
			// on recovery the task will already be in the cache so we don't check for it
			// or an IllegalStateException will be thrown
			schedule(task, false);
		}
	}

	public void shutdownNow() {
		if (logger.isDebugEnabled()) {
			logger.debug("Shutdown now.");
		}
		cluster.removeFailOverListener(clusterClientLocalListener);
		cluster.removeDataRemovalListener(clusterClientLocalListener);
		
		executor.shutdownNow();
		localRunningTasks.clear();
	}
	
	@Override
	public String toString() {
		return "FaultTolerantScheduler [ name = "+name+" ]";
	}
	
	public String toDetailedString() {		
		return "FaultTolerantScheduler [ name = "+name+" , local tasks = "+localRunningTasks.size()+" , all tasks "+cacheData.getTaskIDs().size()+" ]";
	}
	
	public void stop() {
		this.shutdownNow();		
	}
	
	private class ClientLocalListener implements FailOverListener, DataRemovalListener {

		/**
		 * the priority of the scheduler as a client local listener of the mobicents cluster
		 */
		private final byte priority;
				
		/**
		 * @param priority
		 */
		public ClientLocalListener(byte priority) {
			this.priority = priority;
		}

		/*
		 * (non-Javadoc)
		 * @see org.mobicents.cluster.FailOverListener#getBaseFqn()
		 */
		@SuppressWarnings("unchecked")
		public Fqn getBaseFqn() {
			return baseFqn;
		}

		/*
		 * (non-Javadoc)
		 * @see org.mobicents.cluster.FailOverListener#getElector()
		 */
		public ClientLocalListenerElector getElector() {
			return null;
		}
		
		/* 
		 * (non-Javadoc)
		 * @see org.mobicents.cluster.FailOverListener#getPriority()
		 */
		public byte getPriority() {
			return priority;
		}

		/*
		 * (non-Javadoc)
		 * @see org.mobicents.cluster.FailOverListener#failOverClusterMember(org.jgroups.Address)
		 */
		public void failOverClusterMember(Address address) {
			
		}
		
		/* 
		 * (non-Javadoc)
		 * @see org.mobicents.cluster.FailOverListener#lostOwnership(org.mobicents.cluster.cache.ClusteredCacheData)
		 */
		public void lostOwnership(ClusteredCacheData clusteredCacheData) {
			
		}

		/* 
		 * (non-Javadoc)
		 * @see org.mobicents.cluster.FailOverListener#wonOwnership(org.mobicents.cluster.cache.ClusteredCacheData)
		 */
		public void wonOwnership(ClusteredCacheData clusteredCacheData) {
			
			if (logger.isDebugEnabled()) {
				logger.debug("wonOwnership( clusterCacheData = "+clusteredCacheData+")");
			}

			try {
				Serializable taskID = TimerTaskCacheData.getTaskID(clusteredCacheData);
				TimerTaskCacheData timerTaskCacheData = new TimerTaskCacheData(taskID, baseFqn, cluster);
				recover(timerTaskCacheData.getTaskData());
			}
			catch (Throwable e) {
				logger.error(e.getMessage(),e);
			}
		}
		
		/*
		 * (non-Javadoc)
		 * @see org.mobicents.cluster.DataRemovalListener#dataRemoved(org.jboss.cache.Fqn)
		 */
		@SuppressWarnings("unchecked")
		public void dataRemoved(Fqn clusteredCacheDataFqn) {
			Object lastElement = clusteredCacheDataFqn.getLastElement();
			if (logger.isDebugEnabled()) {
				logger.debug("remote notification dataRemoved( clusterCacheDataFqn = "+clusteredCacheDataFqn+"), lastElement " + lastElement);
			}
			final TimerTask task = localRunningTasks.remove(lastElement);
			if (task != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("remote notification dataRemoved( task = "+task.getData().getTaskID()+" removed locally cancelling it");
				}
				task.cancel();
			}			
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return FaultTolerantScheduler.this.toString();
		}
		
	}
}
