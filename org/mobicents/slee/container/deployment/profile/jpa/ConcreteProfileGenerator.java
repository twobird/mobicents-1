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

package org.mobicents.slee.container.deployment.profile.jpa;

import java.beans.Introspector;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.CtPrimitiveType;
import javassist.NotFoundException;

import javax.slee.Address;
import javax.slee.SLEEException;
import javax.slee.profile.Profile;
import javax.slee.profile.ProfileManagement;

import org.apache.log4j.Logger;
import org.mobicents.slee.container.component.ClassPool;
import org.mobicents.slee.container.component.profile.ProfileAttribute;
import org.mobicents.slee.container.component.profile.ProfileConcreteClassInfo;
import org.mobicents.slee.container.component.profile.ProfileSpecificationComponent;
import org.mobicents.slee.container.component.profile.ProfileSpecificationDescriptor;
import org.mobicents.slee.container.component.profile.cmp.ProfileCMPInterfaceDescriptor;
import org.mobicents.slee.container.deployment.ClassUtils;
import org.mobicents.slee.container.deployment.profile.ClassGeneratorUtils;
import org.mobicents.slee.container.deployment.profile.SleeProfileClassCodeGenerator;
import org.mobicents.slee.container.profile.ProfileCmpHandler;
import org.mobicents.slee.container.profile.ProfileConcrete;
import org.mobicents.slee.container.profile.ProfileObjectImpl;
import org.mobicents.slee.container.util.ObjectCloner;

/**
 * 
 * Generates the ProfileConcrete impl for a specific Profile Specification.
 * 
 * <br>
 * Project: mobicents <br>
 * 11:16:57 AM Mar 23, 2009 <br>
 * 
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 */
@SuppressWarnings("deprecation")
public class ConcreteProfileGenerator {

	private static final Logger logger = Logger
			.getLogger(ConcreteProfileGenerator.class);

	private final ProfileSpecificationComponent profileComponent;

	private final int profileCombination;

	public ConcreteProfileGenerator(
			ProfileSpecificationComponent profileComponent) {
		this.profileComponent = profileComponent;
		this.profileCombination = SleeProfileClassCodeGenerator
				.checkCombination(profileComponent);
		ClassGeneratorUtils.setClassPool(this.profileComponent.getClassPool()
				.getClassPool());
	}

	@SuppressWarnings("unchecked")
	public Class<?> generateConcreteProfile() {

		Class<?> clazz = null;

		try {
			/*
			 * 10.12 Profile concrete class A Profile concrete class is
			 * implemented by the SLEE when a Profile Specification is deployed.
			 * The Profile concrete class extends the Profile abstract class and
			 * implements the Profile CMP methods.
			 * 
			 * The following rules apply to the Profile concrete class
			 * implemented by the SLEE: - If a Profile abstract class is
			 * defined, then the SLEE implemented Profile concrete class extends
			 * the Profile abstract class and implements the Profile CMP
			 * methods.
			 * 
			 * - If a Profile abstract class is not defined, then the SLEE
			 * implemented Profile concrete class provides an implementation of
			 * the Profile CMP interface.
			 */

			ProfileSpecificationDescriptor profileDescriptor = profileComponent
					.getDescriptor();

			if (logger.isTraceEnabled())
				logger.trace("Profile combination for "
					+ profileComponent.getProfileSpecificationID() + " = "
					+ this.profileCombination);

			String deployDir = profileComponent.getDeploymentDir()
					.getAbsolutePath();

			ProfileCMPInterfaceDescriptor cmpInterface = profileDescriptor
					.getProfileCMPInterface();

			String concreteClassName = cmpInterface
					.getProfileCmpInterfaceName()
					+ "Impl";

			// Create the Impl class
			CtClass profileConcreteClass = ClassGeneratorUtils.createClass(
					concreteClassName, new String[] {
							cmpInterface.getProfileCmpInterfaceName(),
							ProfileConcrete.class.getName() });

			// If this is combination 3 or 4, the the concrete class extends the
			// Concrete Profile Management Abstract Class
			if (profileCombination >= 3) {
				if (profileDescriptor.getProfileAbstractClass() != null) {
					ClassGeneratorUtils.createInheritanceLink(
							profileConcreteClass, profileDescriptor
									.getProfileAbstractClass()
									.getProfileAbstractClassName());
				}

				if (profileDescriptor.getProfileManagementInterface() != null) {
					ClassGeneratorUtils.createInterfaceLinks(
							profileConcreteClass,
							new String[] { profileDescriptor
									.getProfileManagementInterface() });
				}
			}

			generateProfileConcreteClassInfo(profileConcreteClass);
			
			// add profile object field and getter/setter
			CtField fProfileObject = ClassGeneratorUtils
					.addField(ClassGeneratorUtils.getClass(ProfileObjectImpl.class
							.getName()), "profileObject", profileConcreteClass);
			ClassGeneratorUtils.generateGetterAndSetter(fProfileObject, null);

			// CMP fields getters and setters
			generateCMPAccessors(profileConcreteClass);

			generateConstructors(profileConcreteClass);

			// Profile Management methods for JAIN SLEE 1.1
			Map<String, CtMethod> profileManagementMethods = new HashMap<String, CtMethod>();

			// only add Profile Management methods for JAIN SLEE 1.0 that are implemented by SLEE
			for (Object object : ClassUtils
					.getInterfaceMethodsFromInterface(ClassGeneratorUtils
							.getClass(ProfileManagement.class.getName())).entrySet()) {
				Entry entry = (Entry) object;
				CtMethod ctMethod = (CtMethod) entry.getValue();
				if (ctMethod.getName().equals("markProfileDirty") || ctMethod.getName().equals("isProfileDirty") || ctMethod.getName().equals("isProfileValid")) {
					profileManagementMethods.put((String) entry.getKey(),ctMethod);
				}				
			}

			// Check for a Profile Management Interface
			Class<?> profileManagementInterface = this.profileComponent
					.getProfileManagementInterfaceClass();

			if (profileManagementInterface != null) {
				profileManagementMethods
						.putAll(ClassUtils
								.getInterfaceMethodsFromInterface(ClassGeneratorUtils
										.getClass(profileManagementInterface
												.getName())));
			}

			Class<?> profileLocalInterface = this.profileComponent
					.getProfileLocalInterfaceClass();

			if (profileLocalInterface != null) {
				profileManagementMethods.putAll(ClassUtils
						.getAbstractMethodsFromClass((ClassGeneratorUtils
								.getClass(profileLocalInterface.getName()))));
			}

			Map<String, CtMethod> cmpInterfaceMethods = ClassUtils
					.getInterfaceMethodsFromInterface(ClassGeneratorUtils
							.getClass(this.profileComponent
									.getProfileCmpInterfaceClass().getName()));
			generateManagementHandlerDelegationMethods(profileConcreteClass,
					profileManagementMethods, cmpInterfaceMethods);

			if (profileComponent.getUsageParametersInterface() != null) {
				generateDefaultUsageParameterGetter(profileConcreteClass);
				generateNamedUsageParameterGetter(profileConcreteClass);
			}

			profileConcreteClass.getClassFile().setVersionToJava5();

			if (logger.isTraceEnabled())
				logger.trace("Writing PROFILE CONCRETE CLASS to: " + deployDir);

			profileConcreteClass.writeFile(deployDir);

			clazz = Thread.currentThread().getContextClassLoader().loadClass(
					profileConcreteClass.getName());

			profileConcreteClass.defrost();

		} catch (Exception e) {
			throw new SLEEException(e.getMessage(), e);
		}

		return clazz;
	}

	private void generateConstructors(CtClass profileConcreteClass) {
		ClassGeneratorUtils.generateDefaultConstructor(profileConcreteClass);
	}

	/**
	 * Create a named usage parameter getter.
	 * 
	 * @param profileConcreteClass
	 * @throws SLEEException
	 */
	private void generateNamedUsageParameterGetter(CtClass profileConcreteClass) {
		String methodName = "getUsageParameterSet";
		for (CtMethod ctMethod : profileConcreteClass.getMethods()) {
			if (ctMethod.getName().equals(methodName)) {				
				try {
					// copy method, we can't just add body becase it is in super
					// class and does not sees profileObject field
					CtMethod ctMethodCopy =  CtNewMethod.copy(ctMethod, profileConcreteClass, null);
					// create the method body
					String methodBody = "{ return ($r)"
						+ ClassGeneratorUtils.MANAGEMENT_HANDLER
						+ ".getUsageParameterSet(profileObject,$1); }";
					if (logger.isTraceEnabled())
						logger.trace("Implemented method " + methodName
								+ " , body = " + methodBody);
					ctMethodCopy.setBody(methodBody);
					profileConcreteClass.addMethod(ctMethodCopy);
				} catch (CannotCompileException e) {
					throw new SLEEException(e.getMessage(), e);
				}				
			}
		}
	}

	private void generateDefaultUsageParameterGetter(
			CtClass profileConcreteClass) {
		String methodName = "getDefaultUsageParameterSet";
		for (CtMethod ctMethod : profileConcreteClass.getMethods()) {
			if (ctMethod.getName().equals(methodName)) {
				try {
					// copy method, we can't just add body becase it is in super
					// class and does not sees profileObject field
					CtMethod ctMethodCopy =  CtNewMethod.copy(ctMethod, profileConcreteClass, null);
					// create the method body
					String methodBody = "{ return ($r)"
						+ ClassGeneratorUtils.MANAGEMENT_HANDLER
						+ ".getDefaultUsageParameterSet(profileObject); }";
					if (logger.isTraceEnabled())
						logger.trace("Implemented method " + methodName
								+ " , body = " + methodBody);
					ctMethodCopy.setBody(methodBody);
					profileConcreteClass.addMethod(ctMethodCopy);
				} catch (CannotCompileException e) {
					throw new SLEEException(e.getMessage(), e);
				}
			}
		}
	}

	private void generateManagementHandlerDelegationMethods(CtClass profileConcreteClass,
			Map<String, CtMethod> methods,
			Map<String, CtMethod> cmpInterfaceMethods) {
		// boolean useInterceptor = true;
		Class<?> abstractClass = this.profileComponent
				.getProfileAbstractClass();

		Iterator<Map.Entry<String, CtMethod>> mm = methods.entrySet()
				.iterator();

		Set<String> implementedMethods = new HashSet<String>();
		implementedMethods.addAll(cmpInterfaceMethods.keySet());

		while (mm.hasNext()) {
			String interceptor = ClassGeneratorUtils.MANAGEMENT_HANDLER;

			Map.Entry<String, CtMethod> entry = mm.next();

			CtMethod method = entry.getValue();

			// We should use key, but ClassUtils has different behaviors... go
			// safe!
			String methodKey = method.getName() + method.getSignature();

			if (!implementedMethods.add(methodKey)) {
				// This was already implemented
				continue;
			}

			if (abstractClass != null) {
				try {
					int i = 0;
					Class<?>[] pTypes = new Class[method.getParameterTypes().length];

					for (CtClass pType : method.getParameterTypes()) {
						if (pType.isPrimitive())
							pTypes[i++] = ((Class<?>) Class.forName(
									((CtPrimitiveType) pType).getWrapperName())
									.getField("TYPE").get(null));
						else
							pTypes[i++] = Class.forName(pType.getClassFile()
									.getName());
					}

					Method m = abstractClass
							.getMethod(method.getName(), pTypes);
					interceptor = Modifier.isAbstract(m.getModifiers()) ? interceptor
							: "super";
				} catch (Exception e) {
					if (!(e instanceof NoSuchMethodException))
						throw new SLEEException(
								"Problem with Business method generation: "
										+ method.getName(), e);

					// else ignore... we are using default interceptor.
				}
			}
			
			try {
				ClassGeneratorUtils.generateDelegateMethod(
						profileConcreteClass,method, interceptor,
						true);
			} catch (Exception e) {
				throw new SLEEException(e.getMessage(), e);
			}
		}
	}

	private void generateCMPAccessors(CtClass profileConcreteClass)
			throws Exception {

		// Get the CMP interface to generate the getters/setters
		ProfileCMPInterfaceDescriptor cmpInterface = profileComponent.getDescriptor()
				.getProfileCMPInterface();

		ClassPool pool = profileComponent.getClassPool();

		CtClass cmpInterfaceClass = pool.get(cmpInterface
				.getProfileCmpInterfaceName());
		CtClass objectClass = pool.get(Object.class.getName());

		for (CtMethod method : cmpInterfaceClass.getMethods()) {

			if (!method.getDeclaringClass().equals(objectClass)) {
				// ignoring methods from Object class

				if (method.getName().startsWith("get")) {
					generateCMPGetter(method, profileConcreteClass);
				} else if (method.getName().startsWith("set")) {
					generateCMPSetter(method, profileConcreteClass);
				} else {
					throw new SLEEException(
							"unexpected method name in cmp interface "
									+ method.getName());
				}
			}
		}
	}

	private boolean isPrimitiveOrPrimitiveArray(CtClass ctClass) {
		if (ctClass.isArray()) {
			try {
				return ctClass.getComponentType().isPrimitive();
			} catch (NotFoundException e) {
				throw new SLEEException(e.getMessage(), e);
			}
		} else {
			return ctClass.isPrimitive();
		}
	}

	private void generateCMPGetter(CtMethod method,
			CtClass classToBeInstrumented) throws Exception {

		String fieldName = Introspector.decapitalize(method.getName()
				.replaceFirst("get", ""));

		boolean isPrimitive = isPrimitiveOrPrimitiveArray(method
				.getReturnType());

		// 1. invoke profile entity getter
		String profileEntityGetterInvocationBody = "(("
				+ profileComponent.getProfileEntityFramework()
						.getProfileEntityClass().getName()
				+ ")profileObject.getProfileEntity()).get"
				+ ClassGeneratorUtils.getPojoCmpAccessorSufix(fieldName) + "()";

		// 2. create the open and close "return" code - do a deep copy on the
		// result if the return type is not a primitive, this ensures no refs
		// with original value
		String returnOpenBody = "return ($r) "
				+ (!isPrimitive ? ObjectCloner.class.getName()
						+ ".makeDeepCopy(" : "");
		String returnCloseBody = !isPrimitive ? ");" : ";";

		// 3. lets add the code between the profile entity getter invocation and
		// the return clause - if the return type is an array then we need to
		// convert it from the attr array value list
		String methodBody = null;
		if (method.getReturnType().isArray()) {
			if (isPrimitiveOrPrimitiveArray(method.getReturnType())
					|| method.getReturnType().getComponentType().getName()
							.equals(String.class.getName())
					|| method.getReturnType().getComponentType().getName()
							.equals(Address.class.getName())) {
				methodBody = returnOpenBody
						+ ProfileAttributeArrayValueUtils.class.getName()
						+ ".to"
						+ method.getReturnType().getComponentType()
								.getSimpleName() + "Array("
						+ profileEntityGetterInvocationBody + ")"
						+ returnCloseBody;
			} else {
				methodBody = List.class.getName() + " list = "
						+ profileEntityGetterInvocationBody + ";"
						+ returnOpenBody
						+ ProfileAttributeArrayValueUtils.class.getName()
						+ ".toSerializableArray( new "
						+ method.getReturnType().getComponentType().getName()
						+ "[list.size()] , list)" + returnCloseBody;
			}
		} else {
			methodBody = returnOpenBody + profileEntityGetterInvocationBody
					+ returnCloseBody;
		}
		;
		// add final method wrappers
		methodBody = "{" + ProfileCmpHandler.class.getName()
				+ ".beforeGetCmpField(profileObject);" + "	try { " + methodBody
				+ " } finally { " + ProfileCmpHandler.class.getName()
				+ ".afterGetCmpField(profileObject);	}" + "}";

		if (logger.isTraceEnabled()) {
			logger.trace("Adding method named " + method.getName()
					+ ", with source : " + methodBody + ", into: "
					+ classToBeInstrumented);
		}

		CtMethod methodCopy = CtNewMethod.copy(method, classToBeInstrumented,
				null);
		methodCopy.setBody(methodBody);
		classToBeInstrumented.addMethod(methodCopy);

	}

	private void generateCMPSetter(CtMethod method,
			CtClass classToBeInstrumented) throws Exception {

		String fieldName = Introspector.decapitalize(method.getName()
				.replaceFirst("set", ""));

		ProfileAttribute profileAttribute = profileComponent
				.getProfileAttributes().get(fieldName);

		JPAProfileEntityFramework profileEntityFramework = (JPAProfileEntityFramework) profileComponent
				.getProfileEntityFramework();

		String pojoCmpAccessorSufix = ClassGeneratorUtils
				.getPojoCmpAccessorSufix(fieldName);

		// define the string of the object to store in the profile entity
		// if it is not a primitive do a deep copy of the object
		String objectToStore = !profileAttribute.isPrimitive() ? "("
				+ method.getParameterTypes()[0].getName() + ")"
				+ ObjectCloner.class.getName() + ".makeDeepCopy($1)" : "$1";
		// if it is an array convert it to a list
		if (profileAttribute.getType().isArray()) {
			objectToStore = ProfileAttributeArrayValueUtils.class.getName()
					+ ".toProfileAttributeArrayValueList( "
					+ profileEntityFramework
							.getProfileEntityArrayAttrValueClassMap().get(
									profileAttribute.getName()).getName()
					+ ".class , profileEntity, profileEntity.get"
					+ pojoCmpAccessorSufix + "() , "
					+ (profileAttribute.isUnique() ? true : false) + " , "
					+ objectToStore + ")";
		}

		String methodBody = "{" + ProfileCmpHandler.class.getName()
				+ ".beforeSetCmpField(profileObject);" + "	try {" + " "
				+ profileEntityFramework.getProfileEntityClass().getName()
				+ " profileEntity = ("
				+ profileEntityFramework.getProfileEntityClass().getName()
				+ ")profileObject.getProfileEntity();"
				+ "	    profileEntity.set" + pojoCmpAccessorSufix + "("
				+ objectToStore + ");" + "	}" + "	finally {"
				+ ProfileCmpHandler.class.getName()
				+ ".afterSetCmpField(profileObject);" + "	};" + "}";

		if (logger.isTraceEnabled()) {
			logger.trace("Adding method named " + method.getName()
					+ ", with source : " + methodBody + ", into: "
					+ classToBeInstrumented);
		}

		CtMethod methodCopy = CtNewMethod.copy(method, classToBeInstrumented,
				null);
		methodCopy.setBody(methodBody);
		classToBeInstrumented.addMethod(methodCopy);

	}

	/**
	 * Generates info that indicates if a method from {@link Profile} interface
	 * should be invoked or not, in runtime. Note that all methods from {@link ProfileManagement}
	 * that we are interested are all contained in {@link Profile} interface.
	 */
	private void generateProfileConcreteClassInfo(CtClass profileConcreteClass) {

		final ClassPool pool = profileComponent.getClassPool();
		
		CtClass profileClass = null;
		try {
			profileClass = pool.get(Profile.class.getName());
		} catch (NotFoundException e) {
			throw new SLEEException(e.getMessage(), e);
		}

		ProfileConcreteClassInfo profileConcreteClassInfo = profileComponent.getProfileConcreteClassInfo();

		for (CtMethod method : profileClass.getDeclaredMethods()) {
			for (CtMethod profileConcreteMethod : profileConcreteClass
					.getMethods()) {
				if (profileConcreteMethod.getName().equals(
						method.getName())
						&& profileConcreteMethod.getSignature().equals(
								method.getSignature())) {
					// match, save info
					profileConcreteClassInfo.setInvokeInfo(profileConcreteMethod
							.getMethodInfo().getName(), !profileConcreteMethod
							.isEmpty());
					break;
				}
			}
		}
	}
}
