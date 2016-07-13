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

import java.util.HashSet;
import java.util.Set;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;

import javax.slee.InvalidStateException;
import javax.slee.SLEEException;
import javax.slee.management.DeploymentException;
import javax.slee.management.ManagementException;
import javax.slee.profile.ProfileImplementationException;

import org.apache.log4j.Logger;
import org.mobicents.slee.container.component.ClassPool;
import org.mobicents.slee.container.component.profile.ProfileSpecificationComponent;
import org.mobicents.slee.container.component.profile.ProfileSpecificationDescriptor;
import org.mobicents.slee.container.profile.AbstractProfileMBean;
import org.mobicents.slee.container.profile.AbstractProfileMBeanImpl;

public class ConcreteProfileMBeanGenerator {

	private static final Logger logger = Logger.getLogger(ConcreteProfileMBeanGenerator.class);

	private ProfileSpecificationComponent component = null;
	private String cmpProfileInterfaceName = null;

	private String profileManagementInterfaceName = null;
	private ClassPool pool = null;
	private CtClass cmpProfileInterface = null;
	private CtClass profileManagementInterface = null;
	private CtClass profileMBeanConcreteClass = null;
	private CtClass profileMBeanConcreteInterface = null;
	
	/**
	 * holds all cmp acessor methods to copy to mbean interface and implement in mbean impl
	 */
	private Set<CtMethod> mBeanCmpAcessorMethods = new HashSet<CtMethod>();
	/**
	 * holds all management methods to copy to mbean interface and implement in mbean impl
	 */
	private Set<CtMethod> mBeanManagementMethods = new HashSet<CtMethod>();
	
	public ConcreteProfileMBeanGenerator(ProfileSpecificationComponent component) {
		this.component = component;
		ProfileSpecificationDescriptor descriptor = component.getDescriptor();
		this.cmpProfileInterfaceName = descriptor.getProfileCMPInterface().getProfileCmpInterfaceName();
		this.profileManagementInterfaceName = descriptor.getProfileManagementInterface() == null ? null : descriptor.getProfileManagementInterface();
		this.pool = component.getClassPool();
	}

	/**
	 * Generates the Profile MBean interface
	 * 
	 * @return the interface generated
	 */
	public void generateProfileMBeanInterface() throws Exception {
		
		if (SleeProfileClassCodeGenerator.checkCombination(component) == -1) {
			throw new DeploymentException("Profile Specification doesn't match any combination " + "from the JSLEE spec 1.0 section 10.5.2");
		}
		
		String profileMBeanConcreteInterfaceName = cmpProfileInterfaceName + "MBean";
		
		profileMBeanConcreteInterface = pool.makeInterface(profileMBeanConcreteInterfaceName);

		try {
			cmpProfileInterface = pool.get(cmpProfileInterfaceName);
			profileManagementInterface = profileManagementInterfaceName != null ? pool.get(profileManagementInterfaceName) : null;
		}
		catch (NotFoundException nfe) {
			throw new DeploymentException("Failed to locate CMP/Management Interface for " + component, nfe);
		}
		
		// set interface
		try {
			profileMBeanConcreteInterface.addInterface(pool.get(AbstractProfileMBean.class.getName()));
		}
		catch (Throwable e) {
			throw new SLEEException(e.getMessage(),e);
		}
		
		// gather exceptions that the mbean methods may throw
		CtClass[] managementMethodExceptions = new CtClass[3];
		try {
			managementMethodExceptions[0] = pool.get(ManagementException.class.getName());
			managementMethodExceptions[1] = pool.get(InvalidStateException.class.getName());
			managementMethodExceptions[2] = pool.get(ProfileImplementationException.class.getName());
		}
		catch (NotFoundException e) {
			throw new SLEEException(e.getMessage(),e);
		}
		CtClass[] cmpGetAcessorMethodExceptions = new CtClass[] {managementMethodExceptions[0]};
		CtClass[] cmpSetAcessorMethodExceptions = new CtClass[] {managementMethodExceptions[0],managementMethodExceptions[1]};
		
		// gather all Object class methods, we don't want those in the mbean
		Set<CtMethod> objectMethods = new HashSet<CtMethod>();
		try {
			CtClass objectClass = pool.get(Object.class.getName());
			for (CtMethod ctMethod : objectClass.getMethods()) {
				objectMethods.add(ctMethod);
			}
		}
		catch (NotFoundException e) {
			throw new SLEEException(e.getMessage(),e);
		}
		
		// gather methods to copy
		Set<CtMethod> cmpAcessorMethods = new HashSet<CtMethod>();
		Set<CtMethod> managementMethods = new HashSet<CtMethod>();
		if (profileManagementInterface != null) {
			// If the Profile Specification defines a Profile Management Interface, the profileMBean interface has the same methods
			// 1. gather all methods from management interface
			for (CtMethod ctMethod : profileManagementInterface.getMethods()) {
				if (!objectMethods.contains(ctMethod)) {
					managementMethods.add(ctMethod);
				}
			}
			// 2. gather all methods present also in cmp interface, removing those from the ones gather from management interface
			for (CtMethod ctMethod : cmpProfileInterface.getMethods()) {
				if (!objectMethods.contains(ctMethod)) {
					if (managementMethods.remove(ctMethod)) {
						cmpAcessorMethods.add(ctMethod);					
					}
				}
			}
		}
		else {
			for (CtMethod ctMethod : cmpProfileInterface.getMethods()) {
				if (!objectMethods.contains(ctMethod)) {
					cmpAcessorMethods.add(ctMethod);									
				}
			}
		}
		
		// copy cmp acessor & mngt methods
		for (CtMethod ctMethod : cmpAcessorMethods) {
			// copy method
			CtMethod methodCopy = new CtMethod(ctMethod, profileMBeanConcreteInterface, null);
			// set exceptions
			CtClass[] exceptions = null;
			if(ctMethod.getName().startsWith("set")) {
				exceptions = cmpSetAcessorMethodExceptions;							
			}
			else if(ctMethod.getName().startsWith("get")) {
				exceptions = cmpGetAcessorMethodExceptions;
			}
			else {
				throw new DeploymentException("unexpected method in profile cmp interface "+ctMethod);
			}
			methodCopy.setExceptionTypes(exceptions);
			// add to class
			profileMBeanConcreteInterface.addMethod(methodCopy);
			// store in set to be used in mbean impl
			mBeanCmpAcessorMethods.add(methodCopy);
		}
		for (CtMethod ctMethod : managementMethods) {
			// copy method
			CtMethod methodCopy = new CtMethod(ctMethod, profileMBeanConcreteInterface, null);
			// set exceptions
			methodCopy.setExceptionTypes(managementMethodExceptions);
			// add to class
			profileMBeanConcreteInterface.addMethod(methodCopy);
			// store in set to be used in mbean impl
			mBeanManagementMethods.add(methodCopy);

		}
		
		// write class file
		try {
			profileMBeanConcreteInterface.writeFile(this.component.getDeploymentDir().getAbsolutePath());
		}
		catch (Throwable e) {
			throw new SLEEException(e.getMessage(), e);
		}
		finally {
			profileMBeanConcreteInterface.defrost();	
		}
		
		// and load it to the component
		try {
			this.component.setProfileMBeanConcreteInterfaceClass(Thread.currentThread().getContextClassLoader().loadClass(profileMBeanConcreteInterfaceName));
		}
		catch (Throwable e) {
			throw new SLEEException(e.getMessage(), e);
		}	
	}
	
	/**
	 * This method generates concrete class of MBean impl
	 */
	public void generateProfileMBean() throws Exception
	{
		if (SleeProfileClassCodeGenerator.checkCombination(component) == -1)
		{
			throw new DeploymentException("Profile Specification doesn't match any combination " + "from the JSLEE spec 1.0 section 10.5.2");
		}

		String profileMBeanConcreteClassName = profileMBeanConcreteInterface.getName() + "Impl";

		profileMBeanConcreteClass = pool.makeClass(profileMBeanConcreteClassName);

		// set interface & super class
		try {
			profileMBeanConcreteClass.setInterfaces(new CtClass[] {profileMBeanConcreteInterface});
			profileMBeanConcreteClass.setSuperclass(pool.get(AbstractProfileMBeanImpl.class.getName()));			
		}
		catch (NotFoundException e) {
			throw new SLEEException(e.getMessage(),e);
		}
		
		// implement cmp acessor & management methods gather in the mbean interface building
		for (CtMethod method : mBeanCmpAcessorMethods) {
			
			// copy method & remove abstract modifier			
			CtMethod newMethod = CtNewMethod.copy( method, profileMBeanConcreteClass, null );
			// generate body
			String body = null;				
			if (method.getName().startsWith("set")) {
				body = 	"{ " +
						"	beforeSetCmpField();" +
						"	try { " +
						"		(("+component.getProfileCmpConcreteClass().getName()+")getProfileObject().getProfileConcrete())." + method.getName()+"($1);" +
						"	} finally {" +
						"		afterSetCmpField();" +
						"	}" +
						"}"; 				
			}
			else {
				body = 	"{ " +
						"	boolean activatedTransaction = beforeGetCmpField();" +
						"	try { " +
						"		return ($r) (("+component.getProfileCmpConcreteClass().getName()+")getProfileObject().getProfileConcrete())." + method.getName()+"();" +
						"	} finally {" +
						"		afterGetCmpField(activatedTransaction);" +
						"	}" +
						"}";
			}
			if(logger.isTraceEnabled()) {
				logger.trace("Implemented profile mbean method named "+method.getName()+", with body:\n"+body);
			}
			newMethod.setBody(body);
			profileMBeanConcreteClass.addMethod(newMethod);
		}
		
		for (CtMethod method : mBeanManagementMethods) {
			
			// copy method & remove abstract modifier			
			CtMethod newMethod = CtNewMethod.copy( method, profileMBeanConcreteClass, null );
			
			// generate body
			boolean voidReturnType = newMethod.getReturnType().equals(CtClass.voidType);
				
			String body = "{ boolean activatedTransaction = beforeManagementMethodInvocation(); try { ";
			if (!voidReturnType) {
				body += "return ($r) ";
			}
			body += "(("+component.getProfileCmpConcreteClass().getName()+")getProfileObject().getProfileConcrete())." + method.getName()+"($$); } catch(Throwable t) { throwableOnManagementMethodInvocation(t); } finally { afterManagementMethodInvocation(activatedTransaction); }";
			if (!voidReturnType) {
				body += "throw new "+SLEEException.class.getName()+"(\"bad code generated\");";				 				
			}
			body += " }";
						
			if(logger.isTraceEnabled()) {
				logger.trace("Implemented profile mbean method named "+method.getName()+", with body:\n"+body);
			}
			newMethod.setBody(body);
			profileMBeanConcreteClass.addMethod(newMethod);
		}		
		
		try {
			profileMBeanConcreteClass.writeFile(this.component.getDeploymentDir().getAbsolutePath());
		}
		catch (Throwable e) {
			throw new SLEEException(e.getMessage(),e);
		}
		finally {
			profileMBeanConcreteClass.defrost();
		}

		try {
			component.setProfileMBeanConcreteImplClass(Thread.currentThread().getContextClassLoader().loadClass(profileMBeanConcreteClassName));
		}
		catch (Throwable e) {
			throw new SLEEException(e.getMessage(),e);
		}
	}

}
