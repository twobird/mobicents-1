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

package org.rhq.plugins.jslee;

import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.security.auth.login.LoginException;

import org.mobicents.slee.container.management.jmx.EventRouterConfigurationMBean;
import org.mobicents.slee.container.management.jmx.EventRouterStatisticsMBean;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.measurement.MeasurementDataNumeric;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.plugins.jmx.MBeanResourceComponent;
import org.rhq.plugins.jslee.utils.JainSleeServerUtils;
import org.rhq.plugins.jslee.utils.MBeanServerUtils;

/**
 * JAIN SLEE 2.x Event Router Statistics Component
 * 
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 */
public class ExecutorComponent extends MBeanResourceComponent {

  private ResourceContext<JainSleeServerComponent> resourceContext;

  volatile MBeanServerUtils mbeanUtils = null;
  
  Integer executorId = null;

  public void start(ResourceContext context) {
    if(log.isTraceEnabled()) {
      log.trace("start(" + context + ") called.");
    }

    this.resourceContext = context;

    this.executorId = this.resourceContext.getPluginConfiguration().getSimple("executorId").getIntegerValue();
    this.mbeanUtils = ((JainSleeServerUtils) context.getParentResourceComponent()).getMBeanServerUtils();    
  }

  public AvailabilityType getAvailability() {
    try {
      if(log.isTraceEnabled()) {
        log.trace("getAvailability() called.");
      }

      MBeanServerConnection connection = this.mbeanUtils.getConnection();
      this.mbeanUtils.login();
  
      ObjectName erConfigObjectName = new ObjectName("org.mobicents.slee:name=EventRouterConfiguration"/* FIXME */);
  
      EventRouterConfigurationMBean erConfigMBean = (EventRouterConfigurationMBean) MBeanServerInvocationHandler.newProxyInstance(connection, 
          erConfigObjectName, EventRouterConfigurationMBean.class, false);
  
      return this.executorId < erConfigMBean.getEventRouterThreads() ? AvailabilityType.UP : AvailabilityType.DOWN;
    }
    catch (Exception e) {
      log.debug("Failed to get Availability for Executor #" + this.executorId, e);
    }
    finally {
      try {
        mbeanUtils.logout();
      }
      catch (LoginException e) {
        if(log.isDebugEnabled()) {
          log.debug("Failed to logout from secured JMX", e);
        }
      }
    }
    
    return AvailabilityType.DOWN;
  }

  public void getValues(MeasurementReport report, Set requests) {
    try {
      if(log.isTraceEnabled()) {
        log.trace("getValues(" + report + "," + requests + ") called.");
      }

      Set<MeasurementScheduleRequest> metrics = requests;
      
      EventRouterStatisticsMBean erStats = getEventRouterStatisticsMBean();
      
      for (MeasurementScheduleRequest request : metrics) {
        if (request.getName().equals("activitiesMapped")) {
          report.addData(new MeasurementDataNumeric(request, Double.valueOf(erStats.getActivitiesMapped(executorId))));
        }
        else if (request.getName().equals("averageEventRoutingTime")) {
          report.addData(new MeasurementDataNumeric(request, Double.valueOf(erStats.getAverageEventRoutingTime(executorId))));
        }
        else if (request.getName().equals("executedTasks")) {
          report.addData(new MeasurementDataNumeric(request, Double.valueOf(erStats.getExecutedTasks(executorId))));
        }
        else if (request.getName().equals("executingTime")) {
          report.addData(new MeasurementDataNumeric(request, Double.valueOf(erStats.getExecutingTime(executorId))));
        }
        else if (request.getName().equals("idleTime")) {
          report.addData(new MeasurementDataNumeric(request, Double.valueOf(erStats.getIdleTime(executorId))));
        }
        else if (request.getName().equals("miscTasksExecuted")) {
          report.addData(new MeasurementDataNumeric(request, Double.valueOf(erStats.getMiscTasksExecuted(executorId))));
        }
        else if (request.getName().equals("miscTasksExecutingTime")) {
          report.addData(new MeasurementDataNumeric(request, Double.valueOf(erStats.getMiscTasksExecutingTime(executorId))));
        }
      }
    }
    catch (Exception e) {
      log.debug("Failure gathering values for Executor #" + executorId, e);
    }
  }

  private EventRouterStatisticsMBean getEventRouterStatisticsMBean() throws Exception {
    try {
      MBeanServerConnection connection = this.mbeanUtils.getConnection();
      this.mbeanUtils.login();

      ObjectName erStatsObjectName = new ObjectName("org.mobicents.slee:name=EventRouterStatistics"/* FIXME */);
      EventRouterStatisticsMBean erStatsMBean = (EventRouterStatisticsMBean) MBeanServerInvocationHandler.newProxyInstance(connection, 
          erStatsObjectName, EventRouterStatisticsMBean.class, false);
      
      return erStatsMBean;
    }
    finally {
      try {
        mbeanUtils.logout();
      }
      catch (LoginException e) {
        if(log.isDebugEnabled()) {
          log.debug("Failed to logout from secured JMX", e);
        }
      }
    }
  }

}
