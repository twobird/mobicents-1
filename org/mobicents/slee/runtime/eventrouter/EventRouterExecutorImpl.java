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
package org.mobicents.slee.runtime.eventrouter;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.mobicents.slee.container.SleeContainer;
import org.mobicents.slee.container.activity.ActivityContextHandle;
import org.mobicents.slee.container.event.EventContext;
import org.mobicents.slee.container.eventrouter.EventRouterExecutor;
import org.mobicents.slee.container.eventrouter.EventRoutingTask;
import org.mobicents.slee.container.eventrouter.stats.EventRouterExecutorStatistics;
import org.mobicents.slee.runtime.eventrouter.routingtask.EventRoutingTaskImpl;
import org.mobicents.slee.runtime.eventrouter.stats.EventRouterExecutorStatisticsImpl;

/**
 * 
 * @author martins
 * 
 */
//public class EventRouterExecutorImpl implements EventRouterExecutor {

public class EventRouterExecutorImpl implements EventRouterExecutor {

	private final ExecutorService executor;
	private final EventRouterExecutorStatisticsImpl stats;
	private final SleeContainer sleeContainer;
	
	/**
	 * Used to collect executing stats of an {@link EventRoutingTask}.
	 * 
	 * @author martins
	 * 
	 */
	private class EventRoutingTaskStatsCollector implements Runnable {

		private final EventRoutingTask eventRoutingTask;

		public EventRoutingTaskStatsCollector(EventRoutingTask eventRoutingTask) {
			this.eventRoutingTask = eventRoutingTask;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			final long startTime = System.nanoTime();
			eventRoutingTask.run();
			stats.eventRouted(eventRoutingTask.getEventContext().getEventTypeId(), System
					.nanoTime()
					- startTime);
		}
	}

	/**
	 * Used to collect executing stats of a misc {@link Runnable} task.
	 * 
	 * @author martins
	 * 
	 */
	private class MiscTaskStatsCollector implements Runnable {

		private final Runnable runnable;

		public MiscTaskStatsCollector(Runnable runnable) {
			this.runnable = runnable;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			final long startTime = System.nanoTime();
			runnable.run();
			stats.miscTaskExecuted(System.nanoTime() - startTime);
		}
	}

	/**
	 * 
	 */
	public EventRouterExecutorImpl(boolean collectStats, SleeContainer sleeContainer) {		
		//this.executor = Executors.newSingleThreadExecutor();
		/*
			@sainty 2016-07-04
			�滻ԭ�еĵ��̵߳ı��Ͳ��Բ������µ��Ŷӻ���
		*/
		ThreadPoolExecutor executor_new = new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<Runnable>(10));
		executor_new.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		this.executor = executor_new;
		
		stats = collectStats ? new EventRouterExecutorStatisticsImpl() : null;
		this.sleeContainer = sleeContainer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.slee.runtime.eventrouter.EventRouterExecutor#getStatistics
	 * ()
	 */
	public EventRouterExecutorStatistics getStatistics() {
		return stats;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.slee.runtime.eventrouter.EventRouterExecutor#shutdown()
	 */
	public void shutdown() {
		executor.shutdown();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.slee.runtime.eventrouter.EventRouterExecutor#execute(java
	 * .lang.Runnable)
	 */
	public void execute(Runnable task) {
		if (stats == null) {
			executor.execute(task);
		} else {
			executor.execute(new MiscTaskStatsCollector(task));
		}
	}

	/* (non-Javadoc)
	 * @see org.mobicents.slee.core.runtime.eventrouter.EventRouterExecutor#executeNow(java.lang.Runnable)
	 */
	public void executeNow(Runnable task) throws InterruptedException, ExecutionException {
		if (stats == null) {
			executor.submit(task).get();
		} else {
			executor.submit(new MiscTaskStatsCollector(task)).get();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.slee.runtime.eventrouter.EventRouterExecutor#activityMapped(org.mobicents.slee.runtime.activity.ActivityContextHandle)
	 */
	public void activityMapped(ActivityContextHandle ach) {
		if (stats != null) {
			stats.activityMapped(ach);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.slee.runtime.eventrouter.EventRouterExecutor#activityUnmapped(org.mobicents.slee.runtime.activity.ActivityContextHandle)
	 */
	public void activityUnmapped(ActivityContextHandle ach) {
		if (stats != null) {
			stats.activityUnmapped(ach);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.slee.runtime.eventrouter.EventRouterExecutor#routeEvent(org.mobicents.slee.core.event.SleeEvent)
	 */
	public void routeEvent(EventContext event) {
		final EventRoutingTaskImpl eventRoutingTask = new EventRoutingTaskImpl(event,sleeContainer);
		if (stats == null) {
			executor.execute(eventRoutingTask);
		} else {
			executor.execute(new EventRoutingTaskStatsCollector(
					eventRoutingTask));
		}
		/*try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		System.out.println("��ǰִ�е�executor���ǣ�"+executor.hashCode());
		System.out.println("��ǰִ�е�executor�����������е�����:"+((ThreadPoolExecutor)executor).getQueue().size());
	}

	public int getQueueLenth() {
		// TODO Auto-generated method stub
		return ((ThreadPoolExecutor)executor).getQueue().size();
	}
	
	public String toString() {
		// TODO Auto-generated method stub
		return "" + ((ThreadPoolExecutor)executor).getQueue().size();
	}

}
