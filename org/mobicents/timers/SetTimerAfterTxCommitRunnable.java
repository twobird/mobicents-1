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

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;


/**
 * Runnable to set a timer task after the tx commits.
 * 
 * @author martins
 *
 */
public class SetTimerAfterTxCommitRunnable extends AfterTxCommitRunnable {

	private static final Logger logger = Logger
			.getLogger(SetTimerAfterTxCommitRunnable.class);


	private boolean canceled = false;

	SetTimerAfterTxCommitRunnable(TimerTask task,
			FaultTolerantScheduler scheduler) {
		super(task,scheduler);
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.timers.AfterTxCommitRunnable#getType()
	 */
	@Override
	public Type getType() {
		return AfterTxCommitRunnable.Type.SET;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		
		task.setSetTimerTransactionalAction(null);
		
		if (!canceled) {
			
			TimerTask previousTask = scheduler.getLocalRunningTasksMap().putIfAbsent(task.getData().getTaskID(), task);
			if(previousTask != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("A task with id " + task.getData().getTaskID() + " has already been added to the local tasks, not rescheduling");
				}
			}
			
			final TimerTaskData taskData = task.getData();
			// calculate delay
			long delay = taskData.getStartTime() - System.currentTimeMillis();
			if (delay < 0) {
				delay = 0;
			}
			
			try {
				// schedule runnable
				if (taskData.getPeriod() < 0) {
					if (logger.isDebugEnabled()) {
						logger.debug("Scheduling one-shot timer with id "
								+ task.getData().getTaskID() + ", delay " + delay);
					}
					task.setScheduledFuture(scheduler.getExecutor().schedule(task, delay, TimeUnit.MILLISECONDS));
				} else {
					if (logger.isDebugEnabled()) {
						logger.debug("Scheduling periodic timer with id "
								+ task.getData().getTaskID() + ", scheduling strategy " + taskData.getPeriodicScheduleStrategy() + ", delay " + delay + ", period " + taskData.getPeriod());
					}
					if (taskData.getPeriodicScheduleStrategy() == PeriodicScheduleStrategy.withFixedDelay) {
						task.setScheduledFuture(scheduler.getExecutor().scheduleWithFixedDelay(task, delay, taskData.getPeriod(),TimeUnit.MILLISECONDS));
					}
					else {
						// default
						task.setScheduledFuture(scheduler.getExecutor().scheduleAtFixedRate(task, delay, taskData.getPeriod(),TimeUnit.MILLISECONDS));
					}					
				}		
			} catch (Throwable e) {
				logger.error(e.getMessage(), e);
				scheduler.remove(taskData.getTaskID(),true);
			}
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("Canceled scheduling periodic timer with id "
						+ task.getData().getTaskID());
			}
		}
	}

	public void cancel() {
		if (logger.isDebugEnabled()) {
			logger.debug("Canceling set timer action for task with timer id "+task.getData().getTaskID());
		}
		canceled = true;
		scheduler.remove(task.getData().getTaskID(),true);
	}

}
