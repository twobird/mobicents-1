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

package org.mobicents.slee.container.deployment.profile;

import org.apache.log4j.Logger;
import org.mobicents.slee.container.component.profile.ProfileSpecificationComponent;
import org.mobicents.slee.container.deployment.SleeComponentWithUsageParametersClassCodeGenerator;

/**
 * 
 * Start time:17:25:37 2009-03-12<br>
 * Project: mobicents-jainslee-server-core<br>
 * 
 * Base class for calling profile code generation methdos.
 * 
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 */
public class SleeProfileClassCodeGenerator {

  private final static Logger logger = Logger.getLogger(SleeProfileClassCodeGenerator.class);

  public void process(ProfileSpecificationComponent component) throws Exception
  {
    if (logger.isDebugEnabled()) {
      logger.debug("Generating class code for " + component+" at "+component.getDeploymentDir().getAbsolutePath());
    }
    
    // generate profile cmp slee 1.0 wrapper
	new ProfileCmpSlee10WrapperClassGenerator(component).generateClass();
	if (logger.isDebugEnabled()) {
		logger.debug("Generated Profile CMP Slee 1.0 Wrapper for " + component);
	}

    // Generate Profile MBean Interface 
    ConcreteProfileMBeanGenerator mbeanGenerator = new ConcreteProfileMBeanGenerator(component);
    mbeanGenerator.generateProfileMBeanInterface();
    if (logger.isDebugEnabled()) {
      logger.debug("Generated MBean interface for " + component);
    }

	// Generate Profile MBean Impl
    mbeanGenerator.generateProfileMBean();
    if (logger.isDebugEnabled()) {
      logger.debug("Generated MBean impl for " + component);      
    }

   	ConcreteProfileLocalObjectGenerator concreteProfileLocalObjectGenerator = new ConcreteProfileLocalObjectGenerator(component);
    concreteProfileLocalObjectGenerator.generateProfileLocalConcreteClass();
    if (logger.isDebugEnabled()) {
    	logger.debug("Generated Profile Local Object impl for " + component);
    }

    if (component.getProfileTableInterfaceClass() != null) {
    	ConcreteProfileTableGenerator concreteProfileTableGenerator = new ConcreteProfileTableGenerator(component);
    	concreteProfileTableGenerator.generateProfileTable();
    	if (logger.isDebugEnabled()) {
    		logger.debug("Generated Profile Table Interface impl for " + component);
    	}
    }

    if (component.getUsageParametersInterface() != null) {
    	new SleeComponentWithUsageParametersClassCodeGenerator().process(component);
    	if (logger.isDebugEnabled()) {
    		logger.debug("Generated Profile Table Usage Interface impl for " + component);
    		logger.debug("Generated Profile Table Usage MBean impl for " + component);
    	}
    }

  }

  /**
   * Check which combination (see JSLEE 1.0 spec section 10.5.2) matches the
   * Sbb Developer's Profile Specification
   * 
   * @param profileCMPInterfaceName
   *            name of the Profile CMP interface
   * @param profileManagementInterfaceName
   *            name of the Profile Management interface
   * @param profileManagementAbstractClassName
   *            name of the Profile Management Abstract class
   * @return the number of the combination (see JSLEE 1.0 spec section 10.5.2
   *         or 10.5.1.2 in JSLEE 1.1 spec), -1 if it doesn't match no
   *         combination<br>
   *         <ul>
   *         <li><b>1</b> - all cmp fields are exposed to management client and SLEE components(for SLEE as read only), no managemetn methods</li>
   *         <li><b>2</b> - ony double defined CMP accessors and management methods are visible</li>
   *         <li><b>3</b></li>
   *         <li><b>4</b></li>
   *         <li><b>-1</b> - when error occurs, no definitions</li>
   *         </ul>
   */
  public static int checkCombination(ProfileSpecificationComponent component)
  {
    Object profileCmpInterface = component.getDescriptor().getProfileCMPInterface();
    Object profileManagementInterface = component.getDescriptor().getProfileManagementInterface();
    Object profileManagementAbstractClass = component.getDescriptor().getProfileAbstractClass();
    //Object profileManagementLocalObjectInterface = compoenent.getDescriptor().getProfileLocalInterface();
    // if the Profile Specification has no Profile CMP interface, it is incorrect
    if (profileCmpInterface == null)
      return -1;

//    if (compoenent.isSlee11()) {
//      if (profileCmpInterface != null && profileManagementLocalObjectInterface != null && profileManagementAbstractClass != null)
//        return 4;
//      if (profileCmpInterface != null && profileManagementAbstractClass != null)
//        return 3;
//      if (profileCmpInterface != null && profileManagementLocalObjectInterface != null)
//        return 2;
//
//      return 1;
//    }
//    else
//    {
      if (profileCmpInterface != null && profileManagementInterface != null && profileManagementAbstractClass != null)
        return 4;
      if (profileCmpInterface != null && profileManagementAbstractClass != null)
        return 3;
      if (profileCmpInterface != null && profileManagementInterface != null)
        return 2;

      return 1;
//    }
  }

}
